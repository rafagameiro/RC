
import java.net.*;


/**
 * @param args
 */

public class EchoServer  {
	

	static final int PORT = 8000 ;
	
	public static void main(String[] args) throws Exception {
		
		// create input / output UDP socket
		DatagramSocket socket = new DatagramSocket( PORT ) ;
		
		for(;;) { // server endless loop
			// wait for an incoming datagram
			byte[] buffer = new byte[65536] ;
			DatagramPacket echoRequest = new DatagramPacket( buffer, buffer.length ) ;
			//get it
			socket.receive( echoRequest ) ;
			//get UDP datagram payload
			byte[] echoRequestData = echoRequest.getData();
			int echoRequestLength = echoRequest.getLength() ;
			System.out.println( "Recebi: "+new String( echoRequestData, 0, echoRequestLength));
			//prepare an UDP datagram with the reply
			DatagramPacket echoReply = new DatagramPacket( echoRequestData, echoRequestLength ) ;
			// as well as destination IP address and port
			echoReply.setAddress( echoRequest.getAddress() ) ;
			echoReply.setPort( echoRequest.getPort() ) ;
			//send reply
			socket.send( echoReply ) ;
		}
	}
}
