package frc.systems.drive;

import java.util.HashMap;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Button;
import frc.robot.Controller.Axis;
import frc.robot.Controller.ButtonEvent;
import frc.systems.drive.controllers.LightArrayAlignmentController;
import frc.systems.drive.controllers.LimelightAlignmentAutoController;
import frc.systems.drive.controllers.NormalDriveController;
import frc.systems.RobotSystem;
import frc.systems.drive.controllers.BackRocketToFeederAutoController;
import frc.systems.drive.controllers.CargoToFeederAutoController;
import frc.systems.drive.controllers.IDriveController;
import frc.systems.drive.controllers.OperatorDriveController;
import frc.systems.drive.controllers.SwerveController;
import frc.systems.drive.controllers.TestCVTController;
import frc.systems.drive.controllers.SwerveController.CvtMode;
import frc.systems.drive.controllers.SwerveController.SwerveMode;

public class DriveSystem extends RobotSystem {

	public static enum DriveController { NORMAL, OPERATOR, AUTON_CARGO_TO_FEEDER, AUTON_ROCKET_TO_FEEDER, LIMELIGHT_ALIGN, LIGHT_ALIGN, TEST; }

	private SwerveController swerveController;
	private Controller driverController;
	private Controller opController;
	private HashMap<DriveController,IDriveController> driveControllerMap;

	// private HashMap<Pivot,Vector2> pivotMap;

	private IDriveController activeDriveController;
	private DriveController currentDriveController;
	private DriveController nextDriveController;

	public DriveSystem () {
		super("Drive System");
		swerveController = Devices.getSwerveController();
	}

	@Override
	public void init () {
		driverController = Devices.getDriverController();
		// opController = Devices.getOperatorController();

		Runnable returnToNormalController = () -> {
			callback("State machine complete, returning control to Normal.");
			nextDriveController = DriveController.TEST;
		};

		driveControllerMap = new HashMap<>();
		driveControllerMap.put(DriveController.NORMAL, new NormalDriveController(swerveController));
		// driveControllerMap.put(DriveController.OPERATOR, new OperatorDriveController(swerveController));
		// driveControllerMap.put(DriveController.AUTON_CARGO_TO_FEEDER, new CargoToFeederAutoController(swerveController, returnToNormalController));
		// driveControllerMap.put(DriveController.LIMELIGHT_ALIGN, new LimelightAlignmentAutoController(swerveController, returnToNormalController));
		// driveControllerMap.put(DriveController.LIGHT_ALIGN, new LightArrayAlignmentController(swerveController, returnToNormalController));
		driveControllerMap.put(DriveController.AUTON_ROCKET_TO_FEEDER, new BackRocketToFeederAutoController(swerveController, returnToNormalController));
		try {
			// driveControllerMap.put(DriveController.TEST, new TestCVTController());
		} catch (Exception e) {
			e.printStackTrace();
		}

		currentDriveController = null;

		/*
		 * DRIVER CONTROLS
		 */
		driverController.registerButtonListener(ButtonEvent.PRESS, Button.START, () -> {
			callback("reset gyro");
			Devices.getGyro().resetGyro(); 
		});
		
		driverController.registerButtonListener(ButtonEvent.PRESS, Button.Y, () -> {
			callback("switch to light align mode...");
			if (nextDriveController == DriveController.NORMAL) {
				nextDriveController = DriveController.TEST;
			}
			else {
				nextDriveController = DriveController.NORMAL;
			}
		});

		// driverController.registerButtonListener(ButtonEvent.RELEASE, Button.Y, () -> {
		// 	if (currentDriveController == DriveController.LIGHT_ALIGN) {
		// 		callback("switch back to normal mode...");
		// 		nextDriveController = DriveController.NORMAL;
		// 	}
		// });

		// driverController.registerButtonListener(ButtonEvent.PRESS, Button.LJ, () -> {
		// 	callback("switch to limelight align mode...");
		// 	nextDriveController = DriveController.LIMELIGHT_ALIGN;
		// });

		// driverController.registerButtonListener(ButtonEvent.RELEASE, Button.LJ, () -> {
		// 	if (currentDriveController == DriveController.LIMELIGHT_ALIGN) {
		// 		callback("switch back to normal mode...");
		// 		nextDriveController = DriveController.NORMAL;
		// 	}
		// });

		driverController.registerButtonListener(ButtonEvent.PRESS, Button.X, () -> {
			if (currentDriveController == DriveController.OPERATOR) {
				callback("switch to normal drive mode...");
				nextDriveController = DriveController.NORMAL;
			}
		});


		/*
		 * OPERATOR CONTROLS
		 */
		// opController.registerButtonListener(ButtonEvent.PRESS, Button.X, () -> {
		// 	if (currentDriveController == DriveController.NORMAL) {
		// 		callback("enter operator precision mode...");
		// 		nextDriveController = DriveController.OPERATOR;
		// 	} else if (currentDriveController == DriveController.OPERATOR) {
		// 		callback("leave operator precision mode...");
		// 		nextDriveController = DriveController.NORMAL;
		// 	}
		// });

		// opController.registerButtonListener(ButtonEvent.PRESS, Button.Y, () -> {
		// 	callback("Switch to auton mode");

		// 	double angleD = Devices.getGyro().getYaw();
		// 	double minRocket = Math.min(Math.abs(angleD - 208.75), Math.abs(angleD - 151.25));
		// 	double minCargo  = Math.min(Math.abs(angleD - 90), Math.abs(angleD - 270));

		// 	if (minRocket < minCargo) {
		// 		nextDriveController = DriveController.AUTON_ROCKET_TO_FEEDER;
		// 	} else {
		// 		nextDriveController = DriveController.AUTON_CARGO_TO_FEEDER;
		// 	}
		// });

		// opController.registerButtonListener(ButtonEvent.RELEASE, Button.Y, () -> {
		// 	if (currentDriveController == DriveController.AUTON_CARGO_TO_FEEDER || currentDriveController == DriveController.AUTON_ROCKET_TO_FEEDER) {
		// 		callback("Switch out of auton mode...");
		// 		nextDriveController = DriveController.NORMAL;
		// 	}
		// });
		
	}

