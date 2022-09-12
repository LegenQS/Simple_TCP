package UDP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataReceiver {
    private int length;
    private String file_path;

    public DataReceiver() {

    }

    public DataReceiver(String file) {
        file_path = file;
    }

    // write data to local path
    public void writeData(byte[] arr) {
        String file_path = "src/TCP_IP/java/test1.txt";
        File output = new File(file_path);
        FileOutputStream out_stream = null;
        try {
            out_stream = new FileOutputStream(output, true);
            out_stream.write(arr);
        } catch (IOException e) {
            throw new RuntimeException("Wrong output request");
        } finally {
            try {
                if (out_stream != null) out_stream.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

}
