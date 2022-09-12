package UDP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class TCPPackage implements Serializable {
    private static final long serialVersionUID = 36176481394L;
    private ArrayList<TCPPackage> packageQueue;
    private int total_count;
    private int sent_count;
    private int generated_count;

    // source port number
    private long source_port;
    // destination port number
    private long dest_port;
    // checksum of a package
    private long checksum;
    // receive window_size
    private int window_size;
    // FIN sign
    private int FIN = 0;
    // ACK sign
    private int Ack;
    // Sequence number of a package
    private int Seq;
    // data content of a package
    private byte[] data;
    // current received status by receiver
    private boolean status = false;

    public TCPPackage() {

    }


    public void amountCheck(){
        if (total_count - sent_count < window_size) {
            generatePackage();
        }
        else {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void generatePackage() {
        TCPPackage new_package = new TCPPackage();
//        Data data = Data.generateNext();
    }

    public void setData (byte[] data, int seq, int ack) {
        this.data = data;
        this.Seq = seq;
        this.Ack = ack;
        this.checksum = setCheckSum();
    }

    public long setCheckSum() {
        Checksum crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }
    public int getSeq() {
        return this.Seq;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getFIN() {
        return this.FIN;
    }

    public void setFIN(int FIN) {
        this.FIN = FIN;
    }

    public long getChecksum(){
        return checksum;
    }

    @Override
    public String toString() {

        String printdata = new String(this.data);
        return "sequence number: " + this.Seq + "\n" + printdata;
    }
}
