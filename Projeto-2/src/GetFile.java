import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */

public class GetFile {
	
	private static final int OK = 200;
	private static final int PARTIAL = 206;
	private static final int NOT_FOUND = 404;
	private static final int NOT_IMPLEMENTED = 501;
	private static final int RANGE_NOT_SATISFIABLE = 416;
	private static final int CONSTANT = 3000000;
	private static final int BUFFER_MAX_SIZE = 80;
	private static final int HEADER_FIELDS = 3;
	
	private static boolean[] serverNotFound = {false,false,false,false};
	private static int[] serverPort = {8080,8081,8082,8083};
	private static int fsize = 10000000;
	private static int counter;
	private static String fileName;
	private static boolean stop;
	private static Stats stats;
	
	/**
	 * The file the program will receive needs to be less than 1Mbyte.
	 * The program receives the full file from the server, with just one reply.
	 * 
	 * @param in channel where the program receives information from the server
	 * @throws IOException 
	 */
	private static void getFullFile(InputStream in) throws IOException {
		int n;
		byte[] buffer = new byte[BUFFER_MAX_SIZE];
		try {
			File f = new File (fileName);
			FileOutputStream fos = new FileOutputStream(f);
		
		    while( (n = in.read(buffer) ) != -1 ) {
		    	fos.write(buffer, 0, n);
		    }
		    
		    fos.close();
		    in.close();
		    stop = true;
		} catch ( FileNotFoundException e ) {
			System.out.println("Some error happened while creating the file.");
		} catch ( IOException e) {
			System.out.println("Error while receiving the file from server.");
		};
	}
	
	/**
	 * The file the program will receive needs to be more than 1Mbyte, but less than 10Mbytes.
	 * The program receives parts of the requested file from the server,
	 * where this method you'll write those parts into the file created by the program.
	 * 
	 * @param in channel where the program receives information from the server
	 * @param fos channel where the program writes information to the file transfered
	 * @return true in case the 
	 * @throws IOException in case the method codeAndRange throws an IOException
	 */
	private static void getPartialFile(InputStream in, FileOutputStream fos) throws IOException {
		// TODO Auto-generated method stub
		int n;
		byte[] buffer = new byte[BUFFER_MAX_SIZE];
		
		while( (n = in.read(buffer) ) != -1 ) {
			fos.write(buffer, 0, n);
		}
		in.close();
	}
	
	/**
	 * Creates a file, where the program will write the information received from the server.
	 * Firstly, writes the first reply sent by the server, where the request was made in the serverProcessing method
	 * After that, the program will use Round-Robin to change between servers in order to prevent performance drops.
	 * Every iteration, the program receives a code from the server, 
	 * where it will check if the code is 404 NOT_FOUND or 501 NOT_IMPLEMENTED and if it is,
	 * it will change to another server and request the same range of bytes.
	 * In the last request in order the receive the remaining file's bytes, if the new limit is more than the total size of the file,
	 * the limit becomes the total size of the file less 1.
	 * 
	 * @param u URL typed by the user
	 * @param in channel where the program receives information from the server
	 * @param contentLength last byte received from the server 
	 * @throws IOException in case the channel to connect to the file is malformed
	 */
	private static void serversRR(URL u, InputStream in, int contentLength) throws IOException {
		// TODO Auto-generated method stub
		InputStream is = in;
		File f = new File (fileName);
		FileOutputStream fos = new FileOutputStream(f);
		boolean bytesNotTransfer = false;
		int[] header = {0, 0, contentLength};
		
		getPartialFile(is, fos);
		
		counter++;
		int min = contentLength+1;
		int max = (int) (min + CONSTANT >= fsize ? fsize-1 : min + CONSTANT);
		double decrement = 1.0; 
		
		bytesNotTransfer = true;
		for( ; ; ) {
			
			if(stop) break;
			
			if(!serverNotFound[counter]) {
				if(!bytesNotTransfer) {
					min = header[2]+1;
					max = (int) (min + CONSTANT/decrement >= fsize ? fsize-1 : min + CONSTANT/decrement);
				}
				bytesNotTransfer = false;
				is = sendRequest(u, min, max);
				header = processHeader(is);
				if(header[0] == NOT_FOUND || header[0] == NOT_IMPLEMENTED)
					bytesNotTransfer = true;
				else
					getPartialFile(is, fos);
			    
			   
			}
			counter++;
			
			if(counter == 4) {
				counter = 0; 
				decrement = decrement >= 3.0 ? decrement : decrement+0.5;
				
			}
			
			if(!bytesNotTransfer && max == fsize-1)
				stop = true;
		}
		fos.close();
		is.close();
	}
	
