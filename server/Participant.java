import java.io.IOException;
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
    private OutputStream out;
    private Socket s;
    private int port;

    public Participant(int id, InetAddress ip, String status) {
        this.id = id;
        this.ip = ip;
        this.status = status;
    } //constructor

    public void addMsg(String s, Long t) {
        msgHistory.add(new Pair<String, Long>(s, t));
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
    } //getHistory

    public String getStatus() {
        return this.status;
    } //getStatus

    public void setStatus(String s) {
        this.status = s;
    }

    public void setPort(int port) {
        this.port = port;
    } //setPort

    public int getPort() {
        return this.port;
    } //getPort

    public void connect() throws IOException {
        this.s = new Socket(this.ip, this.port);
        this.out = this.s.getOutputStream();
    } //connect

    public OutputStream getOutputStream() {
        return this.out;
    } //getOutputStream

    public int getId() {
        return this.id;
    }
} //class
