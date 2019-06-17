import java.io.*;
import java.net.*;
import java.util.Date;

/** A really simple and lazy HTTP server supporting ranges
 * 
 * @author The class instructors
 *
 */

public class HttpLazyServer {
    
    static final int PORT = 8080;
    //      static final int MAX_BYTES = 10000000;
            static final int MAX_BYTES = 100000;
    
    /**
     * Sends an error message "Not Implemented"
     */
    private static void sendsNotSupportedPage(OutputStream out) 
	throws IOException {
	String page = 
	    "<HTML><BODY>Lazy server: request not supported</BODY></HTML>";
	int length = page.length();
	String header = "HTTP/1.0 501 Not Implemented\r\n";
	header += "Date: "+new Date().toString()+"\r\n";
	header += "Content-type: text/html\r\n";
	header += "Server: "+"X-Server-RC2018"+"\r\n";
	header += "XAlmost-Accept-Ranges: bytes\r\n";
	header += "Content-Length: "+String.valueOf(length)+"\r\n\r\n";
	header += page;
	out.write(header.getBytes());
    }

    /**
     * Sends a simple valid page with the text of the parameter simplePage
     */
    private static void sendsSimplePage(String simplePage, OutputStream out) 
	throws IOException {
	String page = 
	    "<HTML><BODY>Lazy server: "+simplePage+"</BODY></HTML>\r\n";
	int length = page.length();
	String header = "HTTP/1.0 200 OK\r\n";
	header += "Date: "+new Date().toString()+"\r\n";
	header += "Content-type: text/html\r\n";
	header += "Server: "+"X-Server-RC2018"+"\r\n";
	header += "X-Almost-Accept-Ranges: bytes\r\n";
	header += "Content-Length: "+String.valueOf(length)+"\r\n\r\n";
	header += page;
	out.write(header.getBytes());
    }

    
    private static void processClientRequest(Socket s) {
	try {
	    InputStream in = s.getInputStream();
	    OutputStream out = s.getOutputStream();
	    int[] ranges = { 0,Integer.MAX_VALUE-1 };
	    String line = Http.readLine(in);
	    System.out.println("\nGot: \n\n"+line);
	    String[] request = Http.parseHttpRequest(line);
	    // ignore, but print the header of the http message
	    line = Http.readLine(in);
	    while ( ! line.equals("") ) {
		System.out.println(line);
		String[] header = Http.parseHttpHeader(line);
		if ( header[0].equalsIgnoreCase("Range") ) {
		    ranges = Http.parseRangeValues(header[1]);
		}
		line = Http.readLine(in);
	    }
	    System.out.println();
	    // the requested object must be a locally accessible file
	    if( request[0].equalsIgnoreCase("GET") && request[1] != "") {
		sendFile(request[1], ranges, out);
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
    private static void sendFile (String fileName, int[] ranges,
				  OutputStream out) throws IOException {
	// strips the leading "/"
	String name = fileName.substring(1);
	File f = new File(name);
	System.out.println("Sending file: \""+name+"\"");
	if ( name == "" ) sendsSimplePage ("The empty name is not a file",out);
	else if ( !f.exists() ) sendsSimplePage ("File \""+fileName+"\" does not exist",out);
	else if ( ! f.isFile() ) sendsSimplePage ("File \""+fileName+"\" is a directory",out);
	else if ( !f.canRead() ) sendsSimplePage ("File \""+fileName+"\" cannot be read",out);
	else {
	    
	    long fileSize = f.length();
	    long rangeSize = ranges[1] - ranges[0]; // MAX_VALUE-1 if ranges absent
	    long rest = fileSize - ranges[0];     // never sends more then available
	    if (rest > MAX_BYTES) rest=MAX_BYTES; // never sends more then MAX_BYTES
	    if (rest >= rangeSize) rest=rangeSize; // never sends more then demanded
	    // rest is <= still available && <= MAX_BYTES && <= demanded
	    // compute number of bytes to send
	    long size = rest <= 0? 0 : rest; // number of bytes to send
	    
	    boolean noRanges = false;
	    if (ranges[0] != 0 || ranges[1] <= fileSize ) noRanges = true;
	    if (rangeSize >= MAX_BYTES ) noRanges = false;
	    
	    RandomAccessFile file = new RandomAccessFile ( f, "r" );

	    StringBuilder header = new StringBuilder("");
	    if (noRanges) header.append("HTTP/1.0 200 OK\r\n");
	    else header.append("HTTP/1.0 206 Partial Content\r\n");
	    header.append("Date: "+new Date().toString()+"\r\n");
	    header.append("Server: "+"X-Server-RC2018"+"\r\n");
	    header.append("X-Almost-Accept-Ranges: bytes\r\n");
	    if ( !noRanges) header.append(
					  "Content-Range: bytes "+ranges[0]+"-"+(ranges[0]+size-1)+"\r\n");
	    // header.append("Content-Type: "+"text/html"+"\r\n");
	    header.append("Content-Length: "+String.valueOf(size)+"\r\n\r\n");
	    out.write(header.toString().getBytes());
	    byte[] buffer = new byte[size <= 1024? (int) size : 1024];
	    int totalSent = 0;
	    file.skipBytes(ranges[0]);
	    for(;;) {
		int n = file.read(buffer);
		if( n == -1) break;
		out.write(buffer, 0, n);
		totalSent += n;
		if ( totalSent >= size) break;
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
		System.out.println("\nHttp lazy server ready at port "+PORT+ " waiting for request ...");
		System.out.println("I only accept range requests in the form \"Range: bytes=x-y\"");
		System.out.println("I only accept to send at most "+MAX_BYTES+ " in each reply\n");
		Socket clientSock = ss.accept();
		processClientRequest( clientSock );
		clientSock.close();
	    } catch (Exception e ) {
		ss.close();
		System.err.println("Http lazy server is going down");
		System.exit(-1);
	    }
	}
    }

}