	/**
	 * Reads the first line of the header, corresponding to the status line, 
	 * and save the code sent by the server inside the array header.
	 * Gets the amount of bytes received by the server and save them in Stats class
	 * Gets the range of bytes from where-to-where the server sent the file content, 
	 * and also it gets the total size of the file.
	 * The range is saved inside the array header.
	 * 
	 * @param in channel where the program receives information from the server
	 * @return an array with information about the code in the header sent by the server,
	 * 		   the last byte received from the server, and the total of size of the file.
	 * @throws IOException in case the readLine method throws an IOException
	 */
	private static int[] processHeader(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		int[] header = new int[HEADER_FIELDS];
    	String line;
    	stats.increaseHttpReply();
		stats.setTotalTimeRequestReply();
    	
    	while(!(line = Http.readLine(in)).isEmpty()) {
    		
    		if(line.contains("HTTP/1.0")) {
    			String[] result = Http.parseHttpReply(line);
    	    	header[0] = Integer.parseInt(result[1]);
    	    	
    		} else if(line.contains("Content-Length")) {
    			int bytesReceived = Integer.parseInt(line.substring(16));
    			header[1] = bytesReceived;
            	stats.setTotalBytes(bytesReceived);
    		} else if(line.contains("Content-Range")) {
    			String rangeReceived = line.substring(15);  
            	int[] ranges = Http.parseRangeValuesSentByServer(rangeReceived);
            	header[2] = ranges[1];
            	fsize = ranges[2];
    		}
    			
    	}

    	return header;
    	
	}
	
	/**
	 * Prepares the header the program will send to the server.
	 * Creates a socket, where opens an output channel and an input channel,
	 * so that the program can send the header, and then receive the reply from the server.
	 * 
	 * @param u URL typed by the user
	 * @param initial byte the program wants the server to start sending
	 * @param limit byte the program wants the server to stop
	 * @return the channel where the program receives information from the server
	 * @throws IOException in case the socket is malformed
	 */
	private static InputStream sendRequest(URL u, int initial, int limit) throws IOException {
		// Assuming URL of the form http://server-name/path ....
		String rangeInfo = limit == -1 ? "Range: bytes=" + initial + "- \r\n" : "Range: bytes=" + initial + "-" + limit + "\r\n";
		
		//int port = u.getPort() == -1 ? serverPort[counter] : u.getPort();
		int port = serverPort[counter];
    	String path = u.getPath() == "" ? "/" : u.getPath();
    	Socket sock = new Socket( u.getHost(), port );
    	OutputStream out = sock.getOutputStream();
    	InputStream in = sock.getInputStream();
		String request = String.format("GET %s HTTP/1.0\r\n"+
			       "Host: %s\r\n"+
			       rangeInfo+
			       "User-Agent: X-RC2018\r\n\r\n", path, u.getHost());
		
		out.write(request.getBytes());
		stats.increaseRequests();
		stats.startRequestTime();
		
		return in;
	}
	
	/**
	 * Firstly, it will change the value inside the array serverNotFound, 
	 * so then the program will not ever do requests to that server.
	 * If the reply sent by the server has an 404 NOT_FOUND or 501 NOT_IMPLEMENTED code,
	 * The program will inform the user that will switch to next server in queue.
	 * If all servers in queue sent error codes, the program will terminate.
	 * 
	 * @param errorCode code sent by the server
	 */
	private static void handlerServerError(int errorCode) {
		// TODO Auto-generated method stub
		serverNotFound[counter] = true;
		if(errorCode == NOT_IMPLEMENTED) {
			System.out.println("Server on port " + serverPort[counter] + " does not implement the requested action.");
		} else
			System.out.println("Server on port " + serverPort[counter] + " did not found the requested file."); 
		
		if(counter+1 >= 4)
			System.out.println("All server cannot solve the requested action. Exiting...");
		else
			System.out.println("Switching to server on port " + serverPort[counter+1] + "...");
	}
	
	/**
	 * Sends the first request to the server.
	 * After analysing the code, if it is an OK code, 
	 * the program will transfer the all file at once.
	 * If it is a RANGE_NOT_SATISFIABLE code the program will execute the same method, 
	 * but this time it will not set a limit to number of bytes the program wants from the server.
	 * If it is a NOT_IMPLEMENTED or NOT_FOUND code, 
	 * the program will change the value of the variable inside of the array serverNotFound to true, in the position
	 * that corresponds to the server.
	 * 
	 * @param u URL typed by the user
	 * @param initial byte the program wants the server to start sending
	 * @param last byte the program wants the server to stop
	 * @throws IOException in case the method getCode, sendRequest throws an IOException
	 */
	private static void serverProcessing(URL u, int initial, int last) throws IOException {
		// Assuming URL of the form http://server-name/path ....
		InputStream in = sendRequest(u, initial, last);
		
		int[] header = processHeader(in);
		
    	switch(header[0]) {
    	 case OK: getFullFile(in);
    	 	break;
    	 case PARTIAL: serversRR(u, in, header[2]);
    	 	break;
    	 case RANGE_NOT_SATISFIABLE: serverProcessing(u, 0, -1);
    	 	break;
    	 case NOT_IMPLEMENTED: handlerServerError(NOT_IMPLEMENTED);	   
 	 		break;
    	 case NOT_FOUND: handlerServerError(NOT_IMPLEMENTED);
 	 		break;
    	}
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
    	if ( args.length != 1 ) {
    		System.out.println("Usage: java GetFile url_to_access");
    		System.exit(0);
    	}
    	String url = args[0];
    	URL u = new URL(url);
    	stats = new Stats();
	 
    	System.out.println(u.getHost());
    	System.out.println(u.getPath());
    	
    	String[] urlLine = url.split("/");
    	fileName = urlLine[urlLine.length-1];
    	stop = false;
    	counter = 0;
	
    	for( ; counter < serverPort.length ; counter++) {
    		serverProcessing(u, 0, CONSTANT);
    		
    		if(stop) break;
    	}
    	stats.statisticsProject();
	 
	} 

}
