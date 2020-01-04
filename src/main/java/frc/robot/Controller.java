package frc.robot;

import java.util.Collection;
import java.util.HashMap;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.systems.RobotSystem;
import frc.utilities.Vector2;
import edu.wpi.first.wpilibj.XboxController;

public class Controller {

	public static enum ButtonEvent { PRESS, RELEASE };

	public static enum Button {
		A, B, X, Y, LB, RB, LT, RT, START, SELECT, LJ, RJ, N, E, S, W, NE, SE, SW, NW;
	}

	public static enum Axis {
		LX, LY, RX, RY, LT, RT;
	}

	private static final double DEFAULT_DEADBAND = 0.2;

	private XboxController controller;
	private HashMap<Button,Boolean> prevButtonMap;
	private HashMap<Button,Boolean> currentButtonMap;
	private HashMap<Integer,ButtonCallbackHandler> callbackMap;
	private int nextId;
	private boolean valid = false;

	private Object buttonMapLock;

	public Controller (int port) {

		try {
			controller = new XboxController(port);
			valid = true;
		} catch (Exception e) {
			controller = null;
			valid = false;
		}

		callbackMap = new HashMap<>();

		String name;
		if (port == 0) { name = "DRIVER"; }
		else if (port == 1) { name = "OPERATOR"; }
		else if (port == 2) { name = "HAB"; }
		else { name = "UNKNOWN"; }

		new RobotSystem(String.format("Controller #%d (%s)", port, name)) {
			@Override
			public void init () {
				if (!valid) { error("No controller on port #" + port + ". Calls will return default values."); }
				
				prevButtonMap = new HashMap<>();
				currentButtonMap = new HashMap<>();
				nextId = 0;
				buttonMapLock = new Object();
			}

			@Override
			public void preStateUpdate () {
				synchronized (buttonMapLock) {
					for (Button button : Button.values()) {
						currentButtonMap.put(button,getButtonRaw(button));
					}
				}
				for (Button button : Button.values()) {
					if (currentButtonMap.containsKey(button) && prevButtonMap.containsKey(button)) {
						boolean now = currentButtonMap.get(button);
						boolean prev = prevButtonMap.get(button);
						if (now != prev && now) { log(String.format("Pressed: %s", button.toString())); }
						else if (now != prev && prev) { log(String.format("Released: %s", button.toString())); }
					}
				}
			}

			@Override public void postStateUpdate () {
				synchronized (buttonMapLock) {
					HashMap<Button,Boolean> tmp = prevButtonMap;
					prevButtonMap = currentButtonMap;
					currentButtonMap = tmp;
				}
			}

			@Override public void teleopUpdate () {
				Collection<ButtonCallbackHandler> handlers;
				synchronized (callbackMap) { handlers = callbackMap.values(); }
				for (ButtonCallbackHandler handler : handlers) {
					boolean condition;
					synchronized (buttonMapLock) {
						condition = (handler.event == ButtonEvent.PRESS && getButtonPressed(handler.button)) ||
									(handler.event == ButtonEvent.RELEASE && getButtonReleased(handler.button));
					}
					if (condition) { handler.task.run(); }
				}
			}

			@Override public void disabledInit () { }
			@Override public void disabledUpdate () { }
			@Override public void autonInit () { }
			@Override public void autonUpdate () { }
			@Override public void teleopInit () { }
			@Override public void testInit () { }
			@Override public void testUpdate () { }
			@Override public void enable () { }
			@Override public void disable () { }
		};
	}

	public int registerButtonListener (ButtonEvent be, Button b, Runnable callback) {
		int id = nextId++;
		ButtonCallbackHandler h = new ButtonCallbackHandler(be, b, callback);
		synchronized (callbackMap) { callbackMap.put(id, h); }
		return id;
	}

	public void unregisterButtonListener (int id) {
		synchronized (callbackMap) { callbackMap.remove(id); }
	}

	public void unregisterAllButtonListeners () {
		synchronized (callbackMap) { callbackMap.clear(); }
	}

	public boolean getButton (Button button) {
		synchronized (buttonMapLock) { return currentButtonMap.get(button); }
	}

	private boolean getButtonRaw (Button button) {
		if (!valid) { return false; }
		switch (button) {
			case A: return controller.getAButton();
			case B: return controller.getBButton();
			case X: return controller.getXButton();
			case Y: return controller.getYButton();
			case LB: return controller.getBumper(Hand.kLeft);
			case RB: return controller.getBumper(Hand.kRight);
			case LT: return controller.getTriggerAxis(Hand.kLeft) > 0.3;
			case RT: return controller.getTriggerAxis(Hand.kRight) > 0.5;
			case START: return controller.getStartButton();
			case SELECT: return controller.getBackButton();
			case LJ: return controller.getStickButton(Hand.kLeft);
			case RJ: return controller.getStickButton(Hand.kRight);
			case N: return controller.getPOV() == 0;
			case E: return controller.getPOV() == 90;
			case S: return controller.getPOV() == 180;
			case W: return controller.getPOV() == 270;
			case NE: return controller.getPOV() == 45;
			case SE: return controller.getPOV() == 135;
			case SW: return controller.getPOV() == 225;
			case NW: return controller.getPOV() == 315;
			default: return false;
		}
	}

	private boolean getButtonPressed (Button button) {
		boolean result = currentButtonMap.get(button) && !prevButtonMap.get(button);
		synchronized (buttonMapLock) { return result; }
	}

	private boolean getButtonReleased (Button button) {
		boolean result = !currentButtonMap.get(button) && prevButtonMap.get(button);
		synchronized (buttonMapLock) { return result; }
	}

	public double getAxis (Axis axis, double deadband) {
		if (!valid) { return 0.0; }
		Vector2 lj = applyDeadband(controller.getX(Hand.kLeft), -controller.getY(Hand.kLeft), deadband);
		Vector2 rj = applyDeadband(controller.getX(Hand.kRight), -controller.getY(Hand.kRight), deadband);
		switch (axis) {
			case LX: return lj.getX();
			case LY: return lj.getY();
			case RX: return rj.getX();
			case RY: return rj.getY();
			case LT: return applyDeadband(controller.getTriggerAxis(Hand.kLeft), 0, deadband).getX();
			case RT: return applyDeadband(controller.getTriggerAxis(Hand.kRight), 0, deadband).getX();
			default: return 0.0;
		}
	}

	public double getAxis (Axis axis) {
		return getAxis(axis, DEFAULT_DEADBAND);
	}

	public int getPOV () {
		if (!valid) { return -1; }
		return controller.getPOV();
	}

	public void vibrate(RumbleType type, double amount) {
		controller.setRumble(type, amount);
	}

	private Vector2 applyDeadband (double x, double y, double deadband) {
		Vector2 result = new Vector2(x, y);
		if(result.magnitude() < deadband) {
			result.set(0, 0);
		}
		else {
			double rescale = ( (result.magnitude()-deadband) / (1-deadband) );
			result.multiply(rescale);
		}

		return result;
	}

	private static class ButtonCallbackHandler {
		
		ButtonEvent event;
		Button button;
		Runnable task;

		public ButtonCallbackHandler (ButtonEvent e, Button b, Runnable r) {
			event = e;
			button = b;
			task = r;
		}

	}

}