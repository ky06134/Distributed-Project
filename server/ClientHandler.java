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

    private Lock lock = new ReentrantLock();
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final Condition condition = lock.newCondition();

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
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
            int count = 0;

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
                lock.lock();
                // System.out.println("main thread acquired lock");
                try {
                    // msgFromClient = br.readLine();
                    byte[] buffer = new byte[32];
                    int i = in.read(buffer);
                    msgFromClient = new String(buffer, 0, i);
                    System.out.println(
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!message from client: " + msgFromClient);
                } finally {
                    condition.signal();
                    lock.unlock();
                    // System.out.println("main thread released lock");
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
                                get(path + "/" + arr[1], socket);
                            } catch (IOException e) { // i didnt change anything else
                                e.printStackTrace();
                            }
                        });
                    } else {
                        get(path + "/" + arr[1], socket);
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                    } // if
                }

                // TESTING HERE TOO
                if (arr[0].equals("put")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        runNow(() -> {
                            // lock.lock();
                            System.out.println("worker thread aqcuired initial lock");
                            try {
                                // all this is doing is placing put() on another thread
                                // isListening = false;
                                put(path + "/" + arr[1]);
                                System.out.println("put finished");
                            } catch (IOException | InterruptedException e) { // i didnt change anything else
                                e.printStackTrace();
                            } finally {
                                lock.unlock();
                                System.out.println("worker finished");
                            }
                        });
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
                    final BufferedWriter finalBw = bw;
                    if (newThread) {
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                        runNow(() -> {
                            try {
                                // all this is doing is placing put() on another thread
                                // isListening = false;
                                finalBw.write(path);
                                finalBw.newLine();
                                finalBw.flush();
                                finalBw.write("myftp>");
                                finalBw.newLine();
                                finalBw.flush();
                            } catch (IOException e) { // i didnt change anything else
                                e.printStackTrace();
                            }
                        });
                    } else {
                        bw.write(path);
                        bw.newLine();
                        bw.flush();
                        bw.write("myftp>");
                        bw.newLine();
                        bw.flush();
                    } // if
                }

                if (arr[0].equals("ls")) {
                    bw.write(listDirectory(System.getProperty("user.dir")));
                    bw.newLine();
                    bw.flush();
                    // if (newThread) {
                    // synchronized (lock) {
                    // bw.write("myftp>");
                    // bw.newLine();
                    // }
                    // final BufferedWriter finalBw = bw;
                    // runNow(() -> {
                    // try {
                    // synchronized (lock) {
                    // finalBw.write(listDirectory(System.getProperty("user.dir")));
                    // finalBw.newLine();
                    // finalBw.write("myftp>");
                    // finalBw.newLine();
                    // finalBw.flush();
                    // }
                    // } catch (IOException e) { // i didnt change anything else
                    // e.printStackTrace();
                    // }
                    // });
                    // } else {
                    // bw.write(listDirectory(System.getProperty("user.dir")));
                    // bw.newLine();
                    // bw.flush();
                    // bw.write("myftp>");
                    // bw.newLine();
                    // bw.flush();
                    // } // if
                }

                if (arr[0].equals("quit")) {
                    bw.write("Closing connection");
                    bw.newLine();
                    bw.flush();
                    bw.close();
                    br.close();
                    socket.close();
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
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } // try
        } // try

    } // run

    // made changes to put for test
    private synchronized void put(String destination) throws IOException, InterruptedException {
        lock.lock();
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
            System.out.println(s);
            if (s.contains("$")) {
                // System.out.println(s);
                // release for main
                lock.unlock();
                // System.out.println("worker thread released lock");
                // ask for it back
                lock.lock();
                condition.await();
                // System.out.println("worker thread acquired locked");
            } else if (s.contains(delimiter)) {
                // int delimIndex = sb.indexOf(delimiter) - 1;
                out.write(buffer, 0, bytesRead - 1);
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
    }

    /*
     * given a filepath and socket send the file over TCP socket
     */
    private static void get(String filepath, Socket s) throws FileNotFoundException, IOException {

        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);
        OutputStream out = s.getOutputStream();

        // read and send in chunks
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
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
