import java.io.*;
import java.net.*;
import java.util.ArrayList;
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

    private static ServerSocket server;
    static Socket nsocket = null;
    public static ArrayList<ParticipantStats> participants = new ArrayList<>();
    public static int incomingMessagePort;
    public static int td;

    public ClientHandler(ArrayList<ParticipantStats> participants, int td, int incomingMessagePort)
            throws IOException {
        this.incomingMessagePort = incomingMessagePort;
        this.participants = participants;
        this.td = td;
        server = new ServerSocket(incomingMessagePort);
    }

    public void run() {
        try {
            while (true) {
                System.out.println("Started");
                nsocket = server.accept();
                System.out.println("Participant connected");
                ObjectInputStream inputStream = new ObjectInputStream(nsocket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(nsocket.getOutputStream());
                String logs = (String) inputStream.readObject();
                String[] arr = logs.split(" ");
                System.out.println(arr[0]);

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
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    } // run

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    } // runNow
} // class
