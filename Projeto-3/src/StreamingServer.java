import java.io.File;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */
public class StreamingServer {
	
	private static final int LIMIT_START_STREAM = 100000;
	private static final String DUMMY_HOST = "localhost";
	private static final String DUMMY_PATH = "/";

	/**
	 * Receives and handles a client request
	 * Firstly, it will parse the request
	 * After that checks if the proxy server has the movie, and
	 * if it has calls a thread to send it
	 * If it does not have, the program will first download it from the server, 
	 * and then stream it 
	 * After receiving some bytes from the server, the program will start streaming the video,
	 * even if does not have the full file at the moment
	 * 
	 * @param s socket from the program will receive the server request
	 * @param cache programs cache that contains the already in cache files
	 * @throws Exception in case the getRquest() throws an Exception
	 */
	void handlePlayerRequest(Socket s, SmartCache cache) throws Exception {
		String[] request = getRequest(s);
		
		StreamingThread streamThread;
		if(cache.hasContent(request[0])) {
				 
		     streamThread = new StreamingThread(request[0], request[3], Integer.parseInt(request[4]));
		     streamThread.start();
		     
		} else {			
			RequestThread requestThread;
		    requestThread = new RequestThread(request[0], request[1], request[2]);
		    requestThread.start();
		    cache.addContent(request[0]);
		    
		    Path path = Paths.get(request[0]);
		    while(!Files.exists(path));
		    	 
		    File file = new File(request[0]);
		    		
		    int length = 0;
		    do {
		    length = (int) file.length();
		    	 
		    } while(length < LIMIT_START_STREAM);
		    streamThread = new StreamingThread(request[0], request[3], Integer.parseInt(request[4]));
			streamThread.start();
		}
		streamThread.join();
	}

	/**
	 * Receives the request from the client and parses it.
	 * For example: http://asc.di.fct.unl.pt/rc/movies/monsters.dat?ip=localhost&port=1234
	 * answer[0] = monsters.dat, answer[1] = asc.di.fct.unl.pt, answer[2] = /rc/movies/monsters.dat, 
	 * answer[3] = localhost, answer[4] = 1234,
	 * 
	 * @param s the socket form where the program receives the information
	 * @return an array with the fileName, the host, the path to the file, the ip address and the port
	 * @throws Exception in case an error occurred with socket, or the input channel
	 */
	private String[] getRequest(Socket s) throws Exception {
		// TODO Auto-generated method stub
		InputStream in = s.getInputStream();
		String [] answer = new String[5];
		
		boolean firstTime = true;
		String [] request = null;
		String line = "";
		while(!(line = Http.readLine(in)).isEmpty()) {
			if(firstTime) {
				firstTime = false;
				request = Http.parseHttpRequest(line);
			}
		}
		in.close();
		s.close();
		
		if(request[1].contains("http")) {
			URLparse url = new URLparse(request[1]);

			answer[0] = url.getFilename();
			answer[1] = url.getHost();
			answer[2] = url.getPath();
			answer[3] = url.getIP();
			answer[4] = url.getPort();

		} else {
			RequestParse url = new RequestParse(request[1]);
			
			answer[0] = url.getFilename();
			answer[1] = DUMMY_HOST;
			answer[2] = DUMMY_PATH;
			answer[3] = url.getIP();
			answer[4] = url.getPort();
		}
		
		return answer;
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if( args.length != 1 ) {
			System.err.println("usage: java   -cp .:vlcproxy.jar StreamingServer port") ;
			System.exit(0) ;
		}
		
		int port = Integer.parseInt( args[0] );

		proxy.VlcProxy.start( port ); //entry point of the vlcproxy.jar
		SmartCache cache = new SmartCache();
		cache.addContent("monsters.dat");
		for(;;) {
			try(ServerSocket ss = new ServerSocket ( port )) {
				Socket s = ss.accept();
				new StreamingServer().handlePlayerRequest( s , cache);
			} catch (Exception e) {
				e.printStackTrace();
			};
		}		

	}
}
