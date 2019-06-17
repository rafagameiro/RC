import java.net.* ;
import java.util.* ;


public class EchoClient {

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
		
		// Ler o pedido do canal "standard input".
		System.out.printf("Qual a mensagem? " ) ;
		String request = in.nextLine() ;
		byte[] requestData = request.getBytes() ;
		
		// Criar a mensagem para enviar
		DatagramPacket echoRequest = new DatagramPacket( requestData, requestData.length, serverAddress, port ) ;

		// echoRequest.setAddress( serverAddress ) ;
		// echoRequest.setPort( port ) ;

		socket.send( echoRequest ) ;
		
		// Criar uma mensagem vazia para receber a resposta
		byte[] buffer = new byte[65536] ;
		DatagramPacket echoReply = new DatagramPacket( buffer, buffer.length ) ;
		
		socket.receive( echoReply ) ;
		socket.close() ;
		
		// Constroi uma string com o conteudo da mensagem
		String echoedMsg = new String( echoReply.getData(), 0, echoReply.getLength() ) ;
		System.out.printf("A resposta foi: \"%s\"\n", echoedMsg ) ;
	}
}
