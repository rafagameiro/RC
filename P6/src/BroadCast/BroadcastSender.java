package BroadCast;


import java.net.*;
import java.io.*;
import java.util.*;

public class BroadcastSender {

    public static void main(String[] args ) throws Exception {
	if( args.length != 3 ) {
	    System.err.println("usage: java BroadcastSender end-ip-broadcast porto time-interval") ;
	    System.exit(0) ;
	}
 
	int clockTicks=20; // change if needed

	int port = Integer.parseInt( args[1]) ;
	InetAddress serveraddr = InetAddress.getByName(args[0] ) ;
	int timeinterval = Integer.parseInt( args[2]) ;
	String msg;


	DatagramSocket bs = new DatagramSocket() ;
	do {
	    msg = new Date().toString();
	    bs.send( new DatagramPacket( msg.getBytes(), msg.getBytes().length, serveraddr, port ) ) ;
	    --clockTicks;

	    try {
		Thread.sleep(1000*timeinterval);
	    } 
	    catch (InterruptedException e) { }

	} while( clockTicks >0 ) ;
	msg="fim";
	bs.send( new DatagramPacket( msg.getBytes(), msg.getBytes().length, serveraddr, port ) ) ;
	bs.close();
	    
    }
}

