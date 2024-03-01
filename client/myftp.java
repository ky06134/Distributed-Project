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
    private static final ReentrantLock syncGet = new ReentrantLock(true);
    private static final Condition condition = lock.newCondition();
    private static Socket client;
    private static InputStream in;
    private static OutputStream out;
    private static boolean isUploading = false;

    public static void main(String[] args) throws IOException, InterruptedException {

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
            if (isUploading) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } // if
              // System.out.println("MAIN THREAD ACQUIRED LOCK");
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
                // try {
                // condition.await();
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // } finally {
                // lock.unlock();
                // System.out.println("main released lock");
                // }
                // condition.signal();
                lock.unlock();
                // System.out.println("MAIN THREAD RELEASED LOCK");
            } // try

            String arr[] = cmd.split(" ");
            int n = arr.length;
            boolean newThread = false;
            if (arr[n - 1].equals("&")) {
                newThread = true;
            }

            if (arr[0].equals("get")) {
                if (newThread) {
                    myftp.isUploading = true;
                    final long currentThreadId = Thread.currentThread().getId();
                    final long tID = ClientThreadPool.getThreadId() + 1;
                    ClientThreadPool.runNow(() -> {
                        System.out.println("WORKER THREAD CREATED");
                        syncGet.lock();
                        try {
                            for (Map.Entry<Long, Pair<String, Thread>> entry : ClientThreadPool.getThreadPool()
                                    .entrySet()) {
                                System.out.println("Key: " + entry.getKey() + ", Value1: " + entry.getValue().getFirst()
                                        + ", Value2: " + entry.getValue().getSecond());
                            }
                            get(arr[1]); // all this is doing is placing put() on
                                         // another thread
                        } catch (IOException | InterruptedException e) { // i didnt change anything else
                            e.printStackTrace();
                        } finally {
                            ClientThreadPool.remove(currentThreadId + tID);
                            if (ClientThreadPool.getThreadPool().isEmpty()) {
                                myftp.isUploading = false;
                            }
                            syncGet.unlock();
                        }
                    }, cmd, currentThreadId);

                } else {
                    get(arr[1]);
                } // if
            } // if

            // LETS PUT IT TO THE TEST LOL
            if (arr[0].equals("put")) {
                if (newThread) {
                    myftp.isUploading = true;
                    final long currentThreadId = Thread.currentThread().getId();
                    final long tID = ClientThreadPool.getThreadId() + 1;
                    ClientThreadPool.runNow(() -> {
                        System.out.println("WORKER THREAD CREATED");
                        syncPut.lock();
                        try {
                            for (Map.Entry<Long, Pair<String, Thread>> entry : ClientThreadPool.getThreadPool()
                                    .entrySet()) {
                                System.out.println("Key: " + entry.getKey() + ", Value1: " + entry.getValue().getFirst()
                                        + ", Value2: " + entry.getValue().getSecond());
                            }
                            put(arr[1]); // all this is doing is placing put() on
                                         // another thread
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
                client.close();
                break;
            } // if
        } // while
          // lock.unlock();
    } // main

    // made changes to put for test
    private static void put(String filepath) throws FileNotFoundException, IOException {
        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);

        System.out.println("UPLOADING...");
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
          // byte[] nbuffer = new byte[32];
          // for (int i = 0; i < nbuffer.length - bytesRead - 1; i++) {
          // nbuffer[i] = '\0';
          // }
          // out.write(nbuffer, 0, nbuffer.length - bytesRead - 1);
        String delimiter = "\0";
        out.write(delimiter.getBytes());
        in.close();

        System.out.println("UPLOAD FINISHED");
    } // put

    private static void get(String destination) throws IOException, InterruptedException {
        OutputStream out = new FileOutputStream(destination);
        StringBuilder sb = new StringBuilder();

        final String delimiter = "\0"; // Define a delimiter
        // final String delimiter2 = "|";
        // String command = "";

        // read and write to a file
        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead;
        while ((bytesRead = myftp.in.read(buffer)) != -1) {
            String s = new String(buffer, 0, bytesRead);
            // System.out.println(s);
            if (s.contains("$")) {

                System.err.println("AWAITING FOR UPLOAD SIGNAL...");
                condition.await();
                System.out.println("UPLOAD RESUMED");

                // System.out.println("worker thread acquired locked");
            } else if (s.contains(delimiter)) {
                int delimIndex = sb.indexOf(delimiter) - 1;
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
    } // get

    // OutputStream out = new FileOutputStream(destination);
    // StringBuilder sb = new StringBuilder();

    // final String delimiter = "\0"; // Define a delimiter

    // // read and write to a file
    // byte[] buffer = new byte[32];
    // int bytesRead;
    // while ((bytesRead = myftp.in.read(buffer)) != -1) {
    // sb.append(new String(buffer, 0, bytesRead));

    // if (sb.toString().contains(delimiter)) {
    // out.write(buffer, 0, bytesRead - 1);
    // break;
    // } else {
    // out.write(buffer, 0, bytesRead);
    // }
    // try {
    // Thread.sleep(250); // this might simulate a larger file
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // } // while
    // out.flush();
    // out.close();

    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    }
} // class
