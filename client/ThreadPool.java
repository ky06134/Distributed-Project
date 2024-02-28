import java.util.HashMap;

//havent used this yet dont know if i have to O.o
public class ThreadPool {
    // consider changing HashMap -> ConcurrentHashMap
    private static HashMap<String, Pair<Integer, Thread>> threadPool = new HashMap<>();
    private static Integer threadID = 0;

    // no constructor

    // method for thread creation
    public static Integer runNow(Runnable target, String cmd) {
        Thread t = new Thread(target);
        threadID++;
        threadPool.put(cmd, new Pair<Integer, Thread>(threadID, t));
        t.start();
        return threadID;
    }

    public static HashMap<String, Pair<Integer, Thread>> getThreadPool() {
        return threadPool;
    }
}
