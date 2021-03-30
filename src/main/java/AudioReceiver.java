import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class AudioReceiver {
    public static void main(String[] args) throws LineUnavailableException, IOException {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 2, 4, 44100.0F, false);
        SourceDataLine speakers;
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        speakers.open(format);
        speakers.start();

//        String hostname = "192.168.128.95";
        int port = 444;

//        InetAddress address = InetAddress.getByName(hostname);
        DatagramSocket socket = new DatagramSocket(port);
        byte[] receiveData = new byte[1024];

        while (true) {
            DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(response);
//            System.out.println(Arrays.toString(response.getData()));
            speakers.write(response.getData(), 0, response.getLength());
        }
    }
}
