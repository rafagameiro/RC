import java.io.*;
import java.net.*;
import java.util.*;

public class EchoClient {

	private static final int PORT = 8000;

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.out.println("usage: java EchoClient maquina_do_servidor");
			System.exit(0);
		}
		String server = args[0];

		// Criar um scanner auxiliar para ler linhas completas da consola.
		try (Scanner in = new Scanner(System.in)) {

			// Cria uma conexao para o servidor
			try (Socket socket = new Socket(server, PORT)) {
				OutputStream os = socket.getOutputStream();
				
				try( Scanner sin =  new Scanner( socket.getInputStream()) ) {
					String echoRequest;
					do {
						echoRequest = in.nextLine() + "\n";
						os.write(echoRequest.getBytes());
						String echoReply = sin.nextLine();
						System.out.printf("Server replied:: \"%s\"\n", echoReply);
					} while (!echoRequest.equals("!end\n"));					
				}

			}
		}
	}
}
