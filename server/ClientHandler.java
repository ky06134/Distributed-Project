import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;

/**
 * Some burning thoughts...
 * Here is an example of a typical progam life cycle:
 * Client runs the command $put file.txt & (this runs on another thread)
 * Server sees & and creates another thread for put
 * Client runs put again on a different file
 * but WAIT isn't it on the same output stream???
 * how does the server know what output to take from?
 * PROBLEM: there are 2 threads on client and server side
 * how do we correspond our output/input streams to the correct
 * threads
 * I'm just gonna place put on different threads to see what happens :P
 * it broke..
 */
public class ClientHandler implements Runnable {

    private final Socket nsocket;
    private final InputStream in;
    private final OutputStream out;

    public ClientHandler(Socket n) throws IOException {
        this.nsocket = n;
        this.in = nsocket.getInputStream();
        this.out = nsocket.getOutputStream();
    } // constructor

    public void run() {
        InputStreamReader reader = null;
        OutputStreamWriter writer = null;
        BufferedReader br = null;
        BufferedWriter bw = null;

        try {

            reader = new InputStreamReader(this.in);
            writer = new OutputStreamWriter(this.out);
            br = new BufferedReader(reader);
            bw = new BufferedWriter(writer);

            while (true) {

                String prompt = "myftp>\n";
                byte[] msg = prompt.getBytes();
                out.write(msg, 0, msg.length);

                String msgFromClient;

                byte[] buffer = new byte[32];
                int i = in.read(buffer);
                msgFromClient = new String(buffer, 0, i);
                System.out.println(
                        "!!!message from client: " + msgFromClient);

                String arr[] = msgFromClient.split(" ");

                System.out.println("The command is " + msgFromClient);

                if (arr[0].equals("register")) {
                    // register = true;
                }
                if (arr[0].equals("deregister")) {
                    // register = false;
                }
                if (arr[0].equals("disconnect")) {
                    // online = false;
                }
                if (arr[0].equals("reconnect")) {
                    // online = true;
                }
                if (arr[0].equals("msend")) {

                }

            } // while

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            } // try
        } // try

    } // run

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    } // runNow
} // class
