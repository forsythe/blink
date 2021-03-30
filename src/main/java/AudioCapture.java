import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Optional;

import static javax.sound.sampled.AudioSystem.getMixerInfo;

public class AudioCapture {
//    public static void displayMixerInfo() {
//        Mixer.Info[] mixersInfo = getMixerInfo();
//
//        for (Mixer.Info mixerInfo : mixersInfo) {
//            System.out.println("Mixer: " + mixerInfo.getName());
//
//            Mixer mixer = AudioSystem.getMixer(mixerInfo);
//
//            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
//            for (Line.Info info : sourceLineInfo)
//                showLineInfo(info);
//
//            Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
//            for (Line.Info info : targetLineInfo)
//                showLineInfo(info);
//        }
//    }
//
//
//    private static void showLineInfo(final Line.Info lineInfo) {
//        System.out.println("  " + lineInfo.toString());
//
//        if (lineInfo instanceof DataLine.Info) {
//            DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
//
//            AudioFormat[] formats = dataLineInfo.getFormats();
//            for (final AudioFormat format : formats)
//                System.out.println("    " + format.toString());
//        }
//    }

    /**
     * my stereo mix supports:
     * PCM_UNSIGNED unknown sample rate, 8 bit, mono, 1 bytes/frame,
     * PCM_SIGNED unknown sample rate, 8 bit, mono, 1 bytes/frame,
     * PCM_SIGNED unknown sample rate, 16 bit, mono, 2 bytes/frame, little-endian
     * PCM_SIGNED unknown sample rate, 16 bit, mono, 2 bytes/frame, big-endian
     * PCM_UNSIGNED unknown sample rate, 8 bit, stereo, 2 bytes/frame,
     * PCM_SIGNED unknown sample rate, 8 bit, stereo, 2 bytes/frame,
     * PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame, little-endian
     * PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame, big-endian
     */

    static boolean broadcast = true;

    public static void main(String[] args) throws LineUnavailableException, IOException {
        DatagramSocket listenSocket = new DatagramSocket(Shared.port);
        byte[] clientReqBuf = new byte[256];
        DatagramPacket clientReqPacket = new DatagramPacket(clientReqBuf, clientReqBuf.length);
        listenSocket.receive(clientReqPacket); //blocks until I connect


        Optional<Mixer.Info> stereoMixInfo = Arrays.stream(getMixerInfo()).filter(i -> i.getName().toLowerCase().contains("stereo mix")).findFirst();
        if (!stereoMixInfo.isPresent())
            throw new IllegalStateException("No mixer named [Stereo Mix] found! Please enable and/or rename in control panel.");

        Mixer.Info info = stereoMixInfo.get();
        TargetDataLine targetLine = AudioSystem.getTargetDataLine(Shared.format, info);

        System.out.println("Recording");
        targetLine.open(Shared.format);
        targetLine.start();
        int numBytesRead;
        byte[] buffer = new byte[Shared.bufferSize];
        DatagramSocket socket = new DatagramSocket();

        boolean aboveNoise;
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
            DatagramPacket req = new DatagramPacket(buffer, numBytesRead, clientReqPacket.getAddress(), clientReqPacket.getPort());
            socket.send(req);
        }
        targetLine.stop();
        targetLine.close();
    }
}
