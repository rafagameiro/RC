package tftp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import tftp.TFtpPacketV18.OpCode;
/**
 * 
 */
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
/**
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */
public class Tftp {
	
	static final String OCTET = "octet";
	static final String BLKSIZE = "blksize";
	static final int MAX_PROTOCOL_SIZE = 65464;
	static final int MAX_ACK_SIZE = 4;
	static final int MAX_SIZE = 512;
	static final int MAX_TRIES = 5;
	
	private static int blkNum;
	private static int blkSizeD;
	
	private static SocketAddress socketAddress;
	private static Stats stats;
	
	/**
	 * Checks if the package received is an error message, and
	 * if it is, displays the error message, and aborts the program.
	 * 
	 * @param ack package received from server
	 */
	private static boolean isError(TFtpPacketV18 ack) {
		if(ack.getOpCode().compareTo(OpCode.OP_ERROR) == 0) {
			if(ack.getErrorCode() == 5)
				return true;
			System.out.println(ack.getErrorMessage());
			System.exit(1);
		}
		return false;
	}
	
	/**
	 * Checks if the package received is an error message, and
	 * if it is, displays the error message, and aborts the program.
	 * 
	 * @param ack package received from server
	 * @param ackNum 
	 * @throws SocketException 
	 */
	private static boolean isAck(DatagramSocket socket, TFtpPacketV18 ack, double rtt, boolean last) throws SocketException {
		stats.increaseAcks();
		double timeout = 0.0;
		boolean is = false;
		
		if(ack.getOpCode().compareTo(OpCode.OP_OACK) == 0) {
			rtt = System.currentTimeMillis() - rtt;
			timeout = stats.calculateInitialTimeout(rtt);
			blkSizeD = Integer.valueOf(ack.getOptionValue(BLKSIZE));
			socket.setSoTimeout((int) timeout);
			is = true;
		} else if(ack.getOpCode().compareTo(OpCode.OP_ACK) == 0) 
			if(ack.getBlockNumber()+1 == blkNum) {
				rtt = System.currentTimeMillis() - rtt;
				timeout = stats.calculateTimeout(rtt);
				socket.setSoTimeout((int) timeout);
				is = true;
				
				if(last)
					stats.setComputedTimeout(timeout);
			}
		
		return is;
	}
	
	/**
	 * Creates a datagram packet so then the program can send it to the server.
	 * 
	 * @param payload byte array that contains file content
	 * @param length number of bytes read, inside of payload
	 * @return datagram packet with payload inside  
	 */
	private static DatagramPacket sendDataPackage(byte[] payload, int length) {
		// TODO Auto-generated method stub
		TFtpPacketV18 fileContent = new TFtpPacketV18(OpCode.OP_DATA);
		fileContent.putShort(blkNum);
		fileContent.putBytes(payload, length);
		return fileContent.toDatagramPacket(socketAddress);
	}
	
	/**
	 * Creates a datagram packet, that has filename, the type of transfer and the size of the payload,
	 * so then the program can send it to the server.
	 * 
	 * @param filename name of the file the program will transfer
	 * @param blkSize user proposal of datagram's payload size
	 * @return datagram packet properly formed according to TFTP 
	 */
	private static DatagramPacket sendWRQPackage(byte[] filename, String blkSize) {
		// TODO Auto-generated method stub
		TFtpPacketV18 nameFile = new TFtpPacketV18(OpCode.OP_WRQ);
		nameFile.putBytes(filename);
		nameFile.putByte(0);
		nameFile.putBytes(OCTET.getBytes());
		nameFile.putByte(0);
		nameFile.putBytes(BLKSIZE.getBytes());
		nameFile.putByte(0);
		nameFile.putBytes(blkSize.getBytes());
		nameFile.putByte(0);
		
		return nameFile.toDatagramPacket(socketAddress);		
	}
	
