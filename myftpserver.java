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
        
        
        ServerSocket server = new ServerSocket(4333);
        System.out.println("server is now online");

        Socket s =server.accept(); //waits for connection from client

        System.out.println("Client Connected");
        
        reader = new InputStreamReader(s.getInputStream());
        writer = new OutputStreamWriter(s.getOutputStream());

        br = new BufferedReader(reader);
        bw = new BufferedWriter(writer);

        String msgFromClient = br.readLine();

        System.out.println("The command is " + msgFromClient);

        bw.write("commmand received");
        

        System.exit(0);
    
    }
}