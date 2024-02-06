package client;
import java.io.*; 
import java.net.*; 
import java.util.Scanner;
  
// Client class 
class myftp { 
    
    public static void main(String[] args) throws IOException{ 
        
        Integer port = 0;
        String machineName = "";       
        
        try {
            machineName = args[0];
            port = Integer.valueOf(args[1]); //grab port from command line arg
        } catch (NumberFormatException e) {
            System.out.println("Bad arguments");
            System.exit(0);
        } //catch

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

            System.out.println(command);
            String arr[] = command.split(" ");

            if (arr[0].equals("get")) {
                
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

            }

            if (arr[0].equals("quit")) {

            } //if
        } //while
    } //main

    /*
     * given a filepath and socket send the file over TCP socket
     */
    private static void put(String filepath, Socket s) throws FileNotFoundException, IOException {

        //read stream
        FileInputStream fileInputStream = new FileInputStream(filepath);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        //write stream
        OutputStream outputStream = s.getOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        //read and send in chunks
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            bufferedOutputStream.write(buffer, 0, bytesRead);
        } //while

        bufferedOutputStream.flush();
        bufferedOutputStream.close();

        System.out.println("File sent");
    } //get
} //myftp
