import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {
    private static Map<String, Lock> locks = new ConcurrentHashMap<>();

    private LockManager() {
        // Private constructor to prevent instantiation
    }

    public static void put(String file, Lock lock) {
        locks.put(file, lock);
    }

    public static void remove(String file) {
        locks.remove(file);
    }

    public static Lock get(String file) {
        return locks.get(file);
    }
}
