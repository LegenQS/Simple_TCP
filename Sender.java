package UDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;

public class Sender implements Callable {
    // initial time out parameter
    private static final long RTT = 2131212313;
    // sender and reader share common sending queue and reading queue
    private Set<Integer> sent;
    private Set<Integer> read;
    private static DatagramSocket socket;
    private static DatagramPacket packet;
    private static int SEQ = 0;
    private static InetAddress host;
    private ObjectOutputStream oout;
    private ByteArrayOutputStream bout;
    private int port;
    private boolean timeout;
    private long sent_time;
    private byte[] data;
    private TCPPackage pack;

    public Sender (int port, Set<Integer> read, Set<Integer> sent) {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        try {
            host = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        this.port = port;
        this.pack = new TCPPackage();
        this.read = read;
        this.sent = sent;
    }

    public void setData(byte[] data, int ack) {
        this.pack.setData(data, this.SEQ, ack);
        this.sent_time = 0;
    }

    @Override
    public Object call() {
        bout = new ByteArrayOutputStream();
        try {
            oout = new ObjectOutputStream(bout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            sent_time = System.nanoTime();
            oout.writeObject(pack);        //序列化对象
            oout.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] sendBuff=bout.toByteArray();       //转化为字节数组
        packet = new DatagramPacket(sendBuff, sendBuff.length, host, port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sent.add(pack.getSeq());
        return null;
    }

    public void setFIN(int FIN) {
        pack.setFIN(FIN);
    }

    public void close() {
        socket.close();
        try {
            if (oout != null)
                oout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (bout != null)
                bout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DatagramSocket getSocket() {return this.socket;}

    public boolean isTimeout(long s) {
        if (sent_time == 0) return false;
        return s - sent_time >= RTT ? true : false;
    }
}
