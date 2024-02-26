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

                String msgFromClient = br.readLine();
                String stringWithoutAmpersands = msgFromClient.replaceAll(" &", "");
                String arr[] = stringWithoutAmpersands.split(" ");
                // for (String command : arr) {
                // System.out.println(command);
                // }

                System.out.println("The command is " + msgFromClient);

                for (int i = 0; i < arr.length; i++) {
                    int value = i + 1;
                    if (arr[i].equals("get")) {
                        String path = System.getProperty("user.dir");
                        get(path + "/" + arr[value], socket);
                        // runNow(() -> {
                        // try {
                        // // all this is doing is placing put() on another thread
                        // get(path + "/" + arr[value], socket);
                        // } catch (IOException e) { // i didnt change anything else
                        // e.printStackTrace();
                        // } // try
                        // });
                    }

                    // TESTING HERE TOO
                    if (arr[i].equals("put")) {
                        String path = System.getProperty("user.dir");
                        put(path + "/" + arr[value], socket);
                        // runNow(() -> {
                        // try {
                        // // all this is doing is placing put() on another thread
                        // put(path + "/" + arr[value], socket);
                        // } catch (IOException e) { // i didnt change anything else
                        // e.printStackTrace();
                        // } // try
                        // });
                    } // if

                    if (arr[i].equals("delete")) {
                        String path = System.getProperty("user.dir");
                        delete(path + "/" + arr[value]);
                        // runNow(() -> {
                        // delete(path + "/" + arr[value]);
                        // });
                    }
                    if (arr[i].equals("cd")) {
                        String path = System.getProperty("user.dir");
                        if (arr[value].equals("..")) {
                            System.setProperty("user.dir", new File(path).getParentFile().getAbsolutePath());
                        } else {
                            System.setProperty("user.dir", path + "/" + arr[value]);
                        }
                        bw.write(System.getProperty("user.dir"));
                        bw.newLine();
                        bw.flush();
                    }

                    if (arr[i].equals("mkdir")) {
                        String path = System.getProperty("user.dir");
                        makeDirectory(path + "/" + arr[value]);
                        // runNow(() -> {
                        // makeDirectory(path + "/" + arr[value]);
                        // });
                    }

                    if (arr[i].equals("pwd")) {
                        String path = System.getProperty("user.dir");
                        bw.write(path);
                        bw.newLine();
                        bw.flush();
                    }

                    if (arr[i].equals("ls")) {
                        bw.write(listDirectory(System.getProperty("user.dir")));
                        bw.newLine();
                        bw.flush();
                    }

                    if (arr[i].equals("quit")) {
                        bw.write("Closing connection");
                        bw.newLine();
                        bw.flush();
                        bw.close();
                        br.close();
                        socket.close();
                    } // if
                } // for
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
    private static void put(String destination, Socket s) throws IOException {

        InputStream in = s.getInputStream();
        OutputStream out = new FileOutputStream(destination);
        StringBuilder sb = new StringBuilder();

        final String delimiter = "\0"; // Define a delimiter

        // read and write to a file
        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytesRead));

            if (sb.toString().contains(delimiter)) {
                out.write(buffer, 0, bytesRead - 1);
                break;
            } else {
                out.write(buffer, 0, bytesRead);
            }
            try {
                Thread.sleep(1000); // this might simulate a larger file
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
