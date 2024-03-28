import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;

public class ParticipantHandler implements Runnable {

    private final Socket nsocket;
    private final InputStream in;
    private final OutputStream out;
    private Participant p;

    public ParticipantHandler(Socket n) throws IOException {
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

            String id = br.readLine();
            this.p = new Participant(Integer.parseInt(id), this.nsocket.getInetAddress(), null);

            while (true) {

                String prompt = "server>\n";
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
                    register();
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

    public void register() {
        Coordinator.pSet.add(p);
    } //register 

    public void deregister() {

    } //deregister

    public void reconnect() {

    } //reconnect

    public void disconnect() {

    } //disconnect

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    } // runNow
} // class
