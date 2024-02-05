import java.io.*; 
import java.net.*; 
  
// Client class 
class myftp { 
    
    public static void main(String[] args) throws IOException{ 
        
        Socket client = new Socket("localhost", 4333);
       
        PrintWriter text = new PrintWriter(client.getOutputStream());
        text.println("hello");
        text.flush();
        
    }
}
