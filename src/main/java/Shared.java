import javax.sound.sampled.AudioFormat;
import java.net.InetAddress;

public class Shared {
    public static final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 2, 4, 44100.0F, false);
    public static final int bufferSize = 128;
    public static final int port = 444;
}
