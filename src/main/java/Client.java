import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws Exception {
        Client.run();
    }

    private static void run() throws Exception {
        Socket socket = new Socket("localhost", 444);
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ps.println("Hello to Server from client");

        InputStreamReader ir = new InputStreamReader(socket.getInputStream());
        BufferedReader br = new BufferedReader(ir);

        String message;
        while ((message = br.readLine()) != null) {
            System.out.println(message);
        }
        System.out.println(message);

    }
}
