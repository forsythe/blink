import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioClient {

    public static void main(String[] args) throws LineUnavailableException, IOException {
        if (args.length != 1) {
            System.out.println("Usage: gradlew runClient --args=<hostname>");
            return;
        }

        //tcp subscribe to server
        Socket clientSocket = new Socket(args[0], Shared.tcpPort);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out.println("LET ME IN");

            //udp subscribe to server
            byte[] buf = new byte[Shared.bufferSize];
            InetAddress address = InetAddress.getByName(args[0]);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Shared.udpPort);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet); //say hi for server to register us

            listenForTcpDisconnect(pool, in, socket);

            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, Shared.format);
            try (SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo)) {
                speakers.open(Shared.format);
                speakers.start();

                byte[] receiveData = new byte[Shared.bufferSize];
                while (true) {
                    DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(response);
                    speakers.write(response.getData(), 0, response.getLength());
                }
            }
        } catch (Exception ignored) {
        } finally {
            pool.shutdownNow();
        }
    }

    private static void listenForTcpDisconnect(ExecutorService pool, BufferedReader in, DatagramSocket socket) {
        pool.submit(() -> {
            String resp;
            try {
                while (((resp = in.readLine()) != null)) {
                    //server alive
                }
            } catch (IOException ignored) {
                System.out.println("Server shutdown!");
                socket.close();
            }
        });
    }
}
