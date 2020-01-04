// package frc.systems.hab;

// import java.util.HashMap;

// import edu.wpi.first.networktables.NetworkTableEntry;
// import edu.wpi.first.networktables.NetworkTableInstance;
// import frc.robot.Controller;
// import frc.robot.Devices;
// import frc.robot.Controller.Button;
// import frc.robot.Controller.ButtonEvent;
// import frc.systems.RobotSystem;
// import frc.systems.grabber.Grabber;
// import frc.systems.lift.controllers.LiftManualController;
// import frc.systems.hab.LiftNormalController;
// import frc.utilities.ProximitySensor;
// import frc.utilities.TimingUtil;
// import frc.utilities.TimingUtil2;
// import frc.utilities.ProximitySensor.UnitEnum;

// public class HabSystem extends RobotSystem {
// 	/**
// 	* Enum for the hab controllers
// 	*/
// 	public static enum HabController { 
// 		/**
// 		* Hab controller that deals with manually controlling the hab
// 		*/
// 		MANUAL_HAB, 
// 		/**
// 		* Hab controller that deals with automatically controlling the hab
// 		*/
// 		PID_TEST, 
// 		/**
// 		* Hab controller that deals with automatically controlling the lift
// 		*/
// 		LIFT, 
// 		/**
// 		* Hab controller that deals with manually controlling the lift
// 		*/
// 		MANUAL_LIFT 
// 	};

// 	private Controller opController;
// 	private Controller habController;

// 	private Hab hab;


// 	private int count;

// 	private IHabController activeHabController;
// 	private IHabController nextHabController;
// 	private HashMap<HabController,IHabController> habControllerMap;

// 	private NetworkTableEntry habStateEntry;

// 	private ProximitySensor proxFront;
// 	private ProximitySensor proxBack;

// 	private NetworkTableEntry proxFrontEntry;
// 	private NetworkTableEntry proxBackEntry; 
// 	private Grabber grabber;

// 	// private Limelight limelight;

// 	public HabSystem () {
// 		super("Hab System");
// 	}

// 	@Override
// 	public void init() {

// 		count = 0;

// 		opController = Devices.getOperatorController();
// 		habController = Devices.getHabController();
// 		grabber = Devices.getGrabber();

// 		proxFront = Devices.getFrontProx();
// 		proxBack = Devices.getBackProx();

// 		proxFrontEntry = NetworkTableInstance.getDefault().getTable("SmartDashboard").getEntry("proxFront");
// 		proxBackEntry = NetworkTableInstance.getDefault().getTable("SmartDashboard").getEntry("proxBack");

		
// 		habStateEntry = NetworkTableInstance.getDefault().getTable("SmartDashboard").getEntry("habState");

// 		// Get the devices
// 		hab = Devices.getHab();
// 		/**
// 		* Sets dog to lift mode and engages hab brakes
// 		*/
// 		hab.engageFrontBrake();
// 		hab.engageRearBrake();
// 		hab.setDogLiftMode();
// 		/**
// 		* HashMap that assigns each hab controller to the enum
// 		*/
// 		habControllerMap = new HashMap<>();
// 		habControllerMap.put(HabController.MANUAL_HAB, new HabManualController(hab));
// 		habControllerMap.put(HabController.PID_TEST, new HabPidController(hab, Devices.getSwerveController()));
// 		habControllerMap.put(HabController.LIFT, new LiftNormalController());
// 		habControllerMap.put(HabController.MANUAL_LIFT, new LiftManualController());
// 		/**
// 		* If RB pressed, switch hab controller to automatic hab
// 		*/
// 		habController.registerButtonListener(ButtonEvent.PRESS, Button.RB, () -> {
// 			callback("switch to pid hab control");
// 			nextHabController = habControllerMap.get(HabController.PID_TEST);
// 		});
// 		/**
// 		* If LB pressed, switch hab controller to manual hab
// 		*/
// 		habController.registerButtonListener(ButtonEvent.PRESS, Button.LB, () -> {
// 			callback("switch to manual hab control");
// 			nextHabController = habControllerMap.get(HabController.MANUAL_HAB);
// 		});
// 		/**
// 		* If select button pressed, switch hab controller to automatic lift
// 		*/
// 		habController.registerButtonListener(ButtonEvent.PRESS, Button.SELECT, () -> {
// 			callback("switch to pid lift control");
// 			nextHabController = habControllerMap.get(HabController.LIFT);
// 		});
// 		/**
// 		* If start button pressed, switch hab controller to manual lift
// 		*/
// 		opController.registerButtonListener(ButtonEvent.PRESS, Button.START, () -> {
// 			callback("switch to manual lift control");
// 			nextHabController = habControllerMap.get(HabController.MANUAL_LIFT);
// 		});
// 		/**
// 		* If start button released, switch hab controller to automatic lift
// 		*/
// 		opController.registerButtonListener(ButtonEvent.RELEASE, Button.START, () -> {
// 			callback("switch to pid lift controller");
// 			nextHabController = habControllerMap.get(HabController.LIFT);
// 		});

// 	}

// 	@Override
// 	public void preStateUpdate() throws Exception {

// 	}

// 	@Override
// 	public void postStateUpdate() throws Exception {

// 	}

// 	@Override
// 	public void disabledInit() throws Exception {

// 	}

// 	@Override
// 	public void disabledUpdate() throws Exception {

// 	}

// 	@Override
// 	public void autonInit() throws Exception {
// 		teleopInit();
// 	}

// 	@Override
// 	public void autonUpdate() throws Exception {

// 	}

// 	@Override
// 	public void teleopInit() throws Exception {
// 		/**
// 		* Set default hab controller to automatic lift
// 		*/
// 		nextHabController = habControllerMap.get(HabController.LIFT);
// 	}

// 	@Override
// 	public void teleopUpdate() throws Exception {

// 		if (++count % 50 == 0) {
// 			proxFrontEntry.setBoolean(proxFront.getDistance(UnitEnum.INCHES) < 5);
// 			proxBackEntry.setBoolean(proxBack.getDistance(UnitEnum.INCHES) < 5);
// 			count = 0;
// 		}
// 		/**
// 		* If the lift gets too low, automatically retract and close the hatch grabber
// 		*/
// 		if (hab.getLiftHeightInCounts() < 10000) {
// 			grabber.retract();
// 			grabber.close();
// 		}
// 		/**
// 		* If the next hab controller is not the active hab controller AND the active hab controller is not null, deactivate the active hab controller
// 		* If the next hab controller is not the active hab controller, activate the active hab controller
// 		*/
// 		if (nextHabController != activeHabController) {
// 			if (activeHabController != null) {
// 				activeHabController.deactivate();
// 				habStateEntry.setBoolean(false);
// 			}
// 			activeHabController = nextHabController;
// 			activeHabController.activate();
// 			habStateEntry.setBoolean(true);

// 		}

// 		activeHabController.update();

// 	}

// 	@Override
// 	public void testInit() throws Exception {

// 	}

// 	@Override
// 	public void testUpdate() throws Exception {

// 	}

// 	@Override
// 	public void enable() throws Exception {

// 	}

// 	@Override
// 	public void disable() throws Exception {

// 	}

// }