	@Override
	public void disable () {
		swerveController.disable();	
	}

	@Override
	public void enable () {
		swerveController.enable();
	}

	@Override public void preStateUpdate () { }

	@Override public void postStateUpdate () { }

	@Override public void disabledInit () {
		driverController.vibrate(RumbleType.kLeftRumble, 0.0);
		driverController.vibrate(RumbleType.kRightRumble, 0.0);
		if (currentDriveController != null) {
			driveControllerMap.get(currentDriveController).deactivate();
			currentDriveController = null;
		}
	}

	@Override public void disabledUpdate () { }

	@Override public void autonInit () {
		swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
		swerveController.setCVTMode(CvtMode.SANDSTORM);
		currentDriveController = null;
		nextDriveController = DriveController.NORMAL;
		Devices.getGyro().resetGyro();
	}

	@Override public void autonUpdate () { }

	@Override public void teleopInit () {
		if (currentDriveController != null) { driveControllerMap.get(currentDriveController).deactivate(); }
		swerveController.setSwerveMode(SwerveMode.FIELD_CENTRIC);
		swerveController.setCVTMode(CvtMode.SHIFTING);
		currentDriveController = null;
		nextDriveController = DriveController.NORMAL;
	}

	@Override
	public void teleopUpdate () {
		CvtMode cvtMode = swerveController.getCVTMode();
		SwerveMode swerveMode = swerveController.getSwerveMode();

		/* *********** RUMBLE *********** */

		boolean rumbleDriver = (swerveMode == SwerveMode.ROBOT_CENTRIC) && (currentDriveController != DriveController.OPERATOR);
		boolean rumbleOperator = currentDriveController == DriveController.OPERATOR;

		driverController.vibrate(RumbleType.kLeftRumble, (rumbleDriver) ? 0.1 : 0.0);
		driverController.vibrate(RumbleType.kRightRumble, (rumbleDriver) ? 0.1 : 0.0);

		/* *********** CONTROLLERS *********** */

		if (nextDriveController != currentDriveController) {
			if (currentDriveController != null) {
				activeDriveController.deactivate();
			}
			currentDriveController = nextDriveController;
			activeDriveController = driveControllerMap.get(currentDriveController);
			activeDriveController.activate();
		}

		activeDriveController.update();
	}

	@Override public void testInit () { }

	@Override public void testUpdate () { }

}
