import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PutWorker implements Runnable {

    String destination;
    Socket putSocket;
    InputStream in;

    public PutWorker(String destination, ServerSocket putServer) throws IOException {
        this.destination = destination;
        this.putSocket = putServer.accept();
        this.in = putSocket.getInputStream();
    }

    @Override
    public void run() {
        System.out.println("inside run");
        try {
            put(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void put(String destination) throws IOException {
        OutputStream out = new FileOutputStream(destination);
        byte[] buffer = new byte[32]; 
        int bytesRead;
        while ((bytesRead = this.in.read(buffer)) != -1) {  
            out.write(buffer, 0, bytesRead);
            try {
                Thread.sleep(250); // this might simulate a larger file
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // while
        out.flush();
        out.close();

    }
    
}
