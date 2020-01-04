package frc.systems.drive.controllers;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Gyro;
import frc.robot.LineArray;
import frc.robot.Robot;
import frc.utilities.LogUtil;
import frc.utilities.MathUtil;
import frc.utilities.TimingUtil2;
import frc.robot.Controller.Axis;
import frc.systems.drive.controllers.SwerveController;
import frc.systems.drive.controllers.SwerveController.SwerveMode;
import frc.systems.hab.Hab;
import frc.systems.hab.Hab.LiftTarget;

public class LightArrayAlignmentController implements IDriveController {

	private static double angles[] = { 0, 27.5, 90, 152.5, 180, 207.5, 270, 332.5, 360 };

	private enum AlignState {
		GUIDED_STRAFE,
		AUTO_STRAFE,
		FORWARD,
		AUTO_HATCH,
		STOPPED
	}

	private Controller driverController;

	private SwerveController swerveController;

	private PIDController x2Control;
	private PIDController lineArrayPid;
	private double x2Pid;

	private LineArray lineArraySensor;

	private AlignState currentState;
	private AlignState nextState;

	private Gyro gyro;
	private Hab hab;

	private SwerveMode prevSwerveMode;

	private double closestAngle = 0;

	private int count = 0;

	private double lineArrayX1;

	private Runnable onComplete;

	private int centerOnlyCounter;
	private int noSensorCounter;

	public LightArrayAlignmentController (SwerveController swerveController, Runnable onComplete) {
		this.swerveController = swerveController;
		lineArraySensor = Devices.getLineArray();
		lineArrayX1 = 0.0;
		driverController = Devices.getDriverController();
		gyro = Devices.getGyro();
		hab = Devices.getHab();
		this.onComplete = onComplete;

		/********************************************************************/
		/* PID X2 */

		PIDSource x2Source = new PIDSource() {

			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
			}

			@Override
			public double pidGet() {
				double yaw = gyro.getYaw();
				closestAngle = MathUtil.findClosestNumber(angles, yaw);
				return -(yaw - closestAngle);
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

		x2Control = new PIDController(0.01, 0, 0, x2Source, x2Output);
		x2Control.disable();

		lineArrayPid = new PIDController(.035, 0.0, 0.0, new PIDSource() { // .05
		
			@Override public void setPIDSourceType(PIDSourceType pidSource) { }
			@Override public PIDSourceType getPIDSourceType() { return PIDSourceType.kDisplacement; }
		
			@Override
			public double pidGet() {  
				return -lineArraySensor.getNormalized();
			}
		
		}, (double output) -> {
			lineArrayX1 = output;
		});
		lineArrayPid.disable();

		centerOnlyCounter = 0;

	}

	public AlignState getAlignState() {
		return currentState;
	}

	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activating");
		currentState = null;
		nextState = AlignState.GUIDED_STRAFE;
		prevSwerveMode = swerveController.getSwerveMode();
	}

	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivating");
		nextState = AlignState.GUIDED_STRAFE;
		x2Control.disable();
		lineArrayPid.disable();
		swerveController.setSwerveMode(prevSwerveMode);
	}

	@Override
	public void update() {

		if (++count % 200 == 0) {
			LogUtil.log(getClass(), "Closest angle: " + closestAngle);
			count = 0;
		}

		boolean onChange = (nextState != currentState);
		currentState = nextState;

		if (onChange) {
			LogUtil.log(getClass(), "State update: " + currentState.toString());
		}

		switch (currentState) {

			case GUIDED_STRAFE: {
				if (onChange) {
					swerveController.drive(0.0, 0.0, 0.0);
					x2Control.enable();
				}
				if (Double.isNaN(lineArraySensor.getNormalized())) {
					swerveController.drive(
						0.3 * driverController.getAxis(Axis.LX), 
						0.3 * driverController.getAxis(Axis.LY), 
						x2Pid);
				} else {
					nextState = AlignState.AUTO_STRAFE;
				}
			} break;

			case AUTO_STRAFE: {
				if (onChange) {
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
					lineArrayPid.enable();
					centerOnlyCounter = 0;
					noSensorCounter = 0;
				}

				swerveController.drive(lineArrayX1, 0.0, 0.0);

				// if (Robot.wasCargoLastAction()) {
					
				// 	if (lineArraySensor.cargoAlign()) {
				// 		centerOnlyCounter++;
				// 	} 
				// 	else {
				// 		centerOnlyCounter = 0;
				// 	}

				// }
				
					if (lineArraySensor.centerOnly()) {
						centerOnlyCounter++;
					} 
					else {
						centerOnlyCounter = 0;
					}

				if (centerOnlyCounter == 3) { nextState = AlignState.FORWARD; }
				
				if (lineArraySensor.noSensors()) {
					noSensorCounter++;
				}
				else {
					noSensorCounter = 0;
				}
				
				if (noSensorCounter == 3) {	nextState = AlignState.GUIDED_STRAFE; }

			} break;

			case FORWARD: {
				long forwardTime = 800;
				double forwardSpeed = 0.2;
				if (onChange) {
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
					if (Devices.getHab().getLiftTarget() == LiftTarget.L3_CARGO) {
						LogUtil.log(getClass(), "Increasing driveForward time for L3");
						forwardTime = 1000;
						forwardSpeed = 0.2;
					} else if (closestAngle == 28.75 || closestAngle == 151.25 || closestAngle == 208.75 || closestAngle == 331.25) {
						forwardTime = 800;
						forwardSpeed = 0.2;
					}
					TimingUtil2.registerOneTimeCallback(forwardTime, () ->{
						if (Robot.wasCargoLastAction()) {
							nextState = AlignState.STOPPED;
						} else {
							nextState = AlignState.AUTO_HATCH;
						}
					});
				}
				swerveController.drive(lineArrayX1, forwardSpeed, x2Pid);
			} break;

			case AUTO_HATCH: {
				if (hab.isLiftAtTarget()) {
					double yaw = gyro.getYaw();
					closestAngle = MathUtil.findClosestNumber(angles, yaw);
					if (closestAngle == 180) {
						LogUtil.log(getClass(), "Grabbing");
						Devices.getGrabber().timedDeployAndGrab();
						nextState = AlignState.STOPPED;
					} else {
						LogUtil.log(getClass(), "Releasing");
						Devices.getGrabber().timedDeployAndRelease();
						nextState = AlignState.STOPPED;						
					}
				}
				swerveController.drive(0.0, 0.0, 0.0);
			} break;

			case STOPPED: {
				if (onChange) {		
					swerveController.drive(0.0, 0.0, 0.0);
					Devices.getIntake().ejectGrabber(0.0);
					onComplete.run();
				}
			} break;
			
		}
	}
}