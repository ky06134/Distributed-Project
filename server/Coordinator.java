import java.io.*;
import java.net.*;
import java.util.*;

public class Coordinator {

    protected static String server_IP;
    private static Integer nport = 0; // normal port
    private static Long thresh; // threshold
    public static HashSet<Participant> pSet = new HashSet<>();

    public static void main(String[] args) throws Exception {

        String config = args[0];
        ArrayList<String> configSplit = getConfigDetails(config);
        nport = Integer.parseInt(configSplit.get(0));
        thresh = Long.parseLong(configSplit.get(1)) * 1000; //milliseconds
        InetAddress iAddress = InetAddress.getLocalHost();
        server_IP = iAddress.getHostAddress();
        System.out.println("Server IP address : " + server_IP);

        // normal server on nport will stay on main thread
        ServerSocket n_server = null;
        try {
            n_server = new ServerSocket(nport);
            // System.out.println("normal server ip: " + n_server.getInetAddress());
            System.out.println("normal server is now online and listening on port: " + nport);
        } catch (IOException e) {
            e.printStackTrace();
        } // try

        while (true) {
            Socket n = n_server.accept(); // waits for connection from client
            System.out.println("Normal port connected");
            runNow(new ParticipantHandler(n));
        } // while

    } // main

    private static ArrayList<String> getConfigDetails(String configFile) {
        ArrayList<String> configDetails = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String placeHolder = null;
            while ((placeHolder = br.readLine()) != null) {
                configDetails.add(placeHolder.trim());
            }
            if (br != null) {
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configDetails;
    }

  

    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    }

} // myftpserver