	/**
	 * Starts transferring the file data, splitted between packets with the accorded size between the program and the server
	 * Each time a packet is sent and the respective acknowledge is received, a new timeout is calculated
	 * If the defined timeout is reached and an acknowledge to the last packet was not received
	 * the system throws a SocketTimeoutException, sending another packet, hoping for the server to answer with an acknowledge
	 * If the program sent the same packet 5 times and did not get an answer from the server, the program will terminate
	 * 
	 * @param socket socket where the program sends the datagram
	 * @param pkg datagram to send
	 * @param last is true, if it is the last datagram, false otherwise
	 * @return 0 zero in case of success
	 * @throws IOException in case the datagram socket its not correctly created
	 */
	private static int sendRetry(DatagramSocket socket, DatagramPacket pkg, boolean last) throws IOException {
		
		int ackNum = blkNum++;
		byte[] ackBuffer = new byte[MAX_PROTOCOL_SIZE];
		DatagramPacket ack = new DatagramPacket(ackBuffer, ackBuffer.length);
		double currRTT = System.currentTimeMillis();
		int tries = 1;
		for(;tries < MAX_TRIES;) {
			
			try {
				socket.send(pkg);
				stats.increaseDataBlocks();
				
				while(ackNum != blkNum) {
					socket.receive(ack);
					
					TFtpPacketV18 fileAck = new TFtpPacketV18(ack.getData(), ack.getLength());
					if(isError(fileAck))
						continue;
					
					if(isAck(socket, fileAck, currRTT, last)) 
						return 0;
					else {	
						tries++;
						break;
					}
				}
				
			} catch(SocketTimeoutException e) {
				tries++;
			} 
		}
		return 0;
	}
	
	/**
	 * Initially opens a socket to send packets to the server
	 * Creates the first packet, a WRQ (Write ReQuest), to send to the server
	 * After receiving the option acknowledge (OACK), the program defines the max size the payload can have and sets the timeout
	 * After the transfer was concluded, the program presents the transfer size, the time the transfer took,
	 * the transfer rate (in Megabits per second) and the number of packets sent and acknowledges received
	 * Also it presents the minimum and maximum timeout and RTT calculated, and their average values
	 * 
	 * @param fileName name of the file the program will transfer
	 * @param server machine's ip address 
	 * @param port server's port where it will receive packets
	 * @param blkSize
	 * @throws IOException in case the datagram socket its not correctly created
	 */
	private static void sendFile(String fileName, InetAddress server, int port, String blkSize) throws IOException {
		// TODO Auto-generated method stub
		socketAddress = new InetSocketAddress(server, port);
		DatagramSocket socket = new DatagramSocket() ;	
		TFtpPacketV18 nameAck = null;
		byte[] name = fileName.getBytes();
		
		boolean last = false;
		sendRetry(socket, sendWRQPackage(name, blkSize), last);
		
		File file = new File(fileName);
		FileInputStream fr = new FileInputStream(file);
		
		DatagramPacket pkg = null;
		byte[] requestFile = new byte[blkSizeD];
		int readSize = 0;
		blkNum = 1;
		
		while(true) {
			readSize = fr.read(requestFile);
			if(readSize != -1)
				stats.increaseBytes(readSize);
			
			if(readSize != -1) {
				pkg = sendDataPackage(requestFile, readSize);
				last = false;
			} else {
				pkg = sendDataPackage(requestFile, 0);
				last = true;
			}
			
			sendRetry(socket, pkg, last);
			if(file.length() % blkSizeD != 0 && fr.available() == 0)
				break;
			else if(readSize == -1)
				break;
		}
		stats.setAvgRtt();
		
		socket.close();
		fr.close();
	
	}
	
	/**
	 * @param args
	 */

	public static void main(String[] args) throws Exception { 
	int port; 
	String blkSize;
	String fileName; 
	InetAddress server;

	switch (args.length) {
    	case 4:
    		server = InetAddress.getByName(args[0]);
    		port = Integer.valueOf(args[1]);
    		fileName = args[2];
    		blkSize = args[3];
    		break;
    	default:
    		System.out.printf("usage: java %s server port filename blksize\n", Tftp.class.getName());
    		return;
	}

	stats = new Stats();
	sendFile(fileName, server, port, blkSize);
	stats.printReport();

	}

}
