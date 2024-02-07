package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class myftpserver {
    public static void main(String[] args) throws Exception {

        Socket socket = null;
        InputStreamReader reader = null;
        OutputStreamWriter writer = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        Integer port = 0;

        try {
            port = Integer.valueOf(args[0]); // grab port from command line arg
        } catch (NumberFormatException e) {
            System.out.println("Invalid Port");
            System.exit(0);
        } // catch

        ServerSocket server = new ServerSocket(port);
        System.out.println("server is now online running on port: " + port);

        Socket s = server.accept(); // waits for connection from client

        System.out.println("Client Connected");

        reader = new InputStreamReader(s.getInputStream());
        writer = new OutputStreamWriter(s.getOutputStream());
        br = new BufferedReader(reader);
        bw = new BufferedWriter(writer);

        while (true) {

            bw.write("myftp>");
            bw.newLine();
            bw.flush();

            br = new BufferedReader(reader);
            bw = new BufferedWriter(writer);

            String msgFromClient = br.readLine();
            String arr[] = msgFromClient.split(" ");

            System.out.println("The command is " + msgFromClient);

            if (arr[0].equals("get")) {
                get(arr[1], s);
            }

            if (arr[0].equals("put")) {
                put(arr[1], s);
            }

            if (arr[0].equals("delete")) {
                delete(arr[1]);
            }

            if (arr[0].equals("cd")) {
                String path = System.getProperty("user.dir");
                if (arr[1].equals("..")) {
                    System.setProperty("user.dir", new File(path).getParentFile().getAbsolutePath());
                } else {
                    System.setProperty("user.dir", path + "/" + arr[1]);
                }
                bw.write("path" + System.getProperty("user.dir"));
                bw.newLine();
                bw.flush();
            }

            if (arr[0].equals("mkdir")) {
                makeDirectory(arr[1]);
            }

            if (arr[0].equals("pwd")) {
                String path = System.getProperty("user.dir");
                bw.write("path" + path);
                bw.newLine();
                bw.flush();
            }

            if (arr[0].equals("ls")) {
                bw.write(listDirectory(System.getProperty("user.dir")));
                bw.newLine();
                bw.flush();
            }

            if (arr[0].equals("quit")) {

            } // if
        } // while

    } // main

    private static void put(String destination, Socket s) throws IOException {

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

    }

    /*
     * given a filepath and socket send the file over TCP socket
     */
    private static void get(String filepath, Socket s) throws FileNotFoundException, IOException {

        // read stream
        File file = new File(filepath);
        InputStream in = new FileInputStream(file);
        OutputStream out = s.getOutputStream();

        // read and send in chunks
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        } // while
        String delimiter = "\0";
        out.write(delimiter.getBytes());

    } // get

    private static void delete(String filename) {
        File file = new File(filename);
        file.delete();
    } // delete

    private static void makeDirectory(String directoryName) {
        File file = new File(directoryName);
        file.mkdir();
    } // makeDirectory

    private static String listDirectory(String directory) {
        String res = "";
        File currentDirectory = new File(directory);
        File[] files = currentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                res += file.getName() + "\t";
            }
        } else {
            System.out.println("No files found.");
        }
        return res;
    } 

} //myftpserver
