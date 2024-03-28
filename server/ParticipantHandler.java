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
public class ParticipantHandler implements Runnable {

    private final Socket nsocket;
    private final InputStream in;
    private final OutputStream out;

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

    private static void delete(String filename) throws FileNotFoundException, IOException {
        File file = new File(filename);
        file.delete();
    } // delete

    private static void makeDirectory(String directoryName) {
        File file = new File(directoryName);
        file.mkdir();
    } // makeDirectory

    private static String listDirectory(String directory) {
        String res = "";
        File currentDirectory = new File(directory);
        File[] files = currentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                res += file.getName() + "\t";
            }
        } else {
            System.out.println("No files found.");
        }
        return res;
    }

    private void put1(String destination) throws IOException {
        OutputStream out = new FileOutputStream(destination);
        byte[] buffer = new byte[32];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            String s = new String(buffer);
            if (s.contains("\0")) {
                int index = s.indexOf("\0");
                out.write(buffer, 0, index);
                break;
            } else {
                out.write(buffer, 0, bytesRead);
            }
        } // while
        out.close();

    } // put

    private void get1(String filepath) throws FileNotFoundException, IOException {
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[32];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        } // while
        String delimiter = "\0";
        out.write(delimiter.getBytes());
        in.close();

    } // get

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    } // runNow
} // class
