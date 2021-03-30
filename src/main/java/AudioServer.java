import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;

import static javax.sound.sampled.AudioSystem.getMixerInfo;

public class AudioServer {
    public static void main(String[] args) throws LineUnavailableException, IOException {
        InetAddress IP = InetAddress.getLocalHost();
        System.out.println("Server running on " + IP.getHostAddress());

        DatagramSocket listenSocket = new DatagramSocket(Shared.port);
        byte[] clientReqBuf = new byte[256];
        DatagramPacket clientReqPacket = new DatagramPacket(clientReqBuf, clientReqBuf.length);
        listenSocket.receive(clientReqPacket); //blocks until I connect

        System.out.println("Registered client: " + clientReqPacket.getAddress() + " on port " + clientReqPacket.getPort());
        Optional<Mixer.Info> stereoMixInfo = Arrays.stream(getMixerInfo()).filter(i -> i.getName().toLowerCase().contains("stereo mix")).findFirst();
        if (!stereoMixInfo.isPresent())
            throw new IllegalStateException("No mixer named [Stereo Mix] found! Please enable and/or rename in control panel.");

        Mixer.Info info = stereoMixInfo.get();
        try (TargetDataLine targetLine = AudioSystem.getTargetDataLine(Shared.format, info)) {

            System.out.println("Recording...");
            targetLine.open(Shared.format);
            targetLine.start();
            int numBytesRead;
            byte[] buffer = new byte[Shared.bufferSize];
            DatagramSocket socket = new DatagramSocket();

            boolean aboveNoise;
            boolean broadcast = true;
            while (broadcast) {
                aboveNoise = false;
                numBytesRead = targetLine.read(buffer, 0, Shared.bufferSize);
                for (int i = 0; i < numBytesRead; i++) {
                    if (Math.abs(buffer[i]) > 10) {
                        aboveNoise = true;
                        break;
                    }
                }
                if (!aboveNoise) {
                    continue;
                }
                DatagramPacket req = new DatagramPacket(buffer, numBytesRead, clientReqPacket.getSocketAddress());
                socket.send(req);
            }
            targetLine.stop();
        }
    }

}
