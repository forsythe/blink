import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static javax.sound.sampled.AudioSystem.getMixerInfo;

public class AudioCapture {
    public static void displayMixerInfo() {
        Mixer.Info[] mixersInfo = getMixerInfo();

        for (Mixer.Info mixerInfo : mixersInfo) {
            System.out.println("Mixer: " + mixerInfo.getName());

            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
            for (Line.Info info : sourceLineInfo)
                showLineInfo(info);

            Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
            for (Line.Info info : targetLineInfo)
                showLineInfo(info);
        }
    }


    private static void showLineInfo(final Line.Info lineInfo) {
        System.out.println("  " + lineInfo.toString());

        if (lineInfo instanceof DataLine.Info) {
            DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;

            AudioFormat[] formats = dataLineInfo.getFormats();
            for (final AudioFormat format : formats)
                System.out.println("    " + format.toString());
        }
    }

    /**
     * Stereo mix supports:
     * PCM_UNSIGNED unknown sample rate, 8 bit, mono, 1 bytes/frame,
     * PCM_SIGNED unknown sample rate, 8 bit, mono, 1 bytes/frame,
     * PCM_SIGNED unknown sample rate, 16 bit, mono, 2 bytes/frame, little-endian
     * PCM_SIGNED unknown sample rate, 16 bit, mono, 2 bytes/frame, big-endian
     * PCM_UNSIGNED unknown sample rate, 8 bit, stereo, 2 bytes/frame,
     * PCM_SIGNED unknown sample rate, 8 bit, stereo, 2 bytes/frame,
     * PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame, little-endian
     * PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame, big-endian
     */

    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        Optional<Mixer.Info> stereoMixInfo = Arrays.stream(getMixerInfo()).filter(i -> i.getName().toLowerCase().contains("stereo mix")).findFirst();
        if (!stereoMixInfo.isPresent())
            throw new IllegalStateException("No mixer named [Stereo Mix] found! Please enable and/or rename in control panel.");

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 44100.0F, 8, 2, 2, 44100.0F, false);
        Mixer.Info info = stereoMixInfo.get();// new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine targetLine = AudioSystem.getTargetDataLine(format, info);

        System.out.println("Recording");
        targetLine.open(format);
        targetLine.start();

        Thread thread = new Thread(() -> {
            AudioInputStream audioStream = new AudioInputStream(targetLine);
            File audioFile = new File("record.wav");
            try {
                AudioSystem.write(audioStream,
                        AudioFileFormat.Type.WAVE, audioFile);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("stopped recording");
        });

        thread.start();
        Thread.sleep(5000);
        targetLine.stop();
        targetLine.close();
        System.out.println("Done");
    }
}
