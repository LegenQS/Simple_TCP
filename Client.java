package UDP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private static Set<Integer> readed = Collections.synchronizedSet(new HashSet<>());
    private static Set<Integer> sent = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        int port = 41191, send_port = 41194;
        byte[] data = new byte[1024];

        int cnt = 0;
        Reader read = new Reader(port, "src/UDP/client.txt", readed, sent);
        Sender send = new Sender(send_port, readed, sent);
        executor.submit(read);
        int last_received = 0;
        while (true) {
            if (read.getNext_ack() == last_received + 1) {
                byte[] send_data = ("received your " + read.getNext_ack() + " package\n").getBytes();
                send.setData(send_data, read.getNext_ack());
                System.out.println("sending data");
                last_received++;
                executor.submit(send);
            }
            // send next available package
//            send.setData("received your package".getBytes());
//            executor.submit(send);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (read.isEnd()) {
                byte[] send_data = ("Okay let's break up connection").getBytes();
                send.setData(send_data, read.getNext_ack());
                send.setFIN(1);
                executor.submit(send);
                break;
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Client shuts down");
        executor.shutdown();
        send.close();
        read.close();
    }

    private static Socket startSocket (int port) {
        Socket s = null;
        try {
            s = new Socket(InetAddress.getLocalHost(), port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    private static InputStream startInput (Socket s) {
        InputStream input = null;
        try {
            input = s.getInputStream();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("Input stream initialized error");
        }
        return input;
    }

    private static OutputStream startOutput (Socket s) {
        OutputStream out = null;
        try {
            out = s.getOutputStream();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("Output stream initialized error");
        }
        return out;
    }
    private static void close(Socket s, InputStream input, OutputStream out) {
        try {
            if (s != null)
                s.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (input != null)
                input.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (out != null)
                out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
