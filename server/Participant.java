import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Participant {

    private int id;
    private InetAddress ip;
    private String status;
    private Queue<Pair<String, Long>> msgHistory = new LinkedList<>();
    private OutputStream mcout;
    private Socket s;

    public Participant(int id, InetAddress ip, String status) {
        this.id = id;
        this.ip = ip;
        this.status = status;
    } //constructor

    public void addMsg(String s) {
        msgHistory.add(new Pair<String, Long>(s, System.currentTimeMillis()));
    }

    public ArrayList<String> getHistory(Long threshold) {
        Long current = System.currentTimeMillis();
        current -= threshold;
        ArrayList<String> msgToSend = new ArrayList<>();
        while (!msgHistory.isEmpty()) {
            Pair<String, Long> msg = msgHistory.poll();
            if (msg.getSecond() > current) {
                msgToSend.add(msg.getFirst());
            } //if
        }
        return msgToSend;
    }
}
