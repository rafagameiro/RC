package MultiCast;

import java.net.*;
import java.io.*;
import java.util.*;

public class MulticastReceiver {

    public static void main(String[] args ) throws Exception {
	if( args.length != 2 ) {
	    System.err.println("usage: java MulticastReceiver grupo_multicast porto") ;
	    System.exit(0) ;
	}

        // if need to fix the IPV4 stack
        System.setProperty("java.net.preferIPv4Stack", "true");

	InetAddress group = InetAddress.getByName(args[0]);
	int port = Integer.parseInt(args[1]);

	if( !group.isMulticastAddress() ) {
	    System.err.println("Multicast address required...") ;
	    System.exit(0) ;
	}

	System.out.println("Host addr: " + InetAddress.getLocalHost().getHostAddress());
	System.out.println("Multicast group join to: " + group);

	MulticastSocket rs = new MulticastSocket(port) ;
	rs.joinGroup(group);

	DatagramPacket p = new DatagramPacket( new byte[65536], 65536 ) ;
	String msg;

	do {
	    p.setLength(65536); // resize with max size
            System.out.println("Received:");
	    rs.receive(p) ;

            msg=new String(p.getData(),0, p.getLength());

	    System.out.println("RECV: "+ msg  ) ;
	} while(!msg.equals("tchau!")) ;

	// rs.leave() ... if you want leave from the multicast group ...
	rs.close();
	    
    }
}
