import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ReadOnlyBufferException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PutWorker implements Worker, Runnable {

    String destination;
    Socket putSocket;
    InputStream in;
    boolean killswitch = false;
    Integer id;
    Lock lock = new ReentrantLock(true);

    public PutWorker(String destination, ServerSocket putServer, Integer id) throws IOException {
        this.destination = destination;
        this.putSocket = putServer.accept();
        this.in = putSocket.getInputStream();
        this.id = id;
        if (LockManager.get(this.destination) == null) {
            LockManager.put(this.destination, lock);
        } else {
            this.lock = LockManager.get(this.destination);
        }
        ServerThreadPool.put(this.id, this);
    }

    @Override
    public void run() {
        this.lock.lock();
        try {
            put(this.destination);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            LockManager.remove(this.destination);
        }
    }
    
    private synchronized void put(String destination) throws IOException {
        //System.out.println("made it to server put");
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
