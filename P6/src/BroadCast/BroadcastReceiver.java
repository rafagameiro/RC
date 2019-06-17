package BroadCast;

import java.net.*;
import java.io.*;
import java.util.*;

public class BroadcastReceiver {

    public static void main(String[] args ) throws Exception {
	if( args.length != 1 ) {
	    System.err.println("usage: java BroadcastReceiver porto") ;
	    System.exit(0) ;
	}

	int port = Integer.parseInt(args[0]) ;

	DatagramSocket ds = new DatagramSocket(port) ;

	DatagramPacket p = new DatagramPacket( new byte[65536], 65536 ) ;
	String msg;

	do {
	    p.setLength(65536); // resize with max size
	    ds.receive(p) ;
	    msg =  new String( p.getData(), 0, p.getLength() ) ;
	    System.out.println("Data/Hora recebida: "+ msg  ) ;
	} while(!msg.equals("fim")) ;

	// rs.leave() ... if you want leave from the multicast group ...
	ds.close();
	    
    }
}
