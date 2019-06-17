//
// EchoServer in a Concurrent Version
// to handle concurrent clients in a multithread way
//

import java.io.* ;
import java.net.ServerSocket ;
import java.net.Socket ;

   // Thread handler to manage concurrent connections
   // You could try other alternatives (see the lab explanation)
   // Ex: Threads + Lambda Expression
   // Ex: Threads + Helper Class (implementing the interface Runnable)
   // Ex: Threads + Helper Class (that extends Thread)
   //           ... following this example

class HandlingThread extends Thread  {
      Socket connection;
   
      public HandlingThread (Socket c) 
	{
	   super("EchoSerer Handling Thread");
	   connection=c;
	}
   
      public void run() 
	{
        int n;
        byte[] buf= new byte[10];
	   
	try {
	    System.out.println("   > New Thread, got connection from "
			           +connection.getInetAddress().getHostName());

	    InputStream is = connection.getInputStream();
	    OutputStream os = connection.getOutputStream() ;
	
	    while ((n=is.read(buf)) >0)
	       os.write (buf,0,n);
	    connection.close();
            System.out.println("   > Connection closed, Thread finished !");
	   
	    } catch (IOException e) {
	      System.err.println("... Thread: Handling Error ");
	    }
	   
	}
}

// Main Echo-Server Thread
public class ConcurrentEchoServer {

	public static final int PORT = 8000 ;
	public static void main(String args[] ) throws Exception {

		// Cria o socket de atendimento do servidor
		ServerSocket serverSocket = new ServerSocket( PORT ) ;
		
		for(;;) { 
			// Espera por um cliente e retorna um novo socket 
			Socket clientSocket = serverSocket.accept() ;
		        System.out.println("New client connection accepted");
		        System.out.println("Start a new thread to handle it...");

		        HandlingThread servThread;
		        servThread = new HandlingThread(clientSocket);
		        servThread.start();
		}
	}
   
}