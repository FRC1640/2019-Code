package frc.systems.drive.controllers;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import frc.robot.Devices;
import frc.robot.Gyro;
import frc.systems.drive.controllers.SwerveController.SwerveMode;
import frc.systems.vision.Limelight;
import frc.systems.vision.Limelight.LedEnum;
import frc.systems.vision.Limelight.TargetEnum;
import frc.utilities.LogUtil;
import frc.utilities.TimingUtil2;

public class BackRocketToFeederAutoController implements IDriveController {
	/**
	* Enum for each auton sequence
	*/
	private enum AutoState {
		/**
		* Moves the robot away from the rocket at a given speed
		*/
		STRAFE,
		/**
		* Moves the robot a little more and rotates towards the loading station
		*/
		MOVE_AND_ROTATE,
		/**
		* Drives the robot towards the loading station using the Limelight
		*/
		DRIVE_TO_TARGET,
		/**
		* Ends the sequence 
		*/
		DONE
	};

	private SwerveController swerveController;
	private Gyro gyro;
	private Limelight limelight;

	private AutoState currentState, nextState;
	private SwerveMode prevSwerveMode;

	private PIDController gyroPid, cameraPid;

	private double gyroX2, cameraX2;
	private double targetDriveAngleD;
	private double targetGyroAngleD;
	private double strafeSpeed;
	
	private Runnable onComplete;

	public BackRocketToFeederAutoController (SwerveController sc, Runnable onComplete) {
		swerveController = sc;
		targetDriveAngleD = 0.0;
		targetGyroAngleD = 0.0;
		strafeSpeed = 0.0;
		gyro = Devices.getGyro();
		limelight = Devices.getLimelight();
		this.onComplete = onComplete;
		/**
		* Rotational PID
		*/
		gyroPid = new PIDController(1.0, 0.01, 0.0, new PIDSource() {
		
			@Override public void setPIDSourceType(PIDSourceType pidSource) { }

			@Override public PIDSourceType getPIDSourceType() { return PIDSourceType.kDisplacement; }
		
			@Override
			public double pidGet() {
				double dAngleD = targetGyroAngleD - gyro.getYaw();
				double sin = Math.sin(Math.toRadians(dAngleD));
				return sin;
			}

		}, (double output) -> {
			gyroX2 = output;
		});
		gyroPid.disable();
		/**
		* LimeLight PID
		*/
		cameraPid = new PIDController(0.01, 0.0, 0.0, new PIDSource() {
		
			@Override public void setPIDSourceType(PIDSourceType pidSource) { }

			@Override public PIDSourceType getPIDSourceType() { return PIDSourceType.kDisplacement; }
		
			@Override
			public double pidGet() {
				return -limelight.getTargetX(0.0);
			}
		
		}, (double output) -> {
			cameraX2 = output;
		});
		cameraPid.disable();
	}
	/**
	* Activates the objects in the class
	*/
	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activating");
		prevSwerveMode = swerveController.getSwerveMode();
		currentState = null;
		nextState = AutoState.STRAFE;
		gyroX2 = 0.0;
		cameraX2 = 0.0;
		/**
		* If the yaw angle is that of the right side rocket, set the pivot drive angle to 300, set the gyro angle to 180, and set the strafe speed to 1
		*/
		if (Math.abs(gyro.getYaw() -  208.75) < Math.abs(gyro.getYaw() - 151.25)) {
			LogUtil.log(getClass(), "Right side");
			targetDriveAngleD = 300.0;
			targetGyroAngleD = 180.0;
			strafeSpeed = 1.0;
		/**
		* If the yaw angle is that of the left side rocket, set the pivot drive angle to 240, set the gyro angle to 180, and set the strafe speed to -1
		*/
		} else {
			LogUtil.log(getClass(), "Left side");
			targetDriveAngleD = 240.0;
			targetGyroAngleD = 180.0;
			strafeSpeed = -1.0;
		}
		LogUtil.log(getClass(), "Target drive angle: " + targetDriveAngleD);
		LogUtil.log(getClass(), "Target gyro angle: " + targetGyroAngleD);
		limelight.setTargetMode(TargetEnum.VISION_TAPE);
		limelight.setProcessing(true);
	}
	/**
	* Deactivates the objects in the class
	*/
	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivating");
		swerveController.setSwerveMode(prevSwerveMode);
		gyroPid.disable();
		cameraPid.disable();
		limelight.setLEDOn(LedEnum.FORCE_OFF);
	}

	@Override
	public void update() {

		boolean onChange = (nextState != currentState);
		currentState = nextState;

		if (onChange) {
			LogUtil.log(getClass(), "NextState: " + currentState.toString());
		}
		/**
		* Auton sequence
		*/
		switch (currentState) {
			
			case STRAFE: {
				/**
				* Strafes in robot centric at 60% speed for 600 millisconds, then switches to the next state
				*/
				if (onChange) {
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
					TimingUtil2.registerOneTimeCallback(600, () -> { nextState = AutoState.MOVE_AND_ROTATE; });
				}
				swerveController.drive(strafeSpeed, -0.6, 0);
			} break;

			case MOVE_AND_ROTATE: {
				/**
				* Drives in robot centric until the current angle is within 10 degrees of the target angle, then switches to the next state
				*/
				if (onChange) {
					swerveController.setSwerveMode(SwerveMode.FIELD_CENTRIC);
					gyroPid.reset();
					gyroPid.enable();
				}
				swerveController.drivePolar(1.0, targetDriveAngleD, gyroX2);
				if (Math.abs(gyro.getYaw() - targetGyroAngleD) < 10) { nextState = AutoState.DRIVE_TO_TARGET; }
			} break;

			case DRIVE_TO_TARGET: {
				/**
				* Drives forward in robot centric until the target area of the loading station is greater than 2.5
				*/
				if (onChange) {
					limelight.setLEDOn(LedEnum.FORCE_ON);
					gyroPid.disable();
					cameraPid.reset();
					cameraPid.enable();
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
				}
				swerveController.drive(0.0, 0.75, cameraX2);
				if (limelight.getTargetArea() > 2.5) {
					nextState = AutoState.DONE;
				}
			} break;

			case DONE: {
				/**
				* Stops driving and turns the Limelight's LEDs off
				*/
				if (onChange) {
					cameraPid.disable();
					limelight.setLEDOn(LedEnum.FORCE_OFF);
					onComplete.run();
				}
				swerveController.drive(0.0, 0.0, 0.0);
			} break;
		}

	}

}