import java.net.* ;
import java.util.* ;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;


public class FileClient {

	static final int MAX_SIZE = 1000 ; // nao e 1024 por causa da string message
	static final int MAX_MESSAGE = 24 ;
	static final String PACKAGE = "Package number " ;
	
	/**
	 * @param args
	 */

	public static void main(String[] args) throws Exception {
		
		if( args.length != 2 ) {
			System.out.printf("usage: java EchoClient maquina_do_servidor porto\n") ;
			System.exit(0);
		}

		// Criar um scanner auxiliar para ler linhas completas do canal de entrada standard.
		Scanner in = new Scanner( System.in ) ;
		
		// Preparar endereco e o porto do servidor
		String servidor = args[0] ;
		int port = Integer.parseInt( args[1] ) ;
		InetAddress serverAddress = InetAddress.getByName( servidor ) ;
		
		// Preparar o socket para trocar mensagens (datagramas)
		DatagramSocket socket = new DatagramSocket() ;
		
		System.out.printf("Qual a ficheiro? " ) ;
		String filename = in.nextLine() ;
		
		File file = new File(filename);   
		FileInputStream fr = new FileInputStream(file);
		   
		byte[] requestData = new byte[MAX_SIZE];
		
		String dataCompress = filename + " - " + file.length();
		
		requestData = dataCompress.getBytes() ;
		
		DatagramPacket nameTransfer = new DatagramPacket( requestData, requestData.length, serverAddress, port ) ;
		socket.send( nameTransfer ) ;
		
		ByteArrayOutputStream requestCreater = new ByteArrayOutputStream();
		byte[] requestFile = new byte[MAX_SIZE];
		byte[] requestMessage = new byte[MAX_MESSAGE];
		byte[] ackMessage = new byte[MAX_SIZE];
		int msgNum = 1;
		int pkgNum = 0;
		
		socket.setSoTimeout(20000);
		while(fr.available() != 0) {
			String message = PACKAGE + msgNum + "\n";
			requestMessage = message.getBytes();
			fr.read(requestFile);
			requestCreater.write(requestMessage);
			requestCreater.write(requestFile);
			
			requestData = requestCreater.toByteArray();
			
			// Criar a mensagem para enviar
			DatagramPacket fileTransfer = new DatagramPacket( requestData, requestData.length, serverAddress, port ) ;
			
			while(pkgNum != msgNum) {
				socket.send( fileTransfer );
				DatagramPacket ackRequest = new DatagramPacket(ackMessage, ackMessage.length);
				socket.receive(ackRequest);
				String[] ack = new String(ackRequest.getData(), 0, ackRequest.getLength()).split(" ");
				System.out.println(new String(ackRequest.getData(), 0, ackRequest.getLength()));
				pkgNum = Integer.parseInt(ack[3]);
				
				if(pkgNum == msgNum) {
					requestCreater = new ByteArrayOutputStream();
					requestData = new byte[MAX_SIZE];
					requestFile = new byte[MAX_SIZE];
					requestMessage = new byte[MAX_MESSAGE];
					msgNum++;
					break;
				}
			}				
			
		}
		requestCreater.close();
		fr.close();
		socket.close() ;
		in.close();
	}
}
