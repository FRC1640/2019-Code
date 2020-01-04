// package frc.systems.vision;

// import java.util.HashMap;
// import java.util.Map.Entry;

// import edu.wpi.first.wpilibj.PIDController;
// import edu.wpi.first.wpilibj.PIDOutput;
// import edu.wpi.first.wpilibj.PIDSource;
// import edu.wpi.first.wpilibj.PIDSourceType;
// import frc.robot.Controller;
// import frc.robot.Devices;
// import frc.robot.Robot;
// import frc.systems.vision.Limelight;
// import frc.utilities.MathUtil;
// import frc.utilities.Vector2;
// import frc.systems.vision.Limelight.LedEnum;
// import frc.robot.Sensors;
// import frc.robot.Controller.Axis;
// import frc.robot.Controller.Button;
// import frc.robot.Controller.ButtonEvent;
// import frc.robot.Robot.AlignState;
// import frc.robot.Robot.CVTMode;
// import frc.systems.RobotSystem;
// import frc.systems.drive.controllers.OldCvtController;
// import frc.systems.drive.controllers.SwerveController;
// import frc.systems.drive.controllers.SwerveController.SwerveMode;
// import frc.systems.drive.pivot.Pivot;
// import frc.systems.drive.pivot.CVTPivot;

// public class AlignSystem extends RobotSystem {

// 	private Controller driverController;

// 	private SwerveController swerveController;
// 	private HashMap<Pivot, Vector2> pivotMap;

// 	private PIDController x1Control;
// 	private double x1Pid;
// 	private PIDController x2Control;
// 	private double x2Pid;

// 	private Sensors sensorInstance;
// 	private Limelight limelight;

// 	private AlignState currentState;

// 	private static double angles[] = { -180, -150, -90, -30, 0, 30, 90, 150, 180 };

// 	public AlignSystem() {
// 		super("Vision Alignment System");
// 	}

// 	@Override
// 	public void init() {
// 		currentState = null;

// 		sensorInstance = Sensors.getInstance();
// 		driverController = Controller.getController(0);

// 		limelight = Devices.getLimelight();
// 		limelight.setProcressing(true);
// 		limelight.setLEDOn(LedEnum.PIPELINE);

// 		pivotMap = Devices.getPivotMap();

// 		swerveController = Devices.getSwerveController();
// 		swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);

// 		/********************************************************************/
// 		/* PID X1 */

// 		PIDSource x1Source = new PIDSource() {

// 			@Override
// 			public void setPIDSourceType(PIDSourceType pidSource) {
// 			}

// 			@Override
// 			public double pidGet() {
// 				return -limelight.getTargetX(0.0);
// 			}

// 			@Override
// 			public PIDSourceType getPIDSourceType() {
// 				return PIDSourceType.kDisplacement;
// 			}
// 		};

// 		PIDOutput x1Output = new PIDOutput() {

// 			@Override
// 			public void pidWrite(double output) {
// 				x1Pid = output;
// 			}
// 		};

// 		x1Control = new PIDController(0.05, 0.0, 0, x1Source, x1Output);

// 		/********************************************************************/
// 		/* PID X2 */

// 		PIDSource x2Source = new PIDSource() {

// 			@Override
// 			public void setPIDSourceType(PIDSourceType pidSource) {
// 			}

// 			@Override
// 			public double pidGet() {
// 				return -(sensorInstance.getYaw() - MathUtil.findClosestNumber(angles, sensorInstance.getYaw()));
// 			}

// 			@Override
// 			public PIDSourceType getPIDSourceType() {
// 				return PIDSourceType.kDisplacement;
// 			}
// 		};

// 		PIDOutput x2Output = new PIDOutput() {

// 			@Override
// 			public void pidWrite(double output) {
// 				x2Pid = output;
// 			}
// 		};

// 		x2Control = new PIDController(0.01, 0.0, 0, x2Source, x2Output);

// 		/********************************************************************/

// 		driverController.registerButtonListener(ButtonEvent.PRESS, Button.START, () -> {
// 			sensorInstance.resetGyro();
// 		});
// 		driverController.registerButtonListener(ButtonEvent.PRESS, Button.SELECT, () -> {
// 			swerveController.toggleFieldCentric();
// 		});
// 		driverController.registerButtonListener(ButtonEvent.RELEASE, Button.B, () -> {
// 			Robot.setAlignState(AlignState.INACTIVE);
// 		});
// 	}

// 	@Override
// 	public void preStateUpdate() {

// 	}

// 	@Override
// 	public void postStateUpdate() {

// 	}

// 	@Override
// 	public void disabledInit() {
// 	}

// 	@Override
// 	public void disabledUpdate() {
// 		x2Control.disable();
// 		x1Control.disable();
// 	}

// 	@Override
// 	public void autonInit() {

// 	}

// 	@Override
// 	public void autonUpdate() {

// 	}

// 	@Override
// 	public void teleopInit() {
// 	}

// 	@Override
// 	public void teleopUpdate() {

// 		double y1 = 0.0;
// 		double x1 = 0.0;
// 		double x2 = 0.0;

// 		if (driverController.getButton(Button.B)) {
// 			Robot.setAlignState(AlignState.INACTIVE);
// 		}

// 		boolean onChange = (Robot.getAlignState() != currentState);
// 		currentState = Robot.getAlignState();

// 		switch (currentState) {
// 			case INACTIVE: {
// 				if (onChange) {
// 					limelight.setLEDOn(LedEnum.FORCE_ON);
// 					x1Control.disable();
// 					x2Control.disable();
// 				}

// 				if (driverController.getButton(Button.Y)) {
// 					Robot.setAlignState(AlignState.ROTATE);
// 				}
// 			}
// 			break;
// 			case ROTATE: {
// 				if (onChange) {
// 					limelight.setLEDOn(LedEnum.FORCE_ON);
// 					x2Control.enable();
// 				}

// 				y1 = driverController.getAxis(Axis.LY);
// 				x1 = driverController.getAxis(Axis.LX);
// 				x2 = x2Pid;
// 				swerveController.drive(x1, y1, x2);

// 				if (driverController.getButton(Button.RT)) {
// 					Robot.setAlignState(AlignState.STRAFE);
// 				}
// 			}
// 			break;
// 			case STRAFE: {
// 				if (onChange) {
// 					limelight.setLEDOn(LedEnum.FORCE_ON);
// 					x2Control.disable();
// 					x1Control.enable();
// 				}
// 				y1 = driverController.getAxis(Axis.LY);
// 				x1 = x1Pid;
// 				swerveController.drive(x1, y1, x2);

// 				if (driverController.getButton(Button.RB)) {
// 					Robot.setAlignState(AlignState.DRIVE);
// 				}
// 			}
// 			break;
// 			case DRIVE: {
// 				if (onChange) {
// 					x2Control.enable();
// 					x1Control.disable();
// 					limelight.setLEDOn(LedEnum.FORCE_ON);
// 					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
// 				}

// 				y1 = driverController.getAxis(Axis.LY);
// 				x2 = x2Pid;
// 				x1 = 0.0;
// 				swerveController.drive(x1, y1, x2);
// 			}
// 			break;
// 		}
// 	}

// 	@Override
// 	public void testInit() {

// 	}

// 	@Override
// 	public void testUpdate() {

// 	}

// 	@Override
// 	public void disable() {
// 		for (Pivot piv : pivotMap.keySet()) {
// 			piv.disable();
// 		}
// 	}

// 	@Override
// 	public void enable() {
// 		for (Pivot piv : pivotMap.keySet()) {
// 			piv.enable();
// 		}
// 	}
// }