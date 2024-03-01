import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientThreadPool {
    private static Map<Integer, Worker> threadPool = new ConcurrentHashMap<>();
    private static Integer threadID = 0;

    private ClientThreadPool() {
        // Private constructor to prevent instantiation
    }

    public static Map<Integer, Worker> getThreadPool() {
        return threadPool;
    }

    public static void put(Integer id, Worker r) {
        threadPool.put(id, r);
    }

    public static void remove(Integer id) {
        threadPool.remove(id);
    }

    public static Worker getThread(Integer id) {
        return threadPool.get(id);
    }
}
