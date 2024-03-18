import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientPutWorker implements ClientWorker, Runnable {

    Socket psocket; 
    OutputStream out;
    String filePath;
    Integer id;
    boolean killswitch = false;

    public ClientPutWorker(String machineName, String filepath, Integer id) throws IOException {
        this.psocket = new Socket(machineName, 8080);
        this.out = psocket.getOutputStream();
        this.filePath = filepath;
        this.id = id;
        ClientThreadPool.put(this.id, this);
    } 

    @Override
    public void run() {
        try {
            put(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // made changes to put for test
    private void put(String filepath) throws FileNotFoundException, IOException {
        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);

        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead; // length command get file1.txt &
        while ((bytesRead = in.read(buffer)) != -1) {
            this.out.write(buffer, 0, bytesRead);
            try {
                if (killswitch) {
                    Thread.currentThread().interrupt();
                }   
                Thread.sleep(250); // this might simulate a larger file
            } catch (InterruptedException e) {
                break;
            }
        } // while
        in.close();
    } // put

    @Override
    public void terminate() {
        this.killswitch = true;
    }

    public Integer getId() {
        return this.id;
    }
}
