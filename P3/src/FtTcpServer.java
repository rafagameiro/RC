//
// A simple TCP server for file transfer (C>S)
//

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FtTcpServer {

	static final int BLOCKSIZE = 512;
	public static final int PORT = 8000 ;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
	        // Creates the server socket to wait for connections
		ServerSocket serverSocket = new ServerSocket( PORT ) ;
		System.out.println("Server ready at port " +PORT);

	        for(;;) {
		        // Accepts a new connection from a client
			// Retruns a new socket used to talk with the client
			Socket clientSocket = serverSocket.accept() ;

                        // Creates an input stream to read
			InputStream is = clientSocket.getInputStream();

			int n ;
			byte[] buf = new byte[BLOCKSIZE] ;
			
			for ( n=0; n<BLOCKSIZE; n++ ) {  // le nome do ficheiro que o cliente envia
				int s = is.read();
				if ( s!=-1 ) buf[n]=(byte)s;
				else System.exit(1);
				if ( buf[n] == 0 ) break;
			}
		   
		        // Well... the server side file will be called
			// tmp.out .... It could be said by the client
			// in a first sent message, example receiving
			// a byte array, terminating with \0
		   
			System.out.println("Receiving: '"+new String(buf, 0, n)+"'");
			FileOutputStream f = new FileOutputStream("tmp.out");

			while( (n = is.read( buf )) > 0 ) // write to file
				f.write( buf, 0, n ) ;
			
                        // Close the socket
			clientSocket.close();
                        // Close the file
			f.close();
		}

	}


}
