import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Client class 
class myftp {

    private static final ReentrantLock lock = new ReentrantLock(true);
    private static final ReentrantLock syncPut = new ReentrantLock(true);
    private static final Condition condition = lock.newCondition();
    private static Socket nsocket;
    private static Socket tsocket;
    private static InputStream in;
    private static OutputStream out;
    // private static InputStream tin;
    // private static OutputStream tout;
    private static boolean isUploading = false;

    public static void main(String[] args) throws IOException {

        Integer nport = 0;
        Integer tport = 0;
        String machineName = "";

        try {
            machineName = args[0];
            nport = Integer.valueOf(args[1]); // grab nport from command line arg
            tport = Integer.valueOf(args[2]); // grab tport
        } catch (NumberFormatException e) {
            System.out.println("Bad arguments");
            System.exit(0);
        } // catch

        nsocket = new Socket(machineName, nport);
        //tsocket = new Socket(machineName, tport);
        myftp.in = nsocket.getInputStream();
        myftp.out = nsocket.getOutputStream();
        // myftp.tin = tsocket.getInputStream();
        // myftp.tout = tsocket.getOutputStream();
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
            if (isUploading) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } //if
            try {
                if (isUploading) {
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
            } finally {
                lock.unlock();
            } // try

            String arr[] = cmd.split(" ");
            int n = arr.length;
            boolean newThread = false;
            if (arr[n - 1].equals("&")) {
                newThread = true;
            }

            if (arr[0].equals("get")) {
                get(arr[1], nsocket);

            } // if

            if (arr[0].equals("put")) {
                if (newThread) {
                    myftp.isUploading = true;
                    final long currentThreadId = Thread.currentThread().getId();
                    final long tID = ClientThreadPool.getThreadId() + 1;
                    ClientThreadPool.runNow(() -> {
                        System.out.println("WORKER THREAD CREATED");
                        syncPut.lock();
                        try {
                            for (Map.Entry<Long, Pair<String, Thread>> entry : ClientThreadPool.getThreadPool().entrySet()) {
                                System.out.println("Key: " + entry.getKey() + ", Value1: " + entry.getValue().getFirst()
                                        + ", Value2: " + entry.getValue().getSecond());
                            }
                            put(arr[1]); 
                        } catch (IOException e) { // i didnt change anything else
                            e.printStackTrace();
                        } finally {
                            ClientThreadPool.remove(currentThreadId + tID);
                            if (ClientThreadPool.getThreadPool().isEmpty()) {
                                myftp.isUploading = false;
                            }
                            syncPut.unlock();
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
                nsocket.close();
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

        System.out.println("UPLOADING...");
        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead; // length command get file1.txt &
        while ((bytesRead = in.read(buffer)) != -1) {
            lock.lock();
            try {
                if (bytesRead != 32) {
                    for (int i = bytesRead; i < 32; i++) {
                        buffer[i] = '\0';
                    } //for
                }
                myftp.out.write(buffer, 0, 32);
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
        in.close();

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
        out.close();
    } // get

    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    }
} // class
