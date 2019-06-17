import java.net.*;
import java.io.File;
import java.io.FileOutputStream;


/**
 * @param args
 */

public class FileServer  {
	

	static final int PORT = 8000 ;
	static final int MAX_SIZE = 1024 ;
	static final String ACK = "Acknowledged " ;
	
	public static void main(String[] args) throws Exception {
		
		// create input / output UDP socket
		DatagramSocket socket = new DatagramSocket( PORT ) ;
		
		byte[] buffer = new byte[MAX_SIZE] ;
		DatagramPacket nameRequest = new DatagramPacket( buffer, buffer.length ) ;
		
		while(nameRequest.getLength() == MAX_SIZE) { // server endless loop
			// wait for an incoming datagram
			//get it
			socket.receive( nameRequest ) ;

		}
		String[] fileInfo = new String(nameRequest.getData() , 0, nameRequest.getLength()).split(" - ");
//		String fileName = fileInfo[0];
		String fileName = "arsenal.txt";
		int fileSize = Integer.parseInt(fileInfo[1]);
		
		File file = new File(fileName);
		FileOutputStream fw = new FileOutputStream(file);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		
		DatagramPacket previousReceived = null;
		DatagramPacket fileAck = null;
		buffer = new byte[MAX_SIZE] ;
		String datagramStr = "";
		String fileText = "";
		String ackMsg = "";
		String message = "";
		int received = 0;
		while(received < fileSize) {
			DatagramPacket fileReceived = new DatagramPacket( buffer, buffer.length ) ;
			//get it
			socket.receive( fileReceived );
			if(previousReceived.equals(fileReceived)) {
				socket.send(fileAck);
				continue;
			}
			datagramStr = new String(fileReceived.getData(), 0, fileReceived.getLength());
			fileText = datagramStr.substring(datagramStr.indexOf('\n'));

			fw.write(fileText.getBytes());
			received += fileText.length();
			buffer = new byte[MAX_SIZE] ;
			previousReceived = fileReceived;
			
			ackMsg = datagramStr.substring(0, datagramStr.indexOf('\n'));
			message = ACK + ackMsg;
			
			fileAck = new DatagramPacket( message.getBytes(), message.length() ) ;
			// as well as destination IP address and port
			fileAck.setAddress( fileReceived.getAddress() ) ;
			fileAck.setPort( fileReceived.getPort() ) ;
			//send reply
			socket.send( fileAck ) ;
		}
		fw.flush();
		fw.close();
		socket.close();
	}
}
