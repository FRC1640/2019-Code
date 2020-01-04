package frc.systems.drive.controllers;

import java.util.HashMap;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Gyro;
import frc.utilities.MathUtil;
import frc.utilities.TimingUtil2;
import frc.utilities.Vector2;
import frc.robot.Controller.Axis;
import frc.robot.Controller.Button;
import frc.systems.drive.controllers.SwerveController;
import frc.systems.drive.controllers.SwerveController.SwerveMode;
import frc.systems.drive.pivot.Pivot;
import frc.systems.vision.Limelight;
import frc.systems.vision.Limelight.LedEnum;

public class LimelightAlignController {

	private Controller driverController;

	private SwerveController swerveController;

	private PIDController x2Control;
	private double x2Pid;

	private PIDController x1Control;
	private double x1Pid;

	private PIDController x2CenterControl;
	private double x2CenterPid;

	private double forwardStartTime;

	private AlignState currentState;
	private AlignState nextState;

	private SwerveMode prevDriveMode;

	private Gyro sensorInstance;

	private static double angles[] = { -180, -151.25, -90, -28.75, 0, 28.75, 90, 151.25, 180 };

	private double count;

	private Limelight limelight;

	public enum AlignState {
		INACTIVE,
		ROTATE_TO_CENTER,
		DRIVE_UP,
		ROTATE,
		BACKUP,
		FORWARD,
		STRAFE,
		DRIVE,
		STOPPED,
		STRAFEBACK
	}

	public LimelightAlignController(SwerveController swerveController) {
		this.swerveController = swerveController;
		prevDriveMode = swerveController.getSwerveMode();

		driverController = Devices.getDriverController();

		limelight = Devices.getLimelight();
		limelight.setLEDOn(LedEnum.FORCE_OFF);
		sensorInstance = Devices.getGyro();

		currentState = null;
		nextState = AlignState.INACTIVE;

		forwardStartTime = 0;

		count = 0;

		setAlignState(AlignState.INACTIVE);

		/********************************************************************/
		/* PID X1 */

		PIDSource x1Source = new PIDSource() {

			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
			}

			@Override
			public double pidGet() {
				return -limelight.getTargetX(0.0);
			}

			@Override
			public PIDSourceType getPIDSourceType() {
				return PIDSourceType.kDisplacement;
			}
		};

		PIDOutput x1Output = new PIDOutput() {

			@Override
			public void pidWrite(double output) {
				x1Pid = output;
			}
		};

		x1Control = new PIDController(0.05, 0.0, 0, x1Source, x1Output);

		/********************************************************************/
		/* PID X2 */

		PIDSource x2Source = new PIDSource() {

			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
			}

			@Override
			public double pidGet() {
				return -(sensorInstance.getYaw() - MathUtil.findClosestNumber(angles, sensorInstance.getYaw()));
			}

			@Override
			public PIDSourceType getPIDSourceType() {
				return PIDSourceType.kDisplacement;
			}
		};

		PIDOutput x2Output = new PIDOutput() {

			@Override
			public void pidWrite(double output) {
				x2Pid = output;
			}
		};

		x2Control = new PIDController(0.01, 0.0, 0, x2Source, x2Output);

		/********************************************************************/
		/* PID X2 CENTER */

		PIDSource x2CenterSource = new PIDSource() {

			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
			}

			@Override
			public double pidGet() {
				return -limelight.getTargetX(0.0);
			}

			@Override
			public PIDSourceType getPIDSourceType() {
				return PIDSourceType.kDisplacement;
			}
		};

		PIDOutput x2CenterOutput = new PIDOutput() {

			@Override
			public void pidWrite(double output) {
				x2CenterPid = output;
			}
		};

		x2CenterControl = new PIDController(0.01, 0.0, 0, x2CenterSource, x2CenterOutput);



	}

	public SwerveMode update(SwerveMode prevDriveMode) {
		double y1 = 0.0;
		double x1 = 0.0;
		double x2 = 0.0;

		boolean onChange = (nextState != currentState);
		currentState = nextState;

		switch (currentState) {
			// case DRIVE_UP: {
			// 	if (onChange) {
			// 		x1Control.enable();
			// 		x2Control.enable();
			// 		limelight.setLEDOn(LedEnum.PIPELINE);
			// 		limelight.setProcessing(true);
			// 		prevDriveMode = swerveController.getSwerveMode();
			// 		swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
			// 	}

			// 	y1 = 0.3;
			// 	x1 = x1Pid;
			// 	x2 = x2Pid;

			// 	swerveController.drive(x1, y1, x2);

			// 	if (limelight.getTargetArea() < 10) {
			// 		setAlignState(AlignState.STOPPED);
			// 	}
			// }
			// break;
			case ROTATE_TO_CENTER: {
				if (onChange) {
					limelight.setLEDOn(LedEnum.FORCE_ON);
					prevDriveMode = swerveController.getSwerveMode();
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
					x2CenterControl.enable();
				}

				y1 = 0.2;
				x1 = 0.0;
				x2 = x2CenterPid;

				if (limelight.getTargetArea() > 1) {
					y1 = 0.0;
					setAlignState(AlignState.FORWARD);
				}

				swerveController.drive(x1, y1, x2);
			}
			break;
			case FORWARD: {
				if (onChange) {
					prevDriveMode = swerveController.getSwerveMode();
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);	
					forwardStartTime = System.nanoTime();
				}
				
				if (System.nanoTime() <= forwardStartTime + 2e+9) {
					swerveController.drive(0.0, 0.1, 0.0);
				} else {
					setAlignState(AlignState.STOPPED);
				}
			}
			break;
			// case ROTATE: {
			// 	if (onChange) {
			// 		x2Control.enable();
			// 	}

			// 	y1 = 0.0;
			// 	x1 = 0.0;
			// 	x2 = x2Pid;
			// 	swerveController.drive(x1, y1, x2);

			// 	if (Math.abs(sensorInstance.getYaw() - MathUtil.findClosestNumber(angles, sensorInstance.getYaw())) <= 1) {
			// 		setAlignState(AlignState.STRAFE);
			// 	}
			// }
			// break;
			case STRAFE: {
				if (onChange) {
					x1Control.enable();
					x2Control.enable();
					limelight.setLEDOn(LedEnum.FORCE_ON);
					limelight.setProcessing(true);
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
				}
				x1 = x1Pid;
				x2 = x2Pid;
				y1 = 0.4;

				if (limelight.getTargetArea() > 0.8) {
					y1 = 0.0;
				}

				swerveController.drive(x1, y1, x2);
			}
			break;
			case STOPPED: {
				if (onChange) {
					x1Control.disable();
					x2Control.disable();
					limelight.setLEDOn(LedEnum.FORCE_OFF);
				}
			}
			case INACTIVE:
			default: {
				if (onChange) {
					x1Control.disable();
					x2Control.disable();
					x2CenterControl.disable();
					swerveController.setSwerveMode(prevDriveMode);
					limelight.setLEDOn(LedEnum.FORCE_ON);
				}

				if (driverController.getButton(Button.Y)) {
					setAlignState(AlignState.ROTATE_TO_CENTER);
				}
			}
		}

		if (!driverController.getButton(Button.Y)) {
			setAlignState(AlignState.INACTIVE);
		}

		return prevDriveMode;
	}

	public void setAlignState(AlignState state) {
		nextState = state;
	}

	public AlignState getAlignState() {
		return currentState;
	}
}