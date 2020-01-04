package frc.robot;

import java.net.Socket;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashMap;

import frc.systems.RobotSystem;

import java.io.InputStream;

public class KeyboardController extends RobotSystem {

	private HashMap<Integer,ButtonCallbackHandler> callbackMap;
	private int nextId;
	private String lastKey;

	private int socketPort;
	private ServerSocket serverSocket;
	private Socket socket;
	private InputStream inputStream;

	private Object lock;

	public KeyboardController(int socketPort) {
		super("Keyboard Controller");
		this.socketPort = socketPort;
		
		callbackMap = new HashMap<>();
		nextId = 0;

		lock = new Object();
	}

	@Override
	public void init() {
		try {
			serverSocket = new ServerSocket(socketPort);
			socket = serverSocket.accept();
			System.out.print("Socket Connected!");
			inputStream = socket.getInputStream();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public int registerButtonListener (String key, Runnable callback) {
		int id = nextId++;
		ButtonCallbackHandler handler = new ButtonCallbackHandler(key, callback);
		synchronized (callbackMap) { callbackMap.put(id, handler); }
		return id;
	}

	public void unregisterButtonListener (int id) {
		synchronized (callbackMap) { callbackMap.remove(id); }
	}

	public void unregisterAllButtonListeners () {
		synchronized (callbackMap) { callbackMap.clear(); }
	}

	public boolean getButtonPressed (String key) {
		synchronized (lock) {
			return lastKey.equals(key);
		}
	}

	@Override
	public void preStateUpdate() {
		try {
			// System.out.println("Running");
			int i = inputStream.read();
			// System.out.println("Done");
			byte b = (byte) (i & 0xff);
			synchronized (lock) {
				lastKey = new String(new byte[] { b }, "UTF-8");
			}
			// System.out.println(lastKey);
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	@Override
	public void postStateUpdate() {
		// keyMap.put(lastKey, false);
		synchronized (lock) {
			lastKey = null;
		}
	}

	@Override
	public void disabledInit() {

	}

	@Override
	public void disabledUpdate() {

	}

	@Override
	public void autonInit() {

	}

	@Override
	public void autonUpdate() {
	}

	@Override
	public void teleopInit() {
	}

	@Override
	public void teleopUpdate() {
		Collection<ButtonCallbackHandler> handlers;
		synchronized (callbackMap) { handlers = callbackMap.values(); }
		for (ButtonCallbackHandler handler : handlers) {
			boolean condition;
			condition = (getButtonPressed(handler.key));
			if (condition) { new Thread(handler.task).start(); }
		}
	}

	@Override
	public void testInit() {

	}

	@Override
	public void testUpdate() {

	}

	@Override
	public void enable() {

	}

	@Override
	public void disable() {

	}

	private static class ButtonCallbackHandler {
		
		String key;
		Runnable task;

		public ButtonCallbackHandler (String k, Runnable r) {
			key = k;
			task = r;
		}

	}

}
