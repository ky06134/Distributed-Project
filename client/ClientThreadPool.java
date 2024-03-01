import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientThreadPool {
    private static Map<Long, Pair<String, Thread>> threadPool = new ConcurrentHashMap<>();

    private ClientThreadPool() {
        // Private constructor to prevent instantiation
    }

    public static void runNow(Runnable target, String cmd, long currentThreadId) {
        Thread t = new Thread(target);
        threadPool.put(currentThreadId, new Pair<String, Thread>(cmd, t));
        t.start();
    }

    public static Map<Long, Pair<String, Thread>> getThreadPool() {
        return threadPool;
    }

    public static void remove(long id) {
        threadPool.remove(id);
    }

}
