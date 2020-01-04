package frc.utilities;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TimingUtil {

	private static HashMap<Integer,TimerTask> taskCallbackMap;
	private static Timer timer;

	private static long globalStartTime = 0;

	private static int nextId = 0;

	final static Object idLock = new Object();

	static {
		globalStartTime = System.currentTimeMillis();
		timer = new Timer();
		taskCallbackMap = new HashMap<>();
	}

	public static double getElapsedTimeInSeconds () {
		return (System.currentTimeMillis() - globalStartTime) * 1e-3;
	}

	public static int registerOneTimeCallback (long delayInMs, Runnable callback) {
		int id; synchronized (idLock) { id = nextId++; }
		TimerTask tt;
		timer.schedule(tt = new TimerTask() {
			@Override public void run() { callback.run(); }
		}, delayInMs);
		synchronized (taskCallbackMap) { taskCallbackMap.put(id, tt); }
		return id;
	}

	public static int registerRecurringCallback (long delayInMs, long periodInMs, Runnable callback) {
		int id; synchronized (idLock) { id = nextId++; }
		TimerTask tt;
		timer.scheduleAtFixedRate(tt = new TimerTask() { 
			@Override public void run () { callback.run(); }
		}, delayInMs, periodInMs);
		synchronized (taskCallbackMap) { taskCallbackMap.put(id, tt); }
		return id;
	}

	public static void cancelCallback (int id) {
		TimerTask tt; synchronized (taskCallbackMap) { tt = taskCallbackMap.remove(id); }
		if (tt != null) { tt.cancel(); }
	}

}