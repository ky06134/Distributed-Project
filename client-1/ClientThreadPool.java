import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientThreadPool {
    private static Map<Integer, ClientWorker> threadPool = new ConcurrentHashMap<>();
    private static Integer threadID = 0;

    private ClientThreadPool() {
        // Private constructor to prevent instantiation
    }

    public static Map<Integer, ClientWorker> getThreadPool() {
        return threadPool;
    }

    public static void put(Integer id, ClientWorker r) {
        threadPool.put(id, r);
    }

    public static void remove(Integer id) {
        threadPool.remove(id);
    }

    public static ClientWorker getThread(Integer id) {
        return threadPool.get(id);
    }
}
