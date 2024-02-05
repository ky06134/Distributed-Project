import java.io.*;
import java.net.*;
import java.util.*;

public class myftpserver {
    public static void main(String[]args) throws Exception {

        ServerSocket server = new ServerSocket(4333);
        System.out.println("server is now online");

        Socket s=server.accept(); //waits for connection from client

        System.out.println("Client Connected");
        System.out.println("Enter Command: ");
        
        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);

        String str = bf.readLine();
        System.out.println("client : " + str);

        System.exit(0);
    
    }
}