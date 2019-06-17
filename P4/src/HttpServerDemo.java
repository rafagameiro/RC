import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/** A really simple HTTP server
 * 
 * @author The class instructors
 *
 */

public class HttpServerDemo {
    
    static final int PORT = 8080;

    
    /**
     * Sends an error message "Not Implemented"
     */
    private static void sendsNotSupportedPage(OutputStream out) 
	throws IOException {
	String page = 
	    "<HTML><BODY>Demo server: request Not Supported</BODY></HTML>";
	int length = page.length();
	String header = "HTTP/1.0 501 Not Implemented\r\n";
	header += "Date: "+new Date().toString()+"\r\n";
	header += "Content-type: text/html\r\n";
	header += "Server: "+"X-Server-RC2018"+"\r\n";
	header += "Accept-Ranges: none\r\n";
	header += "Content-Length: "+String.valueOf(length)+"\r\n\r\n";
	header += page;
	out.write(header.getBytes());
    }

    /**
     * Sends a simple valid page with the text of the parameter simplePage
     */
    private static void sendsSimplePage(String simplePage, OutputStream out) 
	throws IOException {
	String page = "<HTML><BODY>Demo server: "+simplePage+"</BODY></HTML>";
	int length = page.length();
	String header = "HTTP/1.0 200 OK\r\n";
	header += "Date: "+new Date().toString()+"\r\n";
	header += "Content-type: text/html\r\n";
	header += "Server: "+"X-Server-RC2018"+"\r\n";
	header += "Accept-Ranges: none\r\n";
	header += "Content-Length: "+String.valueOf(length)+"\r\n\r\n";
	header += page;
	out.write(header.getBytes());
    }

    
    private static void processClientRequest(Socket s) {
	try {
	    InputStream in = s.getInputStream();
	    OutputStream out = s.getOutputStream();
	    String line = Http.readLine(in);
	    System.out.println("\nGot: \n\n"+line);
	    String[] request = Http.parseHttpRequest(line);
	    // ignore, but print the header of the http message
	    line = Http.readLine(in);
	    while ( ! line.equals("") ) {
		System.out.println(line);
		line = Http.readLine(in);
	    }
	    System.out.println();
	    // the requested object must be a locally accessible file
	    if( request[0].equalsIgnoreCase("GET") && request[1] != "") {
		sendFile(request[1], out);
	    } else {
		sendsNotSupportedPage(out);
	    }
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}
    }

    
    /**
     * sendFile: when available, sends the file in the URL to the client
     * 
     */
    private static void sendFile (String fileName, OutputStream out) throws IOException {
	// strips the leading "/"
	String name = fileName.substring(1);
	File f = new File(name);
	System.out.println("Sending file: \""+name+"\"");
	if ( name == "" ) sendsSimplePage ("The empty name is not a file",out);
	else if ( !f.exists() ) sendsSimplePage ("File "+fileName+" does not exist",out);
	else if ( ! f.isFile() ) sendsSimplePage ("File "+fileName+" is a directory",out);
	else if ( !f.canRead() ) sendsSimplePage ("File "+fileName+" cannot be read",out);
	else {
	    long size = f.length();
	    FileInputStream file = new FileInputStream(f);
	    StringBuilder header = new StringBuilder("HTTP/1.0 200 OK\r\n");
	    header.append("Date: "+new Date().toString()+"\r\n");
	    header.append("Server: "+"X-Server-RC2018"+"\r\n");
	    header.append("Accept-Ranges: none\r\n");
	    // header.append("Content-Type: "+"text/html"+"\r\n");
	    header.append("Content-Length: "+String.valueOf(size)+"\r\n\r\n");
	    out.write(header.toString().getBytes());
	    byte[] buffer = new byte[1024];
	    for(;;) {
		int n = file.read(buffer);
		if( n == -1) break;
		out.write(buffer, 0, n);
	    }
	    file.close();
	}
    }


    /**
     * MAIN - accept and handle client connections
     */

    public static void main(String[] args) throws IOException {
	ServerSocket ss = new ServerSocket( PORT );
	for (;;) {
	    try {
		System.out.println("\nHttp demo server ready at port "+PORT+ " waiting for request ...\n");
		Socket clientSock = ss.accept();
		processClientRequest( clientSock );
		clientSock.close();
	    } catch (Exception e ) {
		ss.close();
		System.err.println("Http demo server is going down");
		System.exit(-1);
	    }
	} 
    }

}
