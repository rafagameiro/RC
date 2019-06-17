package MultiCast;

import java.net.*;
import java.io.*;
import java.util.*;

public class MulticastSender {

    public static void main(String[] args ) throws Exception {
	if( args.length != 3 ) {
	    System.err.println("usage: java MulticastSender  grupo_multicast porto time-interval") ;
	    System.exit(0) ;
	}
	System.setProperty("java.net.preferIPv4Stack", "true");
 
	int clockTicks=20; // change if needed

	int port = Integer.parseInt( args[1]) ;
	InetAddress group = InetAddress.getByName( args[0] ) ;
	int timeinterval = Integer.parseInt( args[2]) ;

        System.out.println("Host addr: " + InetAddress.getLocalHost().getHostAddress());
	System.out.println("Destination mcast address: " + group);
        System.out.println("Port: " + port);

	String msg;

	if( !group.isMulticastAddress() ) {
	    System.err.println("Multicast address required...") ;
	    System.exit(0) ;
	}

	MulticastSocket ms = new MulticastSocket() ;
        ms.setTimeToLive(3);  // Ex., putting TTL = 3

	do {
	    msg = new Date().toString();
            System.out.println("sending ...." +msg);
	    ms.send( new DatagramPacket( msg.getBytes(), msg.getBytes().length, group, port ) ) ;
	    --clockTicks;

	    try {
		Thread.sleep(1000*timeinterval);
	    } 
	    catch (InterruptedException e) { }

	} while( clockTicks >0 ) ;
	msg="tchau!";
	ms.send( new DatagramPacket( msg.getBytes(), msg.getBytes().length, group, port ) ) ;
	ms.close();
	    
    }
}

