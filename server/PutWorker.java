import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PutWorker implements Worker, Runnable {

    String destination;
    Socket putSocket;
    InputStream in;
    boolean killswitch;
    Integer id;

    public PutWorker(String destination, ServerSocket putServer) throws IOException {
        this.destination = destination;
        this.putSocket = putServer.accept();
        this.in = putSocket.getInputStream();
        this.id = ServerThreadPool.generateID();
        ServerThreadPool.put(this.id, this);
    }

    @Override
    public void run() {
        try {
            put(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void put(String destination) throws IOException {
        File file = new File(destination);
        OutputStream out = new FileOutputStream(destination);
        byte[] buffer = new byte[32]; 
        int bytesRead;
        while ((bytesRead = this.in.read(buffer)) != -1) {  
            out.write(buffer, 0, bytesRead);
            try {
                if (killswitch) {
                    Thread.currentThread().interrupt();
                }               
                Thread.sleep(250); // this might simulate a larger file
            } catch (InterruptedException e) {
                out.flush();
                out.close();
                file.delete();
                break;
            }
        } // while    
        out.flush();
        out.close();
    }

    public void terminate() {
        this.killswitch = true;
    }

    public Integer getId() {
        return this.id;
    }
    
}
