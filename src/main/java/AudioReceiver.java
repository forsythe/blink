import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AudioReceiver {
    public static void main(String[] args) throws LineUnavailableException, IOException {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 44100.0F, 8, 2, 2, 44100.0F, false);
        SourceDataLine speakers;
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        speakers.open(format);
        speakers.start();

        String hostname = "localhost";
        int port = 444;

        InetAddress address = InetAddress.getByName(hostname);
        DatagramSocket socket = new DatagramSocket();
        byte[] receiveData = new byte[1024];

        while (true) {
            DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(response);
            speakers.write(response.getData(), 0, response.getLength());
        }
    }
}
