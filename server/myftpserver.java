package server;
import java.io.*;
import java.net.*;
import java.util.*;

public class myftpserver {
    public static void main(String[]args) throws Exception {

        Socket socket = null;
        InputStreamReader reader = null;
        OutputStreamWriter writer = null;
        BufferedReader br= null;
        BufferedWriter bw = null;
        Integer port = 0; 
        
        try {
            port = Integer.valueOf(args[0]); //grab port from command line arg
        } catch (NumberFormatException e) {
            System.out.println("Invalid Port");
            System.exit(0);
        } //catch

        ServerSocket server = new ServerSocket(port);
        System.out.println("server is now online running on port: " + port);


        Socket s =server.accept(); //waits for connection from client

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
                
            }

            if (arr[0].equals("put")) {
                put(arr[1], s);
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
                String path = System.getProperty("user.dir");
                bw.write("path" + path);
                bw.newLine();
                bw.flush();
            }

            if (arr[0].equals("quit")) {

            } //if
        } //while

    } //main

    private static void put(String destination, Socket s) throws IOException {

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

        System.out.println("done");

        // in.close();
        // out.close();
    }

} //myftpserver