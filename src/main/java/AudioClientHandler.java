import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.function.Consumer;

public class AudioClientHandler extends Thread {
    private Socket clientSocket;
    private Consumer<SocketAddress> onClose;

    public AudioClientHandler(Socket socket, Consumer<SocketAddress> onClose) {
        this.clientSocket = socket;
        this.onClose = onClose;
    }

    public void run() {
        System.out.println("Hi " + clientSocket.getRemoteSocketAddress());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String input;
            while ((input = in.readLine()) != null) {
                //TODO: it's alive
            }
        } catch (IOException ignored) {

        } finally {
            System.out.println("Bye " + clientSocket.getRemoteSocketAddress());
            onClose.accept(clientSocket.getRemoteSocketAddress());
        }
    }
}
