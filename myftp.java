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

        while (true) {

        InputStreamReader reader = new InputStreamReader(client.getInputStream());
        OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());

        BufferedReader response = new BufferedReader(reader);
        BufferedWriter toSend = new BufferedWriter(writer);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Command: ");
        String command = scanner.nextLine();

        toSend.write(command);
        toSend.newLine();
        toSend.flush();

        scanner.close();

        }


        

        
    }
}
