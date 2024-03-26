import java.io.*;
import java.net.*;
import java.util.ArrayList;
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

    public static boolean online = false;
    public static boolean registered = false;

    public static void main(String[] args) throws IOException, InterruptedException {

        String configFile = args[0];
        ArrayList<String> configDetails = getConfigDetails(configFile);
        int iD = Integer.parseInt(configDetails.get(0));
        String logFileName = configDetails.get(1);
        String iPAndPort = configDetails.get(2);
        String iPAndPortSplit[] = iPAndPort.split(" ");
        String machineName = iPAndPortSplit[0];
        Integer nport = Integer.parseInt(iPAndPortSplit[1]);
        Socket nsocket;

        nsocket = new Socket(machineName, nport);
        myftp.in = nsocket.getInputStream();
        myftp.out = nsocket.getOutputStream();
        Scanner scanner = new Scanner(System.in);

        // InputStreamReader reader = new InputStreamReader(myftp.in);
        // OutputStreamWriter writer = new OutputStreamWriter(myftp.out);
        // BufferedReader br = new BufferedReader(reader);
        // BufferedWriter bw = new BufferedWriter(writer);

        boolean placeHolder = true;
        while (true) {
            String userCommand = scanner.nextLine();
            placeHolder = checkCommand(userCommand);
        } // while
    } // main

    private static boolean checkCommand(String s) {
        return true; // all commands here
    }

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
} // class
