import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws Exception {

        Server.run();
    }

    private static void run() throws Exception {
        ServerSocket socket = new ServerSocket(444);
        //Waits both client and server to accept and we return
        //a socket
        Socket client = socket.accept();
        //Once accepted
        InputStreamReader isr = new InputStreamReader(client.getInputStream());
        BufferedReader br = new BufferedReader(isr);

        String message;
        while ((message = br.readLine()) != null) {
            System.out.println("I read: [" + message + "] from Client");
            PrintStream ps = new PrintStream(client.getOutputStream());
            ps.println("Message Received");
            ps.println("Sent from Server");
        }

    }
}
