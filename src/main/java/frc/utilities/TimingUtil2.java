package frc.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimingUtil2 {

	private static HashMap<Integer,Handler> handlerCallbackMap;
	
	private static long globalStartTime = 0;
	private static int nextId = 0;

	final static Object idLock = new Object();

	static {
		globalStartTime = System.currentTimeMillis();
		handlerCallbackMap = new HashMap<>();
		new Thread(() -> {

			List<Integer> toRemove = new ArrayList<>();

			while (true) {
				long currentTime = System.currentTimeMillis();

				synchronized (handlerCallbackMap) {
					for (int key : handlerCallbackMap.keySet()) {
						try {
							Handler h = handlerCallbackMap.get(key);
							if (h != null && currentTime > h.timeToActivate) {
								h.callback.run();
								if (h.runOnce) {
									toRemove.add(key);
								} else {
									h.updateActivateTime();
								}
							}
						} catch (Exception e) {
							LogUtil.error("TimingUtil2", e.getMessage());
					e.printStackTrace();
						}
					}
					for (int key : toRemove) { handlerCallbackMap.remove(key); }
				}

				try { Thread.sleep(10); }
				catch (Exception e) { }
			}

		}).start();
	}

	public static double getElapsedTimeInSeconds () {
		return (System.currentTimeMillis() - globalStartTime) * 1e-3;
	}

	public static int registerOneTimeCallback (long delayInMs, Runnable callback) {
		Handler h = new Handler(System.currentTimeMillis(), delayInMs, 0, callback, true);
		int id; synchronized (idLock) { id = nextId++; }
		synchronized (handlerCallbackMap) { handlerCallbackMap.put(id, h); }
		return id;
	}

	public static int registerRecurringCallback (long delayInMs, long periodInMs, Runnable callback) {
		Handler h = new Handler(System.currentTimeMillis(), delayInMs, periodInMs, callback, false);
		int id; synchronized (idLock) { id = nextId++; }
		synchronized (handlerCallbackMap) { handlerCallbackMap.put(id, h); }
		return id;
	}

	public static void cancelCallback (int id) {
		synchronized (handlerCallbackMap) { handlerCallbackMap.remove(id); }
	}

	private static class Handler {

		long timePeriod;
		long timeToActivate;
		boolean runOnce;

		Runnable callback;

		public Handler (long tReg, long tDelay, long tPeriod, Runnable callback, boolean runOnce) {
			timeToActivate = tReg + tDelay;
			timePeriod = tPeriod;
			this.callback = callback;
			this.runOnce = runOnce;
		}

		public void updateActivateTime () {
			timeToActivate += timePeriod;
		}

	}

}