import java.io.*;
import java.net.*;

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

    private final Socket socket;
    private static boolean isListening = true;
    private static String command = "";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    } // constructor

    public void run() {
        InputStreamReader reader = null;
        OutputStreamWriter writer = null;
        BufferedReader br = null;
        BufferedWriter bw = null;

        try {

            reader = new InputStreamReader(socket.getInputStream());
            writer = new OutputStreamWriter(socket.getOutputStream());
            br = new BufferedReader(reader);
            bw = new BufferedWriter(writer);

            while (true) {
                Thread.sleep(1000);
                bw.write("myftp>");
                bw.newLine();
                bw.flush();

                String msgFromClient = isListening ? br.readLine() : command;

                String arr[] = msgFromClient.split(" ");
                int n = arr.length;
                boolean newThread = false;
                if (arr[n - 1].equals("&")) {
                    newThread = true;
                }

                System.out.println("The command is " + msgFromClient);

                if (arr[0].equals("get")) {
                    String path = System.getProperty("user.dir");
                    // get(path + "/" + arr[1], socket);
                    runNow(() -> {
                        try {
                            // all this is doing is placing put() on another thread
                            get(path + "/" + arr[1], socket);
                        } catch (IOException e) { // i didnt change anything else
                            e.printStackTrace();
                        } // try
                          // runNow(new Worker())
                    });
                }

                // TESTING HERE TOO
                if (arr[0].equals("put")) {
                    String path = System.getProperty("user.dir");
                    if (newThread) {
                        setListening(false);
                        runNow(() -> {
                            try {
                                // all this is doing is placing put() on another thread
                                // isListening = false;
                                put(path + "/" + arr[1], socket);
                                setListening(true);
                            } catch (IOException e) { // i didnt change anything else
                                e.printStackTrace();
                            }
                        });
                    } else {
                        put(path + "/" + arr[1], socket);
                    } // if

                } // if

                if (arr[0].equals("delete")) {
                    String path = System.getProperty("user.dir");
                    // delete(path + "/" + arr[1]);
                    runNow(() -> {
                        delete(path + "/" + arr[1]);
                    });
                }
                if (arr[0].equals("cd")) {
                    String path = System.getProperty("user.dir");
                    runNow(() -> {
                        // all this is doing is placing put() on another thread
                        if (arr[1].equals("..")) {
                            System.setProperty("user.dir", new File(path).getParentFile().getAbsolutePath());
                        } else {
                            System.setProperty("user.dir", path + "/" + arr[1]);
                        }

                    });
                    bw.write(System.getProperty("user.dir"));
                    bw.newLine();
                    bw.flush();
                }

                if (arr[0].equals("mkdir")) {
                    String path = System.getProperty("user.dir");
                    // makeDirectory(path + "/" + arr[1]);
                    runNow(() -> {
                        makeDirectory(path + "/" + arr[1]);
                    });
                }

                if (arr[0].equals("pwd")) {
                    String path = System.getProperty("user.dir");
                    bw.write(path);
                    bw.newLine();
                    bw.flush();
                }

                if (arr[0].equals("ls")) {
                    bw.write(listDirectory(System.getProperty("user.dir")));
                    bw.newLine();
                    bw.flush();
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
    private synchronized static void put(String destination, Socket s) throws IOException {

        InputStream in = s.getInputStream();
        OutputStream out = new FileOutputStream(destination);
        StringBuilder sb = new StringBuilder();

        final String delimiter = "\0"; // Define a delimiter
        // final String delimiter2 = "|";
        // String command = "";

        // read and write to a file
        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytesRead));

            // if (sb.toString().contains(delimiter2) && sb.toString().contains(delimiter))
            // {
            // int delimIndex = sb.indexOf(delimiter) - 1;
            // out.write(buffer, 0, delimIndex);

            // } else if (sb.toString().contains(delimiter2)) {
            // int start = sb.indexOf(delimiter2);
            // out.write(buffer, 0, start - 1);
            // int end = sb.indexOf(delimiter2, start);
            // if (end == -1) {
            // command = sb.toString().substring(start, sb.length());
            // bytesRead = in.read(buffer);
            // sb.append(new String(buffer, 0, bytesRead));
            // start = sb.indexOf(delimiter2);
            // command += sb.toString().substring(0, start + 1);
            // if (sb.toString().contains(delimiter2) && sb.toString().contains(delimiter))
            // {
            // out.write(buffer, start + 1, bytesRead - 1);
            // break;
            // } else {
            // out.write(buffer, start + 1, bytesRead);
            // } //if
            // } else {
            // command = sb.toString().substring(start, end + 1);
            // out.write(buffer, end + 1, bytesRead);
            // }
            // }
            if (sb.toString().contains("|")) {
                // "|get file1.txt & "
                String cmd = sb.toString().trim();
                cmd = cmd.substring(1, cmd.length());
                System.out.println(cmd);
                setCommand(cmd);
            } else if (sb.toString().contains(delimiter)) {
                // int delimIndex = sb.indexOf(delimiter) - 1;
                out.write(buffer, 0, bytesRead - 1);
                break;
            } else {
                out.write(buffer, 0, bytesRead);
            }
            try {
                Thread.sleep(1); // this might simulate a larger file
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
        // try {
        // t.join(); // Wait for the thread to finish
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
    } // runNow

    public static void setListening(boolean b) {
        ClientHandler.isListening = b;
        System.out.println("b = " + b);
    }

    public static void setCommand(String s) {
        ClientHandler.command = s;
    } // setCommand

} // class
