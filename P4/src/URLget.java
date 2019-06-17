import java.io.*;
import java.net.*;

// Using class URL to get an object

public class URLget {

    public static void main (String[] args) throws Exception {
	String line;
	URL url = new URL(args[0]);
	InputStream is = url.openStream();
	// Conversao para Buffered Data Input Stream
	// metodo readLine() permite a leitura simplificada
	BufferedReader in = new BufferedReader(new InputStreamReader(is));
	while ((line = in.readLine()) != null) {
            System.out.println(line);
	    // complete: get the object in the HTTP reply payload using input stream in
	}
    }
   
}
