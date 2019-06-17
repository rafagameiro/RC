import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

/**
 * 
 */

/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */
public class StreamingThread extends Thread {
	
	private String movie;
	private String ip;
	private int port;
	
	/**
	 * 
	 * @param movie the movie name to stream
	 * @param ip the ip associated to the vlc player
	 * @param port the port from where the thread will communicate with the vlc
	 */
	public StreamingThread(String movie, String ip, int port) {
		// TODO Auto-generated constructor stub
		this.movie = movie;
		this.ip = ip;
		this.port = port;
	}
	
	/**
	 * The thread starts running as a background process
	 */
	public void run() {
		try {
			
			sendStream(movie, ip, port);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("An error happened during the socket creation.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("An error happened while streaming the movie.");
		}
	}
	
	/**
	 * Sends an stream of movie named fileName to the UDP socket at ip+port
	 * While the file still has bytes to send, the method will process every frame
	 * in each frame, the program gets the timestamp and computes the difference with the program's time (waitTime)
	 * if the result is bigger than 0, this means the method should wait "waitTime" milliseconds,
	 * so then the frame will arrive at the right time without damaging the frames already sent
	 * 
	 * @param fileName the movie name to stream
	 * @param ip the ip associated to the vlc player
	 * @param port the port from where the thread will communicate with the vlc
	 * @throws IOException in case some error occurred with the socket
	 * @throws InterruptedException in case some error occurred during the stream play
	 */
	  void sendStream(String fileName, String ip, int port ) throws IOException, InterruptedException {
		  
	   	    int size;
			int count = 0;
	 		long time;
			DataInputStream g = new DataInputStream( new FileInputStream(fileName) );
			byte[] buff = new byte[65000];
			//MulticastSocket s = new MulticastSocket();
			DatagramSocket s = new DatagramSocket();
														
			InetSocketAddress addr = new InetSocketAddress( ip , port);
			DatagramPacket p = new DatagramPacket(buff, buff.length, addr );

		    long t0 = System.nanoTime(); //reference time to this process la

			while ( g.available() > 0 ) {
				//the number of bytes of the payload encoding the frame contents
				size = g.readShort();

				//the timestamp of the frame
				time = g.readLong();
			   
				g.readFully(buff, 0, size );
				p.setData(buff, 0, size );
				p.setSocketAddress( addr );
			   
				//current time to this process
				long t1 = System.nanoTime();
				
				//the difference between the current time and the reference time of the process
				long timePassed = t1 - t0;
				
				//compare the time of when the frame should come and timePassed
				long difTime = time - timePassed;
				
				//convert the waiting time to milliseconds
				long waitTime = difTime / 1000000; 
				
				//if the waitTime is bigger than 0 
				//this means that our program should wait "waitTime" milliseconds
				//for the frame arrive in the right time 
	            if (waitTime > 0) Thread.sleep(waitTime);
				s.send( p );
			}

		  s.close();
		  g.close();
	  }

}
