import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

/** A really simple HTTP Client
 * 
 * @author The class instructors
 *
 */

public class HttpClientDemo {
    public static void main(String[] args) throws Exception {
	if ( args.length != 1 ) {
	    System.out.println("Usage: java HttpClientDemo url_to_access");
	    System.exit(0);
	}
	String url = args[0];
	URL u = new URL(url);
	// Assuming URL of the form http://server-name/path ....
	int port = u.getPort() == -1 ? 80 : u.getPort();
	String path = u.getPath() == "" ? "/" : u.getPath();
	Socket sock = new Socket( u.getHost(), port );
	OutputStream out = sock.getOutputStream();
	InputStream in = sock.getInputStream();
	String request = String.format("GET %s HTTP/1.0\r\n"+
				       "Host: %s\r\n"+
				       "Range: bytes=100-199\r\n"+
				       "User-Agent: X-RC2018\r\n\r\n", path, u.getHost());
	out.write(request.getBytes());
	System.out.println("\nSent:\n\n"+request);
	System.out.println("Got:\n");
	String answerLine = Http.readLine(in);
	System.out.println(answerLine);
	// String[] result = Http.parseHttpReply(answerLine);
	int contentLength = 0;
	answerLine = Http.readLine(in);
	while ( !answerLine.equals("") ) {
		if(answerLine.contains("Content-Length:"))
			contentLength = Integer.parseInt(answerLine.substring(answerLine.indexOf("Content-Length:")));
	    System.out.println(answerLine);
	    answerLine = Http.readLine(in);
	}
	System.out.println("\nPayload:\n");
	int n;
	byte[] buffer = new byte[80];
	try {
	    while( (n = in.read(buffer) ) != -1 ) {
		System.out.print(new String(buffer,0,n) );
	    }
	} catch ( Exception e ) {};
	sock.close();
    }

}
