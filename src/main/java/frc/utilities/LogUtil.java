package frc.utilities;

import java.util.ArrayDeque;

import frc.systems.RobotSystem;
import frc.utilities.TimingUtil2;

public class LogUtil {

	public static final int maxMessages = 50;

	private static enum MsgType {
		CALLBACK ("CALLBACK"), 
		INFO ("INFO"),
		WARN ("WARNING"),
		ERROR ("ERROR");

		String txt;

		private MsgType (String txt) { this.txt = txt; }
	}

	private static ArrayDeque<String> queue;

	static {
		queue = new ArrayDeque<>();

		new RobotSystem("Log System") {

			// private PrintWriter printer;

			@Override
			public void init() {
				// String timeStr = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
				// String path = String.format("/home/lvuser/logs/robotLog-%s.log", timeStr);

				// try { printer = new PrintWriter(new File(path)); } 
				// catch (Exception e) { System.err.println("Couldn't open file for logging!!"); }
			}

			@Override
			public void postStateUpdate() throws Exception {
				// if (printer == null) { return; }
				int nMsg; synchronized (queue) { nMsg = queue.size(); }
				for (int i = 0; i < nMsg; i++) {
					String msg; synchronized (queue) { msg = queue.poll(); }
					if (msg != null) { System.out.println(msg); }
				}
				// printer.flush();
			}

			@Override public void preStateUpdate() throws Exception { }
			@Override public void disabledInit() throws Exception { }
			@Override public void disabledUpdate() throws Exception { }
			@Override public void autonInit() throws Exception { }
			@Override public void autonUpdate() throws Exception { }
			@Override public void teleopInit() throws Exception { }
			@Override public void teleopUpdate() throws Exception { }
			@Override public void testInit() throws Exception { }
			@Override public void testUpdate() throws Exception { }
			@Override public void enable() throws Exception { }
			@Override public void disable() throws Exception { }
		};
	}

	public static void callback (String key, String message) {
		internalLog(key, message, MsgType.CALLBACK);
	}

	public static void callback (Class<?> clazz, String message) {
		callback(clazz.getSimpleName(), message);
	}

	public static void log (String key, String message) {
		internalLog(key, message, MsgType.INFO);
	}

	public static void log (Class<?> clazz, String message) {
		log(clazz.getSimpleName(), message);
	}

	public static void warn (String key, String message) {
		internalLog(key, message, MsgType.WARN);
	}

	public static void warn (Class<?> clazz, String message) {
		warn(clazz.getSimpleName(), message);
	}

	public static void error (String key, String message) {
		internalLog(key, message, MsgType.ERROR);
	}

	public static void error (Class<?> clazz, String message) {
		error(clazz.getSimpleName(), message);
	}

	private static void internalLog (String key, String msg, MsgType type) {
		double elapsedTime = TimingUtil2.getElapsedTimeInSeconds();
		int qSize; synchronized (queue) { qSize = queue.size(); }
		String logMsg;
		if (qSize >= maxMessages) {
			synchronized (queue) { queue.clear(); }
			logMsg = String.format("%4.3f (%s)\t[%s]\t%s", elapsedTime, "ALERT", "LogUtil", "Queue exceeded " + maxMessages + "messages. Queue cleared... logs have been lost.");
		} else {
			logMsg = String.format("%4.3f (%s)\t[%s]\t%s", elapsedTime, type.txt, key, msg);
		}
		synchronized (queue) { queue.add(logMsg); }
	}

}