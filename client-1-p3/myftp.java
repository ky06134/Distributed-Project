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
    public static boolean register = false;

    public static void main(String[] args) throws IOException, InterruptedException {

        String config = args[0];
        ArrayList<String> configSplit = getConfigDetails(config);
        int iD = Integer.parseInt(configSplit.get(0));
        String logFileName = configSplit.get(1);
        String iPAndPort = configSplit.get(2);
        String iPAndPortSplit[] = iPAndPort.split(" ");
        String machineName = iPAndPortSplit[0];
        Integer nport = Integer.parseInt(iPAndPortSplit[1]);
        Socket nsocket;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        int BPort = 0;
        MulticastThread threadB = null;

        // myftp.in = nsocket.getInputStream();
        // myftp.out = nsocket.getOutputStream();

        Scanner scanner = new Scanner(System.in);
        String userCommand = "";

        // InputStreamReader reader = new InputStreamReader(myftp.in);
        // OutputStreamWriter writer = new OutputStreamWriter(myftp.out);
        // BufferedReader br = new BufferedReader(reader);
        // BufferedWriter bw = new BufferedWriter(writer);

        while (!userCommand.equals("quit")) {
            userCommand = scanner.nextLine();
            String arr[] = userCommand.split(" ");

            if (arr[0].equals("register")) {
                nsocket = new Socket(machineName, nport);
                outputStream = new ObjectOutputStream(nsocket.getOutputStream());
                inputStream = new ObjectInputStream(nsocket.getInputStream());
                BPort = Integer.parseInt(arr[1]);
                threadB = new MulticastThread(machineName, BPort, logFileName);
                threadB.start();
                register = true;
                outputStream.writeObject(arr[0] + " " + arr[1] + " " + iD);
            }
            if (arr[0].equals("deregister")) {
                nsocket = new Socket(machineName, nport);
                outputStream = new ObjectOutputStream(nsocket.getOutputStream());
                inputStream = new ObjectInputStream(nsocket.getInputStream());
                threadB.close();
                threadB.interrupt();
                outputStream.close();
                inputStream.close();
                nsocket.close();
                outputStream.writeObject(arr[0] + " " + iD);
                register = false;
            }
            if (arr[0].equals("disconnect")) {
                nsocket = new Socket(machineName, nport);
                outputStream = new ObjectOutputStream(nsocket.getOutputStream());
                inputStream = new ObjectInputStream(nsocket.getInputStream());
                online = false;
                outputStream.writeObject(arr[0] + " " + iD);
            }
            if (arr[0].equals("reconnect")) {
                nsocket = new Socket(machineName, nport);
                outputStream = new ObjectOutputStream(nsocket.getOutputStream());
                inputStream = new ObjectInputStream(nsocket.getInputStream());
                BPort = Integer.parseInt(arr[1]);
                threadB = new MulticastThread(machineName, BPort, logFileName);
                threadB.start();
                online = true;
                outputStream.writeObject("reconnect " + iD + " " + arr[1]);
            }
            if (arr[0].equals("msend")) {
                nsocket = new Socket(machineName, nport);
                outputStream = new ObjectOutputStream(nsocket.getOutputStream());
                outputStream.writeObject(userCommand.substring(6));
            }
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
} // class
