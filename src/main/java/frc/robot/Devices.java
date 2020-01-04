package frc.robot;

import java.util.HashMap;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.systems.drive.controllers.SwerveController;
import frc.systems.drive.pivot.CVTPivot;
import frc.systems.drive.pivot.Pivot;
import frc.systems.drive.pivot.PivotConfig;
import frc.systems.grabber.Grabber;
import frc.systems.hab.Hab;
import frc.systems.intake.Intake;
import frc.systems.vision.Limelight;
import frc.utilities.ProximitySensor;
import frc.utilities.Vector2;

public class Devices {

	public static Devices instance = null;

	private Hab hab;
	private Intake intake;
	private Grabber grabber;
	private Gyro gyro;
	private HashMap<Pivot,Vector2> pivotMap;
	private Limelight limelight;
	private DigitalInput lightSensor;
	private ProximitySensor proxFront;
	private ProximitySensor proxBack;
	private Controller driverController;
	private Controller operatorController;
	private Controller habController;
	private Compressor compressor;
	private LineArray lineArraySensor;
	private SwerveController swerveController;

	private Devices () {
		hab = new Hab();
		intake =  new Intake(11, 6, 0, 1, 4);
		grabber = new Grabber(7, 2);
		// lift = new Lift(false, false, 3, 2);
		limelight = new Limelight();
		proxFront = new ProximitySensor(4);
		proxBack = new ProximitySensor(5);
		gyro = new Gyro();
		// lightSensor = new DigitalInput(0);

		PivotConfig.loadConfigs("/home/lvuser/deploy/config/pivotcfg.json");
		pivotMap = new HashMap<>();

		driverController = new Controller(0);
		operatorController = new Controller(1);
		habController = new Controller(2);

		pivotMap.put(new CVTPivot("1"), new Vector2()); // FL
		pivotMap.put(new CVTPivot("2"), new Vector2()); // FR
		pivotMap.put(new CVTPivot("3"), new Vector2()); // BL
		pivotMap.put(new CVTPivot("4"), new Vector2()); // BR

		compressor = new Compressor();
		compressor.setClosedLoopControl(true);

		lineArraySensor = null;
		lineArraySensor = new LineArray();

		swerveController = new SwerveController(pivotMap);
	}

	public static void init () {
		if (instance == null) {
			instance = new Devices();
		}
	}

	public static Hab getHab () {
		return instance.hab;
	}

	public static Intake getIntake () {
		return instance.intake;
	}

	public static Grabber getGrabber () {
		return instance.grabber;
	}

	public static HashMap<Pivot,Vector2> getPivotMap () {
		return instance.pivotMap;
	}

	public static SwerveController getSwerveController () {
		return instance.swerveController;
	}

	public static Limelight getLimelight () {
		return instance.limelight;
	}

	public static DigitalInput getLightSensor () {
		return instance.lightSensor;
	}

	public static ProximitySensor getFrontProx () {
		return instance.proxFront;
	}

	public static ProximitySensor getBackProx () {
		return instance.proxBack;
	}

	public static Controller getDriverController () {
		return instance.driverController;
	}
	
	public static Controller getOperatorController () {
		return instance.operatorController;
	}

	public static Controller getHabController () {
		return instance.habController;
	}

	public static Gyro getGyro () {
		return instance.gyro;
	}

	public static Compressor getCompressor () {
		return instance.compressor;
	}

	public static LineArray getLineArray () {
		return instance.lineArraySensor;
	}

}