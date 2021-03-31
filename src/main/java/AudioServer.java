import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static javax.sound.sampled.AudioSystem.getMixerInfo;

public class AudioServer {
    static List<SocketAddress> clients = new CopyOnWriteArrayList<>();

    /**
     * Track which clients are connected via TCP
     *
     * @throws IOException
     */
    static void registerClientTcp() throws IOException {
        ServerSocket svSocket = new ServerSocket(Shared.tcpPort);

        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.submit(() -> {
            while (true) {
                new AudioClientHandler(svSocket.accept(), (tcpAddrStr) -> {
                    String tcpAddrStrIp = tcpAddrStr.toString().substring(0, tcpAddrStr.toString().indexOf(':'));
                    clients = clients.stream().filter(udpAddr -> !udpAddr.toString().contains(tcpAddrStrIp)).collect(Collectors.toList());
                    printRegisteredClients();
                }).start();
            }
        });
    }

    /**
     * Actually send audio to clients via UDP
     *
     * @throws SocketException
     */
    static void registerClientUdp() throws SocketException {
        DatagramSocket listenSocket = new DatagramSocket(Shared.udpPort);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.submit(() -> {
            while (true) {
                byte[] clientReqBuf = new byte[256];
                DatagramPacket clientReqPacket = new DatagramPacket(clientReqBuf, clientReqBuf.length);
                try {
                    listenSocket.receive(clientReqPacket); //blocks until I connect
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.add(clientReqPacket.getSocketAddress());
                printRegisteredClients();
            }
        });
    }

    private static void printRegisteredClients() {
        System.out.println("Registered clients: " + clients);
    }

    public static void main(String[] args) throws LineUnavailableException, IOException {
        InetAddress IP = InetAddress.getLocalHost();
        System.out.println("Server running on " + IP.getHostAddress());

        registerClientTcp();
        registerClientUdp();

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

            boolean broadcast = true;
            while (broadcast) {
                numBytesRead = targetLine.read(buffer, 0, Shared.bufferSize);
                for (SocketAddress clientAddr : clients) {
                    DatagramPacket req = new DatagramPacket(buffer, numBytesRead, clientAddr);
                    socket.send(req);
                }
            }
            targetLine.stop();
        }
    }
}
