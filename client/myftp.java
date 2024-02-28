import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Client class 
class myftp {

    private static final Lock lock = new ReentrantLock();
    private static final Condition condition = lock.newCondition();
    private static Socket client;
    private static InputStream in;
    private static OutputStream out;
    private static boolean isUploading = false;

    public static void main(String[] args) throws IOException {

        Integer port = 0;
        String machineName = "";

        try {
            machineName = args[0];
            port = Integer.valueOf(args[1]); // grab port from command line arg
        } catch (NumberFormatException e) {
            System.out.println("Bad arguments");
            System.exit(0);
        } // catch

        client = new Socket(machineName, port);
        myftp.in = client.getInputStream();
        myftp.out = client.getOutputStream();
        Scanner scanner = new Scanner(System.in);

        InputStreamReader reader = new InputStreamReader(myftp.in);
        OutputStreamWriter writer = new OutputStreamWriter(myftp.out);
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);

        while (true) {

            String serverMsg = br.readLine();
            System.out.print(serverMsg);

            String cmd = scanner.nextLine();
            lock.lock();
            System.out.println("main acquire lock");
            try {
                if (isUploading) {
                    // bw.write("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                    String flag = "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
                    byte[] msg = flag.getBytes();
                    out.write(msg, 0, msg.length);
                }
                int lenAdd = 32 - cmd.length();
                for (int i = 0; i < lenAdd; i++) {
                    cmd += " ";
                }
                byte[] msg2 = cmd.getBytes();
                out.write(msg2, 0, msg2.length);
                // bw.write(cmd);
                // bw.newLine();
                // bw.flush();
            } finally {
                condition.signal();
                lock.unlock();
                System.out.println("main released lock");
            } // try

            String arr[] = cmd.split(" ");
            int n = arr.length;
            boolean newThread = false;
            if (arr[n - 1].equals("&")) {
                newThread = true;
            }

            if (arr[0].equals("get")) {
                get(arr[1], client);
                // runNow(() -> {
                // try {
                // get(arr[1], client); // all this is doing is placing put() on another
                // thread
                // } catch (IOException e) { // i didnt change anything else
                // e.printStackTrace();
                // } // try
                // });
            } // if

            // LETS PUT IT TO THE TEST LOL
            if (arr[0].equals("put")) {
                if (newThread) {
                    myftp.isUploading = true;
                    final long currentThreadId = Thread.currentThread().threadId();
                    final long tID = ThreadPool.getThreadId() + 1;
                    ThreadPool.runNow(() -> {
                        try {
                            for (Map.Entry<Long, Pair<String, Thread>> entry : ThreadPool.getThreadPool().entrySet()) {
                                System.out.println("Key: " + entry.getKey() + ", Value1: " + entry.getValue().getFirst()
                                        + ", Value2: " + entry.getValue().getSecond());
                            }
                            put(arr[1]); // all this is doing is placing put() on
                                         // another thread
                        } catch (IOException e) { // i didnt change anything else
                            e.printStackTrace();
                        } finally {
                            ThreadPool.remove(currentThreadId + tID);
                            if (ThreadPool.getThreadPool().isEmpty()) {
                                myftp.isUploading = false;
                                System.out.println("..,.,.,...............................................");
                            }
                        }
                    }, cmd, currentThreadId);

                } else {
                    put(arr[1]);
                } // if

            } // if

            if (arr[0].equals("delete")) {
                runNow(() -> {
                    // try {
                    // } catch (IOException e) { // i didnt change anything else
                    // e.printStackTrace();
                    // } // try
                });
            }

            if (arr[0].equals("ls")) {
                String s = br.readLine();
                System.out.println(s);
                // runNow(() -> {
                // System.out.println(s);
                // });
            }

            if (arr[0].equals("cd")) {
                String s = br.readLine();
                System.out.println(s);
                // runNow(() -> {
                // System.out.println(s);
                // });
            }

            if (arr[0].equals("mkdir")) {

            }

            if (arr[0].equals("pwd")) {
                String s = br.readLine();
                System.out.println(s);
                // runNow(() -> {
                // System.out.println(s);
                // });
            }

            if (arr[0].equals("quit")) {
                String s = br.readLine();
                System.out.println(s);
                // runNow(() -> {
                // System.out.println(s);
                // });
                client.close();
                break;
            } // if
        } // while
          // lock.unlock();
    } // main

    // made changes to put for test
    private static synchronized void put(String filepath) throws FileNotFoundException, IOException {

        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);

        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead; // length command get file1.txt &
        while ((bytesRead = in.read(buffer)) != -1) {
            lock.lock();
            try {
                myftp.out.write(buffer, 0, bytesRead);
            } finally {
                condition.signal();
                lock.unlock();
            }
            // buffer = command
            // out.write(buffer, 0, 32);
            try {
                Thread.sleep(250); // this might simulate a larger file
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // while
        String delimiter = "\0";
        out.write(delimiter.getBytes());

    } // put

    private static void get(String destination, Socket s) throws IOException {

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
    } // get

    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    }

} // class
