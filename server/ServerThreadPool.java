import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerThreadPool {
    private static Map<Integer, Pair<String, Thread>> threadPool = new ConcurrentHashMap<>();
    private static Integer threadID = 0;

    private ServerThreadPool() {
        // Private constructor to prevent instantiation
    }

    public synchronized static Integer generateID() {
        threadID++;
        return threadID;
    }

    public static void runNow(Runnable target, String cmd, Integer id) {
        Thread t = new Thread(target);
        threadPool.put(id, new Pair<String, Thread>(cmd, t));
        t.start();
    }

    public static Map<Integer, Pair<String, Thread>> getThreadPool() {
        return threadPool;
    }

    public static void remove(Integer id) {
        threadPool.remove(id);
    }

    public static Thread getThread(Integer id) {
        return threadPool.get(id).getSecond();
    }
}
