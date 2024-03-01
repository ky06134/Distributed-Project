import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerThreadPool {
    private static Map<Long, Pair<String, Thread>> threadPool = new ConcurrentHashMap<>();
    private static Integer threadID = 0;

    private ServerThreadPool() {
        // Private constructor to prevent instantiation
    }

    public static long runNow(Runnable target, String cmd, long currentThreadId) {
        Thread t = new Thread(target);
        threadID++;
        currentThreadId += threadID;
        threadPool.put(currentThreadId, new Pair<String, Thread>(cmd, t));
        t.start();
        return currentThreadId;
    }

    public static Map<Long, Pair<String, Thread>> getThreadPool() {
        return threadPool;
    }

    public static void remove(long id) {
        threadPool.remove(id);
    }

    public static int getThreadId() {
        return threadID;
    }
}
