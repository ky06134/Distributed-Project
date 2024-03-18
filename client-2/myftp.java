import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

// Client class 
class myftp {

    private static Socket client;
    private static InputStream in;
    private static OutputStream out;
    private static InputStream tin;
    private static OutputStream tout;
    private static boolean isUploading = false;

    public static void main(String[] args) throws IOException, InterruptedException {

        Integer nport = 0;
        Integer tport = 0;
        String machineName = "";
        Socket nsocket;
        Socket tsocket;

        try {
            machineName = args[0];
            nport = Integer.valueOf(args[1]); // grab port from command line arg
            tport = Integer.valueOf(args[2]); // grab port from command line arg
        } catch (NumberFormatException e) {
            System.out.println("Bad arguments");
            System.exit(0);
        } // catch

        nsocket = new Socket(machineName, nport);
        tsocket = new Socket(machineName, tport);
        myftp.in = nsocket.getInputStream();
        myftp.out = nsocket.getOutputStream();
        myftp.tin = tsocket.getInputStream();
        myftp.tout = tsocket.getOutputStream();
        Scanner scanner = new Scanner(System.in);

        InputStreamReader reader = new InputStreamReader(myftp.in);
        OutputStreamWriter writer = new OutputStreamWriter(myftp.out);
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);

        while (true) {

            String serverMsg = br.readLine();
            System.out.print(serverMsg);

            String cmd = scanner.nextLine();
            String arr[] = cmd.split(" ");
            int n = arr.length;

            byte[] msg = cmd.getBytes();
            out.write(msg, 0, msg.length);
            boolean flag = false;
            if (arr[n - 1].equals("&")) {
                flag = true;
            }

            if (arr[0].equals("get")) {
                if (flag) {
                    String s = br.readLine();
                    System.out.println("Worker ID: " + s);
                    System.out.println(Integer.valueOf(s));
                    runNow(new ClientGetWorker(machineName, arr[1], Integer.valueOf(s)));
                } else {
                    get1(arr[1]);
                }

            } // if

            if (arr[0].equals("put")) {
                if (flag) {
                    String s = br.readLine();
                    System.out.println("Worker ID: " + s);
                    System.out.println(Integer.valueOf(s));
                    runNow(new ClientPutWorker(machineName, arr[1], Integer.valueOf(s)));
                } else {
                    put1(arr[1]);
                    //System.out.println("WE OUT");
                }
            } // if

            if (arr[0].equals("delete")) {

            }

            if (arr[0].equals("ls")) {
                String s = br.readLine();
                System.out.println(s);

            }

            if (arr[0].equals("cd")) {
                String s = br.readLine();
                System.out.println(s);

            }

            if (arr[0].equals("mkdir")) {

            }

            if (arr[0].equals("pwd")) {
                String s = br.readLine();
                System.out.println(s);
            }

            if (arr[0].equals("terminate")) {
                tout.write(arr[1].getBytes());
                ClientThreadPool.getThread(Integer.valueOf(arr[1])).terminate();
                ClientThreadPool.remove(Integer.valueOf(arr[1]));
            }

            if (arr[0].equals("quit")) {
                String s = br.readLine();
                System.out.println(s);

                nsocket.close();
                tsocket.close();
                break;
            } // if
        } // while
    } // main

    private static void get1(String destination) throws IOException {
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

    private static void put1(String filepath) throws FileNotFoundException, IOException {
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

    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    }
} // class
