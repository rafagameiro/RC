//
// Cliente para envio de ficheiros em TCP
// utlizando canais TCP com sockets TCP
//

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FtTcpClient {

    static final int BLOCKSIZE = 512; // Accord. to server
    static final int PORT = 8000 ;    // Server port

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

	if ( args.length != 2 ) {
	    System.out.println("usage: FTTCPClient maquina_servidor file_name");
	    System.exit(0);
	}
	String server = args[0];
	String filename = args[1];
	Socket socket = new Socket(server,PORT) ;
	OutputStream os = socket.getOutputStream();

	int n ;
	byte[] buf = new byte[BLOCKSIZE];

        long byteCount=0;
        long blockCount=0;
        long speed=0;

	System.out.println("Sending: '"+filename+"'");
	FileInputStream f = new FileInputStream(filename);

        long t0=System.currentTimeMillis();
        System.out.println(t0);

	os.write(filename.getBytes());
	buf[0]=0;
	os.write(buf,0,1); 
	while( (n = f.read( buf )) > 0 ) {
	    os.write( buf, 0, n ) ;
            byteCount +=n;
            blockCount +=1;
            System.out.print(".");
	}

        long t1=System.currentTimeMillis();
        System.out.println(t1);
	System.out.println("\nDone!");

	// Fecha e liberta os recursos que usou para este cliente
	socket.close();
	f.close();

        long dur = t1-t0;
        System.out.println(dur);
	speed = 1000 * 8 * Math.round( byteCount / dur );
	System.out.printf("%d blocks and %d bytes sent, in %d milli seconds, at %d bps\n", blockCount, byteCount, dur, speed );
        
    }

}

