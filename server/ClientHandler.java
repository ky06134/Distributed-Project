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

    private ReentrantLock lock = new ReentrantLock(true);
    private ReentrantLock syncPut = new ReentrantLock(true);
    private final Socket nsocket;
    private final Socket tsocket;
    private final InputStream in;
    private final OutputStream out;
    private final InputStream tin;
    private final OutputStream tout;
    private final Condition resumeUpload = lock.newCondition();
    private boolean flag = false;

    public ClientHandler(Socket n, Socket t) throws IOException {
        this.nsocket = n;
        this.tsocket = t;
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
                Thread.sleep(1000);
                // send get/put id back to the client
                String prompt = "myftp>\n";
                byte[] msg = prompt.getBytes();
                out.write(msg, 0, msg.length);
                // bw.write("myftp>");
                // bw.newLine();
                // bw.flush();

                String msgFromClient;
                //ServerThreadPool.purge();
                //System.out.println("MAIN WAITING FOR LOCK...");
                lock.lock();
                //System.out.println("MAIN THREAD ACQUIRED LOCK");

                while (flag) {
                    //System.out.println("UPLOAD FINISHED: ALLOW WORKER TO LOCK");
                    lock.unlock(); 
                    Thread.sleep(100);
                    lock.lock();
                    //System.out.println("MAIN THREAD ACQUIRED LOCK");
                }
                try {
                    // msgFromClient = br.readLine();
                    byte[] buffer = new byte[32];
                    int i = in.read(buffer);
                    msgFromClient = new String(buffer, 0, i);
                    System.out.println(
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!message from client: " + msgFromClient);
                } finally {
                    resumeUpload.signal();
                    lock.unlock();
                    //System.out.println("MAIN THREAD RELEASED LOCK");
                } // try

                // if (msgFromClient.contains("$")) {
                // int len = msgFromClient.length();
                // msgFromClient = msgFromClient.substring(1, len);
                // } //if

                String arr[] = msgFromClient.split(" ");
                int n = arr.length;
                boolean newThread = false;
                if (arr[n - 1].equals("&")) {
                    newThread = true;
                }

                // System.out.println("The command is " + msgFromClient);

                if (arr[0].equals("get")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                        runNow(() -> {
                            try {
                                // all this is doing is placing put() on another thread
                                // isListening = false;
                                get(path + "/" + arr[1]);
                            } catch (IOException e) { // i didnt change anything else
                                e.printStackTrace();
                            }
                        });
                    } else {
                        get(path + "/" + arr[1]);
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                    } // if
                }

                // TESTING HERE TOO
                if (arr[0].equals("put")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        //create thread id and add to hashmap, send id to client through tport
                        Integer threadID = ServerThreadPool.generateID();
                        String temp = "WORKER ID:  " + threadID.toString() + "\n";
                        msg = temp.getBytes();
                        out.write(msg, 0, msg.length);
                        ServerThreadPool.runNow(() -> { 
                            syncPut.lock();                                                                           
                            try {
                                put(path + "/" + arr[1]);
                                System.out.println("put finished");
                            } catch (IOException | InterruptedException e) { // i didnt change anything else
                                e.printStackTrace();
                            } finally {
                                this.flag = true;
                                System.out.println("NEXT PUT REQUEST SIGNAL");
                                lock.unlock();
                                syncPut.unlock();
                                System.out.println("WORKER RELEASED LOCK");
                            }
                        }, arr[0], threadID);
                    } else {
                        put(path + "/" + arr[1]);
                    } // if
                } // if

                if (arr[0].equals("delete")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                        runNow(() -> {
                            delete(path + "/" + arr[1]);
                        });
                    } else {
                        delete(path + "/" + arr[1]);
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                    } // if
                }
                if (arr[0].equals("cd")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                        runNow(() -> {
                            if (arr[1].equals("..")) {
                                System.setProperty("user.dir", new File(path).getParentFile().getAbsolutePath());
                            } else {
                                System.setProperty("user.dir", path + "/" + arr[1]);
                            }
                        });
                    } else {
                        if (arr[1].equals("..")) {
                            System.setProperty("user.dir", new File(path).getParentFile().getAbsolutePath());
                        } else {
                            System.setProperty("user.dir", path + "/" + arr[1]);
                        }
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                    } // if
                    bw.write(System.getProperty("user.dir"));
                    bw.newLine();
                    bw.flush();
                }

                if (arr[0].equals("mkdir")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                        runNow(() -> {
                            makeDirectory(path + "/" + arr[1]);
                        });
                    } else {
                        makeDirectory(path + "/" + arr[1]);
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                    } // if
                }

                if (arr[0].equals("pwd")) {
                    String path = System.getProperty("user.dir");
                    path += "\n";
                    msg = path.getBytes();
                    out.write(msg, 0, msg.length);
                }

                if (arr[0].equals("ls")) {
                    String temp = listDirectory(System.getProperty("user.dir")) + "\n";
                    msg = temp.getBytes();
                    out.write(msg, 0, msg.length);
                    
                }

                /**lets play a game of what if
                     * 1. what if terminate in queue? easy. remove from hashmap, no cleanup
                     * 2. what if terminate while running (while holding lock)? 
                     *      - must give up lock to next worker 
                     *      - must clean buffer
                     *      - must cleant the stream 
                     */
                if (arr[0].equals("terminate")) {
                    byte[] buffer = new byte[32];
                    int bytesRead = this.tin.read(buffer);
                    String s = new String(buffer, 0, bytesRead);
                    Integer i = Integer.parseInt(s);
                    Thread t = ServerThreadPool.getThread(i);
                    System.out.println(t.getState());
                    if (t.getState() == Thread.State.BLOCKED) {
                        
                    } else if (t.getState() == Thread.State.RUNNABLE) {
                        t.interrupt();
                    } //if
                    String temp = "WORKER " + s + " TERMINATED\n";
                    msg = temp.getBytes();
                    out.write(msg, 0, msg.length);
                    

                }

                if (arr[0].equals("quit")) {
                    bw.write("Closing connection");
                    bw.newLine();
                    bw.flush();
                    bw.close();
                    br.close();
                    nsocket.close();
                } // if
            } // while

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                br.close();
                nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } // try
        } // try

    } // run

    // made changes to put for test
    private void put(String destination) throws IOException, InterruptedException {
        System.out.println("WORKER THREAD WAITING FOR LOCK...");
        lock.lock();
        System.out.println("WORKER ACQUIRED LOCK");
        this.flag = false; 
        
        OutputStream out = new FileOutputStream(destination);
        StringBuilder sb = new StringBuilder();

        final String delimiter = "\0"; // Define a delimiter
        // final String delimiter2 = "|";
        // String command = "";

        // read and write to a file
        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead;
        while ((bytesRead = this.in.read(buffer)) != -1) {
            String s = new String(buffer, 0, bytesRead);
            //System.out.println(s);
            if (Thread.interrupted()) {
                System.out.println("WE OUT boooooooooooooooooooooy");
                break;
            } //if
            if (s.contains("$")) {

                System.err.println("AWAITING FOR UPLOAD SIGNAL...");
                resumeUpload.await();
                System.out.println("UPLOAD RESUMED");

                // System.out.println("worker thread acquired locked");
            } else if (s.contains(delimiter)) {
                int delimIndex = s.indexOf(delimiter);
                out.write(buffer, 0, delimIndex);
                break;
            } else {
                out.write(buffer, 0, bytesRead);
            }
            try {
                Thread.sleep(250); // this might simulate a larger file
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // while
        out.flush();
        out.close();
        //nextPutRequest.signal();
        
    }

    /*
     * given a filepath and socket send the file over TCP socket
     */
    private void get(String filepath) throws FileNotFoundException, IOException {

        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);

        // read and send in chunks
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            this.out.write(buffer, 0, bytesRead);
        } // while
        String delimiter = "\0";
        out.write(delimiter.getBytes());
        out.flush();
        // s.shutdownOutput();

    } // get

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

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    } // runNow
} // class
