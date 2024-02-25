import java.io.*; 
import java.net.*; 

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    } //constructor

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
                String arr[] = msgFromClient.split(" ");

                System.out.println("The command is " + msgFromClient);

                if (arr[0].equals("get")) {
                    String path = System.getProperty("user.dir");
                    get(path + "\\" + arr[1], socket);
                }

                if (arr[0].equals("put")) {
                    String path = System.getProperty("user.dir");
                    put(path + "\\" + arr[1], socket);
                }

                if (arr[0].equals("delete")) {
                    String path = System.getProperty("user.dir");
                    delete(path + "\\" + arr[1]);
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
                    makeDirectory(path + "\\" + arr[1]);
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
            } //while

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
            } //try
        } //try

    } //run

    private static void put(String destination, Socket s) throws IOException {

        InputStream in = s.getInputStream();
        OutputStream out = new FileOutputStream(destination);
        StringBuilder sb = new StringBuilder();

        final String delimiter = "\0"; // Define a delimiter

        // read and write to a file
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytesRead));

            if (sb.toString().contains(delimiter)) {
                out.write(buffer, 0, bytesRead - 1);
                break;
            } else {
                out.write(buffer, 0, bytesRead);
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
        //s.shutdownOutput();

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
} //class
