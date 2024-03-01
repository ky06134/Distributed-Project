import java.io.*;
import java.net.*;
import java.util.*;

public class myftpserver {

    protected static String server_IP;
    private static Integer nport = 0; // normal port
    private static Integer tport = 0; // terminate port
    private static HashMap<Integer, Thread> threadPool = new HashMap<>();
    private static Integer threadID = 0;

    public static void main(String[] args) throws Exception {

        try {
            nport = Integer.valueOf(args[0]); // grab port from command line arg
            tport = Integer.valueOf(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Port");
            System.exit(0);
        } // catch

        try {
            InetAddress iAddress = InetAddress.getLocalHost();
            server_IP = iAddress.getHostAddress();
            System.out.println("Server IP address : " + server_IP);
        } catch (UnknownHostException e) {
        }

        // normal server on nport will stay on main thread
        ServerSocket n_server = null;
        try {
            n_server = new ServerSocket(nport);
            System.out.println("normal server ip: " + n_server.getInetAddress());
            System.out.println("normal server is now online and listening on port: " + nport);
        } catch (IOException e) {
            e.printStackTrace();
        } // try
          // start new thread for terminate server on tport
        ServerSocket t_server = null;
        try {
            t_server = new ServerSocket(tport);
            System.out.println("termination server ip: " + t_server.getInetAddress());
            System.out.println("terminaton server is now online and listening on port: " + tport);
        } catch (IOException e) {
            e.printStackTrace();
        } 

        while (true) {
            Socket n = n_server.accept(); // waits for connection from client
            System.out.println("Normal port connected");
            // Socket t = t_server.accept();
            // System.out.println("Terminate port connected");
            runNow(new ClientHandler(n));
        } // while

    } // main

    /**
     * NOTE: we may have to put runnow in its own class with Hashmap
     * threadPool as a class variable so all threads can have access
     * 
     * I am unsure if multiple threads accessing the same variable is
     * a problem or not :/
     * 
     * TIP: do some reseach into ConcurrentHashMap
     */
    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    }

} // myftpserver
