import java.io.*; 
import java.net.*; 
import java.util.Scanner;
  
// Client class 
class myftp { 
    
    public static void main(String[] args) throws IOException{ 
        
        Socket client = new Socket("localhost", 4333);

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
