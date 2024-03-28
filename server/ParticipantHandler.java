import java.io.*;
import java.net.*;
import java.util.ArrayList;

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
                    p.connect();
                    Coordinator.pSet.add(p);
                    p.setStatus("online"); 
                }
                if (arr[0].equals("deregister")) {
                    Coordinator.pSet.remove(p);
                }
                if (arr[0].equals("disconnect")) {
                    p.setStatus("offline");
                }
                if (arr[0].equals("reconnect")) {
                    p.setPort(Integer.parseInt(arr[1]));
                    p.connect();
                    p.setStatus("online");
                    ArrayList<String> history = p.getHistory(Coordinator.thresh);
                    for (String s: history) {
                        byte[] b = s.getBytes();
                        p.getOutputStream().write(b, 0, b.length);
                    } //for
                } //if
                if (arr[0].equals("msend")) {
                    msend(p.getId() + "> " + arr[1]);
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

    //"id>>helloworld"
    public synchronized void msend(String s) throws IOException {
        Long t = System.currentTimeMillis();
        byte[] msg = s.getBytes();
        for(Participant p : Coordinator.pSet) {
            if (p.getStatus().equals("online")) {
                p.getOutputStream().write(msg, 0, msg.length);
            } else { //offline
                p.addMsg(s, t);
            } //if
        } //for
        //write to log
        FileWriter fr = new FileWriter(Coordinator.msgLog, true);
        fr.write(s + t);
        fr.close();
    } //msend

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    } // runNow
} // class
