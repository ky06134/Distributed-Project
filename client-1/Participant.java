import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

// Client class 
class Participant {

    private static InputStream in;
    private static OutputStream out;
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
        int threadBPort;
        Thread threadB = new Thread();

        nsocket = new Socket(machineName, nport);
        Participant.in = nsocket.getInputStream();
        Participant.out = nsocket.getOutputStream();
        Scanner scanner = new Scanner(System.in);
        String userCommand = "";

        InputStreamReader reader = new InputStreamReader(Participant.in);
        OutputStreamWriter writer = new OutputStreamWriter(Participant.out);
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);

        while (!userCommand.equals("quit")) {
            userCommand = scanner.nextLine();
            String arr[] = userCommand.split(" ");

            byte[] msg = userCommand.getBytes();
            out.write(msg, 0, msg.length);

            if (arr[0].equals("register")) {
                threadBPort = Integer.parseInt(arr[1]);
                nsocket = new Socket(machineName, threadBPort);
                register = true;
            }
            if (arr[0].equals("deregister")) {
                threadB.interrupt();
                writer.close();
                reader.close();
                nsocket.close();
                register = false;
            }
            if (arr[0].equals("disconnect")) {
                online = false;
            }
            if (arr[0].equals("reconnect")) {
                threadBPort = Integer.parseInt(arr[1]);
                nsocket = new Socket(machineName, threadBPort);
                threadB.start();
                online = true;
            }
            if (arr[0].equals("msend")) {
                nsocket = new Socket(machineName, nport);
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
