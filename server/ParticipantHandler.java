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
                bw.write(prompt);
                bw.newLine();
                bw.flush();

                String msgFromClient = br.readLine();
                String arr[] = msgFromClient.split(" ");

                System.out.println("The command is " + msgFromClient);

                if (arr[0].equals("register")) {
                    p.setPort(Integer.parseInt(arr[1]));
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

    public synchronized void msend(String s) throws IOException {
        byte[] msg = s.getBytes();
        for(Participant p : Coordinator.pSet) {
            if (p.getStatus().equals("online")) {
                p.getOutStream().write(msg, 0, msg.length);
            } else { //offline

            }
        }
    } //

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    } // runNow
} // class
