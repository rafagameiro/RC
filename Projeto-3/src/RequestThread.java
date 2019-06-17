import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


import Exception.*;

/**
 * 
 */

/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */
public class RequestThread extends Thread {
	
	private static final String NOT_FOUND = "404";
	private static final String NOT_IMPLEMENTED = "501";
	private static final String RANGE_NOT_SATISFIABLE = "416";
	private static final int BUFFER_MAX_SIZE = 80;
	private static final int CONSTANT = 1000000;
	private static final int PORT = 80;
	
	private String path;
	private int fileSize;
	private String movie;
	private String host;
	
	/**
	 * 
	 * @param movie the movie name to request the server
	 * @param host the server name
	 * @param path the path from the server directory, to the file to request
	 */
	public RequestThread(String movie, String host, String path) {
		// TODO Auto-generated constructor stub
		this.fileSize = CONSTANT;
		this.path = path;
		this.movie = movie;
		this.host = host;
		this.path = path;
		
	}
	
	/**
	 * The thread starts running as a background process
	 */
	public void run() {
		try {
			
			getMovie(movie, host);
			
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("The server could not found the file.");
		} catch (NotImplementedException e) {
			// TODO Auto-generated catch block
			System.out.println("The server does not implement the request operation.");
		}
	}
	
	/**
	 * The method creates the header to send to the server
	 * After defining the initial and last bytes the threads want to request,
	 * the method will send the request through the OutputStream
	 * 
	 * @param host server name
	 * @param out channel from where the program will send the request
	 * @param initial initial byte the program requests to the server
	 * @param last the last byte program wants the server to send
	 * @throws IOException in case the output channel was malformed
	 */
	void sendsHttpRequest(String host, OutputStream out, int initial, int last) throws IOException {
		String request = String.format("GET %s HTTP/1.0\r\n"+
			       "Host: %s\r\n"+
			       "Range: bytes=" + initial + "-" + last + "\r\n"+
			       "User-Agent: X-RC2018\r\n\r\n", path, host);
		out.write(request.getBytes());
	}
	
	/**
	 * The method parses the header of the package received from the server
	 * Verifies if the server sent an 404 or 501 code and if not,
	 * it will parse the entire header so then it can retrieve the fileSize( on the first run)
	 * and the last byte received from the server
	 * 
	 * @param in channel from where the program will receive the server answer
	 * @return the last byte sent by the server
	 * @throws IOException in case the program could not read the Http header correctly
	 * @throws NotFoundException in case the server sends an error code 404 "Not Found"
	 * @throws NotImplementedException in case the server sends an error code 501 "Not Implemented"
	 * @throws RangeNotSatisfiableException in case the server sends an error code 416 "Range Not Satisfiable"
	 */
	int parsesHttpRequest(InputStream in) throws IOException, NotFoundException, NotImplementedException, RangeNotSatisfiableException {

		String[] result = Http.parseHttpReply(Http.readLine(in));
		
		switch(result[1]) { 
		 case NOT_IMPLEMENTED: throw new NotFoundException();
		 case NOT_FOUND: throw new NotImplementedException(); 
		}
		
		String line;
		int lastByte = -1;
		while(!(line = Http.readLine(in)).isEmpty()) {			
			if(line.contains("Content-Range")) {
				System.out.println(line);
				fileSize = Integer.parseInt(line.substring(line.indexOf("/")+1));
    			if(result[1].equalsIgnoreCase(RANGE_NOT_SATISFIABLE)) {
    				throw new RangeNotSatisfiableException();
    			}
    			String range = line.split("-")[2];
    			range = range.substring(0, range.indexOf("/"));
    			lastByte = Integer.parseInt(range);
    		}
    	}

		return lastByte; 
	}
	
	/**
	 * The method receives an input channel from where will receive the file content 
	 * so then it write it into the file through the fileOutputStream
	 * 
	 * @param in channel from where the program will receive the server answer
	 * @param fos channel from the program will write the content received from the server into the file
	 * @throws IOException in case the input channel was malformed
	 */
	private void writeInFile(InputStream in, FileOutputStream fos) throws IOException {
		// TODO Auto-generated method stub
		int n;
		byte[] buffer = new byte[BUFFER_MAX_SIZE];
		
		while( (n = in.read(buffer) ) != -1 ) {
			fos.write(buffer, 0, n);
		}
		in.close();
	}
	
	/**
	 * 
	 * @param movie the movie name requested  to the server
	 * @return the channel where the program will be able to write into the file,
	 * 		   or null in case of error
	 */
	private FileOutputStream createFile(String movie) {
		// TODO Auto-generated method stub
		try {
			File file = new File(movie);
			FileOutputStream fos = new FileOutputStream(file);
			
			return fos;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Some error happened while creating the file.");
		}
		return null;
	}
	
	/**
	 * Obtains movie from an HTTP server at ip+port
	 * The thread will request parts of the file, so then the server can send them
	 * After receiving them, the thread parses them and write them into the newly created file
	 * 
	 * @param movie the movie name to request the server
	 * @param host the server name
	 * @throws NotFoundException in case the server sends an error code 404 "Not Found"
	 * @throws NotImplementedException in case the server sends an error code 501 "Not Implemented"
	 */
	void getMovie(String movie, String host) throws NotFoundException, NotImplementedException {
		Socket sock;
		FileOutputStream fos = createFile(movie);
		int initial = 0;
		int last = CONSTANT;
		while(initial < fileSize) {
			try {			
					sock = new Socket( host, PORT );
					OutputStream out = sock.getOutputStream();
					InputStream in = sock.getInputStream();
					sendsHttpRequest(host, out, initial, last);
					initial = parsesHttpRequest(in);
					writeInFile(in, fos);
					
					initial++;
					last = initial + CONSTANT;
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("An error ocurred during the socket creation.");
			} catch (RangeNotSatisfiableException e) {
				// TODO Auto-generated catch block
				last = fileSize;
			} 
		}
	}


}
