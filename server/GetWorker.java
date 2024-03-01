import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class GetWorker implements Worker, Runnable {

    String filepath;
    Socket getSocket;
    OutputStream out;
    boolean killswitch = false;
    Integer id;

    public GetWorker(String filepath, ServerSocket getServer, Integer id) throws IOException {
        this.filepath = filepath;
        this.getSocket = getServer.accept();
        this.out = getSocket.getOutputStream();
        this.id = id;
        ServerThreadPool.put(this.id, this);
    } 

    @Override
    public void run() {
        try {
            get(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void get(String filepath) throws FileNotFoundException, IOException {
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[32];
        int bytesRead;
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

    } // get

    public void terminate() {
        this.killswitch = true;
    }

    public Integer getId() {
        return this.id;
    }
}
