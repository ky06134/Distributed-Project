import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerThreadPool {
    private static Map<Integer, Thread> threadPool = new ConcurrentHashMap<>();
    private static Integer threadID = 0;

    private ServerThreadPool() {
        // Private constructor to prevent instantiation
    }

    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        threadID++;
        threadPool.put(threadID, t);
        t.start();
    }

    public static Map<Integer, Thread> getThreadPool() {
        return threadPool;
    }

    public static void remove(Integer id) {
        threadPool.remove(id);
    }

    public static int getThreadId() {
        return threadID;
    }

    public static boolean isEmpty() {
        return threadPool.isEmpty();
    }

    public static void purge() {
        for (Map.Entry<Integer, Thread> entry : threadPool.entrySet()) {
            Thread thread = entry.getValue();
            if (!thread.isAlive()) {
                // Thread is no longer alive, remove it from the map
                threadPool.remove(entry.getKey());
            }
        }
    }
}
