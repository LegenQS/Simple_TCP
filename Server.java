package UDP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int window_size = 10;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private static Set<Integer> readed = Collections.synchronizedSet(new HashSet<>());
    private static Set<Integer> sent = Collections.synchronizedSet(new HashSet<>());

    private TreeSet<Integer> transmitted = new TreeSet<>();
    private Deque<Package> sent_queue = new LinkedList<>();

    public static void main(String[] args)  {
        int port = 41191, receive_port = 41194;
        String file_name = "src/TCP_IP/java/test.txt";
        OutputStream out = null;
        byte[] receive_data = new byte[1024];

        // static data loaded from local file
        Data data = loadData(file_name);

        if (data == null) {
            System.out.println("Input file is null");
            return;
        }

        Reader read = new Reader(receive_port, "src/UDP/server.txt", readed, sent);
        Sender send = new Sender(port, readed, sent);
//        System.out.println(data.getLength());
        executor.submit(read);
        int i = -1;
        while (i < data.getLength() && !read.isEnd()) {

            // send next available package
            try {
                if (i >= data.getLength()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                // if ack matches the sequence number, then send next
                if (i <= data.getLength() - 1) {
                    if (send.isTimeout(System.nanoTime()))
                        if (i != data.getLength())
                            send.setData(data.getData(i), read.getNext_ack());
                        else send.setFIN(1);
                    else {
                        if (i < data.getLength() - 1)
                            send.setData(data.getData(++i), read.getNext_ack());
                        else send.setFIN(1);
                    }
                }
                executor.submit(send);
                System.out.println("sending " + i + " package");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // sleep to avoid unexpected exception for socket close
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server shuts downs");
        executor.shutdown();
        send.close();
        read.close();

    }

    public static Data loadData(String file_path) {
        // total file data
        Data data = null;
        try {
            data = new Data(file_path);
        }
        catch (FileNotFoundException e) {
            System.out.println("Input file not found!");
        }
        catch (Exception e1) {
            System.out.println("Unexpected error found");
        }
        return data;
    }

    private static ServerSocket startServer(int port) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("port error");
        }
        return ss;
    }

    private static Socket startSocket (ServerSocket ss) {
        Socket s = null;
        try {
            s = ss.accept();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("Socket initialization error");
        }
        return s;
    }

    private static void close(ServerSocket ss, Socket s, OutputStream out) {
        try {
            ss.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            s.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
