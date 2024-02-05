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

            String msgFromClient = br.readLine();
            String arr[] = msgFromClient.split(" ", 2);

            System.out.println("The command is " + msgFromClient);

            if (arr[0].equals("get")) {
                
            }

            if (arr[0].equals("put")) {
                
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

            }

            if (arr[0].equals("quit")) {

            }
        }
    }
}