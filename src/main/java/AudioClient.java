import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class AudioClient {
    public static void main(String[] args) throws LineUnavailableException, IOException {
        if (args.length != 1) {
            System.out.println("Usage: gradlew runClient --args=<hostname>");
            return;
        }

        //Subscribe to server
        byte[] buf = new byte[Shared.bufferSize];
        InetAddress address = InetAddress.getByName(args[0]);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Shared.port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet); //say hi for server to register us

        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, Shared.format);
        try (SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo)) {
            speakers.open(Shared.format);
            speakers.start();

            byte[] receiveData = new byte[Shared.bufferSize];
            boolean listen = true;
            while (listen) {
                DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(response);
                speakers.write(response.getData(), 0, response.getLength());
            }
            speakers.stop();
        }
    }
}
