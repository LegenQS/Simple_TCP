package UDP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class Data {
    // max package segment size
    private int MAX_SEGMENT_SIZE = 576;
    private int total_amount;
    private long checksum;
    private FileInputStream file_stream;
    private LinkedList<byte[]> total_data;
    private int data_length = 0;

    public Data() {

    }

    public Data(String file_path) throws FileNotFoundException {
        initFile(file_path);
    }

    private void initFile(String path) {
        File file = new File(path);
//        BufferedInputStream bis = new BufferedInputStream(file_stream);
        try {
            file_stream = new FileInputStream(file);
            total_data = new LinkedList<>();

            System.out.println("reading file....");
            byte[] bytes = new byte[MAX_SEGMENT_SIZE];
            int offset = 0;

            while ((offset = file_stream.read(bytes)) != -1) {
                data_length++;
                if (offset != MAX_SEGMENT_SIZE) {
                    total_data.add(Arrays.copyOf(bytes, offset));
//                    checksum += getCheckSum(total_data.get(data_length - 1));
                    break;
                }

                total_data.add(bytes);
//                checksum += getCheckSum(total_data.get(data_length - 1));
                bytes = new byte[MAX_SEGMENT_SIZE];
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                file_stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    public long getCheckSum(byte[] arr) {
//        long s = 0;
//        for (byte b : arr) s += b;
//        return s;
//    }
    // print byte to String
    public void printData() {
        int total = 0;
        for (byte[] b : total_data) {
            System.out.println(new String(b));
            total += b.length;
        }
        System.out.println(total);
    }

    public LinkedList<byte[]> getData() {
        return total_data;
    }

    public byte[] getData(int idx) {
        return total_data.get(idx);
    }

    public int getLength() {
        return data_length;
    }
}
