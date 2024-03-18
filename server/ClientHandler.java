import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CountDownLatch;

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
    private final Socket tsocket;
    private final ServerSocket putServerSocket;
    private final ServerSocket getServerSocket;
    private final InputStream in;
    private final OutputStream out;
    private final InputStream tin;
    private final OutputStream tout;

    public ClientHandler(Socket n, Socket t, ServerSocket p, ServerSocket g) throws IOException {
        this.nsocket = n;
        this.tsocket = t;
        getServerSocket = g;
        putServerSocket = p;
        this.in = nsocket.getInputStream();
        this.out = nsocket.getOutputStream();
        this.tin = tsocket.getInputStream();
        this.tout = tsocket.getOutputStream();
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
                int n = arr.length;
                boolean newThread = false;
                if (arr[n - 1].equals("&")) {
                    newThread = true;
                }

                System.out.println("The command is " + msgFromClient);

                if (arr[0].equals("get")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        Integer id = ServerThreadPool.generateID();
                        bw.write(id.toString());
                        bw.newLine();
                        bw.flush();
                        GetWorker gw = new GetWorker(path + "\\" + arr[1], getServerSocket, id);
                        runNow(gw);
                    } else {
                        get1(path + "\\" + arr[1]);
                    } // if
                } // if

                if (arr[0].equals("put")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        Integer id = ServerThreadPool.generateID();
                        bw.write(id.toString());
                        bw.newLine();
                        bw.flush();
                        // System.out.println("before error");
                        PutWorker pw = new PutWorker(path + "\\" + arr[1], putServerSocket, id);
                        System.out.println("PutWorked");
                        runNow(pw);
                    } else {
                        put1(path + "\\" + arr[1]);
                        //System.out.println("WE OUT");
                    } // if
                } // if

                if (arr[0].equals("delete")) {
                    String path = System.getProperty("user.dir");
                    delete(path + "/" + arr[1]);

                }
                if (arr[0].equals("cd")) {
                    String path = System.getProperty("user.dir");

                    if (arr[1].equals("..")) {
                        System.setProperty("user.dir", new File(path).getParentFile().getAbsolutePath());
                    } else {
                        System.setProperty("user.dir", path + "\\" + arr[1]);
                    }
                    bw.write(System.getProperty("user.dir"));
                    bw.newLine();
                    bw.flush();
                }

                if (arr[0].equals("mkdir")) {
                    String path = System.getProperty("user.dir");
                    makeDirectory(path + "/" + arr[1]);

                }

                if (arr[0].equals("pwd")) {
                    String path = System.getProperty("user.dir");
                    bw.write(path);
                    bw.newLine();
                    bw.flush();
                }

                if (arr[0].equals("ls")) {
                    String temp = listDirectory(System.getProperty("user.dir"));
                    bw.write(temp);
                    bw.newLine();
                    bw.flush();
                }

                if (arr[0].equals("terminate")) {
                    ServerThreadPool.getThread(Integer.valueOf(arr[1])).terminate();
                    ServerThreadPool.remove(Integer.valueOf(arr[1]));
                }

                if (arr[0].equals("quit")) {
                    bw.write("Closing connection");
                    bw.newLine();
                    bw.flush();
                    bw.close();
                    br.close();
                } // if
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

    private static void delete(String filename) {
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
