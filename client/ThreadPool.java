import java.util.HashMap;

//havent used this yet dont know if i have to O.o
public class ThreadPool {
    //consider changing HashMap -> ConcurrentHashMap
    private static HashMap<Integer, Thread> threadPool = new HashMap<Integer, Thread>();;
    private static Integer threadID = 0; 

    //no constructor

    //method for thread creation
    public static Integer runNow(Runnable target) {
        Thread t = new Thread(target);
        threadID++;
        threadPool.put(threadID, t);
        t.start();
        return threadID;
    }
}
