import java.net.*;

public class URLparse {
    public static void main(String[] args) throws Exception {
	// URL url = new URL("http://example.com:80/docs/books/tutorial" 
	//+ "/index.html?name=networking#DOWNLOADING");
	URL url = new URL(args[0]);
	System.out.println("protocol = " + url.getProtocol());
	System.out.println("authority = " + url.getAuthority());
	System.out.println("host = " + url.getHost());
	System.out.println("port = " + url.getPort());
	System.out.println("path = " + url.getPath());
	System.out.println("query = " + url.getQuery());
	System.out.println("filename = " + url.getFile());
	System.out.println("ref = " + url.getRef());
	System.out.println(url);
    }
}
