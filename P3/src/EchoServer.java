import java.io.* ;
import java.net.* ;

public class EchoServer {

	public static final int PORT = 8000;
	
	public static void main(String args[] ) throws Exception {

		// Cria o socket de atendimento do servidor
		
		try(ServerSocket serverSocket = new ServerSocket( PORT )) {
			for(;;) { 
		
				// Espera por um cliente 
				try(Socket clientSocket = serverSocket.accept()) {
					
					//Atende o cliente...
					new ConnectionHandler().handle( clientSocket );
				} catch( IOException x ) {
					x.printStackTrace();
				}
			}
		}
	}
	
	static class ConnectionHandler {
		
		private static final int TMP_BUF_SIZE = 16;

		void handle( Socket cs ) throws IOException {
			
			InputStream is = cs.getInputStream();
			OutputStream os = cs.getOutputStream() ;
			
			for(;;) { 
	            // implements the data ECHO, by reading and writing 
	            // while the connection is not closed
	            
	            int n ;
	            byte[] buf = new byte[ TMP_BUF_SIZE] ;
	            while( (n = is.read(buf)) > 0 )
	                os.write( buf, 0, n );
	        }
		}
	}
}
