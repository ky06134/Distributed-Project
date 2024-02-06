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

        Socket client = new Socket("localhost", port);
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

            br = new BufferedReader(reader);
            bw = new BufferedWriter(writer);

            String arr[] = command.split(" ");

            if (arr[0].equals("get")) {
                get(arr[1], client);
            }

            if (arr[0].equals("put")) {
                put(arr[1], client);
            }

            if (arr[0].equals("delete")) {
                
            }

            if (arr[0].equals("ls")) {

            }

            if (arr[0].equals("cd")) {

            }

            if (arr[0].equals("mkdir")) {

            }

            if (arr[0].equals("pwd")) {
                serverMsg = br.readLine();
                System.out.println(serverMsg);
            }

            if (arr[0].equals("quit")) {

            } // if
        } // while
    } // main

    /*
     * given a filepath and socket send the file over TCP socket
     */
    private static void put(String filepath, Socket s) throws FileNotFoundException, IOException {

        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);
        OutputStream out = s.getOutputStream();

        // read and send in chunks
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        } // while
        String delimiter = "\r\n\r\n";
        out.write(delimiter.getBytes());

    } // get

    private static void get(String destination, Socket s) throws IOException {

        InputStream in = s.getInputStream();
        OutputStream out = new FileOutputStream(destination);
        StringBuilder sb = new StringBuilder();

        final String delimiter = "\r\n\r\n"; // Define a delimiter

        //read and write to a file
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            sb.append(new String(buffer, 0, bytesRead));
            
            if (sb.toString().contains(delimiter)) {
                break;
            }
        } //while
    }


} // myftp