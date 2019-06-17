/*
* Sender.java - envio de stream para endereco multicast
*/

package Streaming;

import java.io.*;
import java.net.*;

class SenderProgram {


	static public void main( String []args ) throws Exception {
	    if( args.length != 3 ) {
		System.err.println("usage: java SenderProgram file_name address port") ;
		System.exit(0) ;
	    }

   	        int size;
		int count = 0;
 		long time;
		DataInputStream g = new DataInputStream( new FileInputStream(args[0]) );
		byte[] buff = new byte[65000];
		MulticastSocket s = new MulticastSocket();
		//DatagramSocket s = new DatagramSocket();
		InetSocketAddress addr = new InetSocketAddress( args[1], Integer.parseInt(args[2]));
		DatagramPacket p = new DatagramPacket(buff, buff.length, addr );

	    long t0 = System.nanoTime(); // tempo de referencia para este processo
		long q0 = 0;

		while ( g.available() > 0 ) {
			size = g.readShort();
			time = g.readLong();
		   
			System.out.println("Size: "+size+" time: "+time);
			if ( count == 0 ) q0 = time; // tempo de referencia no stream
			count += 1;
		   
			g.readFully(buff, 0, size );
			p.setData(buff, 0, size );
			p.setSocketAddress( addr );
			
			System.out.println(time);
		   
			long t1 = System.nanoTime()/1000000;
			long diff = t1 - t0;
			long waitTime = time - diff;
			
			if(waitTime > 0) Thread.sleep(time - diff);
		        
			s.send( p );
			t0 = t1;
			System.out.print( "." );
		}

		System.out.println("DONE! packets sent: "+count);
	}

}
