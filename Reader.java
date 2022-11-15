package UDP;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
public class Reader implements Callable {
    // sender and reader share common sending queue and reading queue
    private Set<Integer> sent;
    private Set<Integer> readed;
    private boolean end_sign = false;
    private static DatagramSocket socket;
    private static DatagramPacket packet;
    private ByteArrayInputStream bint;
    private ObjectInputStream oint;
    private FileOutputStream fileout;
    private static HashSet<Integer> read_package;
    private int total;
    private int seq;
    private byte[] data;
    private int next_ack;

    public Reader (int port, String file_path, Set<Integer> readed, Set<Integer> sent) {
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        data = new byte[1024];
        packet = new DatagramPacket(data, data.length);
        try {
            fileout = new FileOutputStream(file_path, true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.readed = readed;
        this.sent = sent;
    }

    @Override
    public Object call() {
        try {
            int i = 1;
            while (!end_sign) {
                socket.receive(packet);
                bint = new ByteArrayInputStream(data);
                oint = new ObjectInputStream(bint);
                TCPPackage message = (TCPPackage) oint.readObject();
                if (corrupted(message)) {
                    // set next ack with the same seq number of the received package
                    next_ack = message.getSeq();
                    System.out.println("message corrupted");
                }
                else {
//                    if (getFIN(message) && next_ack == message.getSeq()) {
//                        break;
//                    }
                    if (getFIN(message)) {
                        end_sign = true;
                        fileout.write("Connection breaks, goodbye".getBytes());
                        break;
                    }
                    fileout.write(message.getData());
                    total++;
                    next_ack++;
                }
            }
        } catch (Exception e) {
            System.out.println("Reader exception occurred");
        }
        System.out.println("reading complete");
        return true;
    }

    public void close() {
        socket.close();
        try {
            if (fileout != null)
                fileout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (bint != null)
                bint.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (oint != null)
                oint.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getSeq() {
        return this.seq;
    }

    public int getTotal() {
        return total;
    }

    public int getNext_ack() {return next_ack;}

    public DatagramSocket getSocket() {return this.socket;}
    public boolean getFIN(TCPPackage message) {
        if (message.getFIN() == 1) return true;
        return false;
    }

    public boolean isEnd() {
        return this.end_sign;
    }

    public boolean corrupted(TCPPackage pack) {
        long check_sum = pack.setCheckSum();
        if (check_sum != pack.getChecksum()) {
            return true;
        }
        return false;
    }

    public long getCheckSum(byte[] arr) {
        long s = 0;
        for (byte b : arr) s += b;
        return s;
    }

    public void getReaderQueue(Queue<TCPPackage> queue) {

        if (!queue.isEmpty()) {

        }
        else {
            try {
                throw new Exception("Queue is Empty");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


}

