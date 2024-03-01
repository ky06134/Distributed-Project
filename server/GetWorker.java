import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class GetWorker implements Runnable {

    String filepath;
    Socket getSocket;
    OutputStream out;

    public GetWorker(String filepath, ServerSocket getServer) throws IOException {
        this.filepath = filepath;
        this.getSocket = getServer.accept();
        this.out = getSocket.getOutputStream();
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
                Thread.sleep(250); // this might simulate a larger file
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // while
        in.close();

    } // get
}
