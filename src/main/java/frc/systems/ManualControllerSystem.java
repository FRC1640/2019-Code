package frc.systems;

import java.util.HashMap;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.Robot;
import frc.robot.Robot.RobotState;
import frc.utilities.WebSocket;
import frc.utilities.WebSocket.WebSocketHandler;
import frc.utilities.WebSocket.WebSocketSession;

public class ManualControllerSystem extends RobotSystem {

	private ManualControllerSystem self;

	private WebSocket ws;
	private WebSocketSession session;

	private HashMap<Integer, CANSparkMax> sparkMap;
	private HashMap<Integer, WPI_TalonSRX> talonMap;
	private HashMap<Integer, Solenoid> solenoidMap;
	private HashMap<Integer, Servo> servoMap;

	public ManualControllerSystem() {
		super("Manual Control Test System");
		self = this;
	}

	@Override
	public void init() {
		ws = new WebSocket(9999);

		sparkMap = new HashMap<>();
		talonMap = new HashMap<>();
		solenoidMap = new HashMap<>();
		servoMap = new HashMap<>();

		int[] sparkChannels = new int[] { 1, 14, 15, 16 };
		int[] talonChannels = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };
		int[] solenoidChannels = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
		int[] servoChannels = new int[] { 6, 7, 8, 9 }; // 2019
		// int[] servoChannels = new int[] { 0, 1, 2, 3 }; // 2018

		for (int i : sparkChannels) {
			try { sparkMap.put(i, new CANSparkMax(i, MotorType.kBrushless)); }
			catch (Exception e) { warn("No spark on " + i); }
		}

		for (int i : talonChannels) {
			try { talonMap.put(i, new WPI_TalonSRX(i)); }
			catch (Exception e) { warn("No talon on " + i); }
		}

		for (int i : solenoidChannels) {
			try { solenoidMap.put(i, new Solenoid(i)); }
			catch (Exception e) { warn("No solenoid on " + i); }
		}

		for (int i : servoChannels) {
			try { servoMap.put(i, new Servo(i)); }
			catch (Exception e) { warn("No servo on " + i); }
		}

		for (int i : servoMap.keySet()) {
			System.out.println("Servo: " + i);
		}

		ws.registerHandler(new WebSocketHandler() {

			@Override
			public void onOpen(WebSocketSession session) {
				self.session = session;
				log("Websocket connected");
			}

			@Override
			public void onMessage(String message) {
				if (Robot.getState() == RobotState.TELEOP) {
					try {
						JSONObject data = new JSONObject(message);

						JSONArray motorArray = (data.has("motorData")) ? data.getJSONArray("motorData") : new JSONArray();
						JSONArray solenoidArray = (data.has("solenoidData")) ? data.getJSONArray("solenoidData") : new JSONArray();
						JSONArray servoArray = (data.has("servoData")) ? data.getJSONArray("servoData") : new JSONArray();

						for (int i = 0; i < servoArray.length(); i++) {
							JSONObject jo = servoArray.getJSONObject(i);
							int channel = jo.getInt("channel");
							boolean enabled = jo.getBoolean("enable");
							try {
								if (enabled && servoMap.containsKey(channel)) {
									double angle = jo.getDouble("angle");
									servoMap.get(channel).set(angle);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						for (int i = 0; i < solenoidArray.length(); i++) {
							JSONObject jo = solenoidArray.getJSONObject(i);
							int channel = jo.getInt("channel");
							try {
								if (solenoidMap.containsKey(channel)) {
									solenoidMap.get(jo.getInt("channel")).set(jo.getBoolean("extended"));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						for (int i = 0; i < motorArray.length(); i++) {
							JSONObject jo = motorArray.getJSONObject(i);
							int channel = jo.getInt("channel");
							boolean enabled = jo.getBoolean("enable");
							double speed = (enabled) ? jo.getDouble("speed") : 0.0;

							try {
								if (sparkMap.containsKey(channel)) {
									sparkMap.get(i).set(speed);
								} else if (talonMap.containsKey(channel)) {
									talonMap.get(i).set(speed);
								}
							} catch (Exception e) {
								// e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onError(String message) {
				System.out.println(message);
			}

			@Override
			public void onClose() {
				session = null;
				warn("Lost connection...");
			}

		});
	}

	@Override
	public void preStateUpdate() throws Exception {

	}

	@Override
	public void postStateUpdate() throws Exception {

	}

	@Override
	public void disabledInit() throws Exception {

	}

	@Override
	public void disabledUpdate() throws Exception {

	}

	@Override
	public void autonInit() throws Exception {

	}

	@Override
	public void autonUpdate() throws Exception {

	}

	@Override
	public void teleopInit() throws Exception {

	}

	@Override
	public void teleopUpdate() throws Exception {

	}

	@Override
	public void testInit() throws Exception {

	}

	@Override
	public void testUpdate() throws Exception {

	}

	@Override
	public void enable() throws Exception {

	}

	@Override
	public void disable() throws Exception {

	}

}