package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class 
class myftp {

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

        Socket client = new Socket(machineName, port);
        Scanner scanner = new Scanner(System.in);

        InputStreamReader reader = new InputStreamReader(client.getInputStream());
        OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());

        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);

        while (true) {

            String serverMsg = br.readLine();
            System.out.print(serverMsg);
            String command = scanner.nextLine();

            bw.write(command);
            bw.newLine();
            bw.flush();

            String stringWithoutAmpersands = command.replaceAll(" &", "");
            String arr[] = stringWithoutAmpersands.split(" ");

            for (int i = 0; i < arr.length; i++) {
                int value = i + 1;
                if (arr[i].equals("get")) {
                    get(arr[value], client);
                    // runNow(() -> {
                    // try {
                    // get(arr[value], client); // all this is doing is placing put() on another
                    // thread
                    // } catch (IOException e) { // i didnt change anything else
                    // e.printStackTrace();
                    // } // try
                    // });
                } // if

                // LETS PUT IT TO THE TEST LOL
                if (arr[i].equals("put")) {
                    put(arr[value], client);
                    // runNow(() -> {
                    // try {
                    // put(arr[value], client); // all this is doing is placing put() on another
                    // thread
                    // } catch (IOException e) { // i didnt change anything else
                    // e.printStackTrace();
                    // } // try
                    // });
                } // if

                if (arr[i].equals("delete")) {
                    runNow(() -> {
                        // try {
                        // } catch (IOException e) { // i didnt change anything else
                        // e.printStackTrace();
                        // } // try
                    });
                }

                if (arr[i].equals("ls")) {
                    final String s = br.readLine();
                    System.out.println(s);
                    // runNow(() -> {
                    // System.out.println(s);
                    // });
                }

                if (arr[i].equals("cd")) {
                    final String s = br.readLine();
                    System.out.println(s);
                    // runNow(() -> {
                    // System.out.println(s);
                    // });
                }

                if (arr[i].equals("mkdir")) {

                }

                if (arr[i].equals("pwd")) {
                    final String s = br.readLine();
                    System.out.println(s);
                    // runNow(() -> {
                    // System.out.println(s);
                    // });
                }

                if (arr[i].equals("quit")) {
                    final String s = br.readLine();
                    System.out.println(s);
                    // runNow(() -> {
                    // System.out.println(s);
                    // });
                    client.close();
                    break;
                } // if
            } // for
        } // while
    } // main

    // made changes to put for test
    private static void put(String filepath, Socket s) throws FileNotFoundException, IOException {

        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);
        OutputStream out = s.getOutputStream();

        // read and send in chunks
        byte[] buffer = new byte[32]; // <----changed for test
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            try {
                Thread.sleep(1000); // this might simulate a larger file
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

    // creates a new thread
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.start();
    }
} // class
