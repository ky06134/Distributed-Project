import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientGetWorker implements Runnable {

    Socket gsocket; 
    InputStream in;
    String destination;

    public ClientGetWorker(String machineName, String destination) throws IOException {
        this.gsocket = new Socket(machineName, 8081);
        this.in = gsocket.getInputStream();
        this.destination = destination;
    } 

    @Override
    public void run() {
        try {
            get(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void get(String destination) throws IOException {
        
        OutputStream out = new FileOutputStream(destination);

        // read and write to a file
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
