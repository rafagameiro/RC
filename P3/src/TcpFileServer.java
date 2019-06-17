
/**
 * TcpServer - servidor em TCP para transferencia de ficheiros 
 * para um cliente TCP.
 * Ver enunciado do trabalho 2 - RC 2012/2013
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpFileServer {

	static final int BLOCKSIZE = 512;

	public static final int PORT = 8000;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Cria o socket de atendimento deste servidor, de onde
		// aceita as conexões dos clientes

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {

			System.out.println("Server ready at port " + PORT);

			for (;;) {
				// Espera até que um cliente se ligue,
				try (Socket clientSocket = serverSocket.accept()) {

					 System.out.println("New client connection accepted");
				     System.out.println("Start a new thread to handle it...");

				     // trata do cliente referente ao novo socket
				     ConnectionHandler servThread;
				     servThread = new ConnectionHandler(clientSocket);
				     servThread.start();
					
				}
			}
		}

	}

	static class ConnectionHandler extends Thread {
		Socket cs;
		
		private static final int TMP_BUF_SIZE = 1024;
		
		 public ConnectionHandler (Socket c) 
			{
			   super("TcpServer Handling Thread");
			   cs = c;
			}

		public void run() {
			System.out.println("ola");
			try {			
				int n, bytes = 0;
				byte[] buf = new byte[TMP_BUF_SIZE];
				InputStream is = cs.getInputStream();
			
				while( (n = is.read()) > 0 && (buf[bytes] = (byte)n) != 0)
					bytes++;
			
				System.out.println("ola 2");
				String filename = new String( buf, 0, bytes);
				System.out.printf("Receiving:  %s\n", filename);
				FileOutputStream fos = new FileOutputStream( "T" + filename );
			
				while( (n = is.read( buf)) > 0)
					fos.write( buf, 0, n);
				
				 cs.close();
		         System.out.println("   > Connection closed, Thread finished !");
		         
			} catch (IOException e) {
			      System.err.println("... Thread: Handling Error ");
		    }
		}
	}
}
		
