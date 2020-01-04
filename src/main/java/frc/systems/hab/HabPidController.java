// package frc.systems.hab;

// import java.util.HashMap;

// import edu.wpi.first.wpilibj.PIDController;
// import edu.wpi.first.wpilibj.PIDOutput;
// import edu.wpi.first.wpilibj.PIDSource;
// import edu.wpi.first.wpilibj.PIDSourceType;
// import frc.robot.Controller;
// import frc.robot.Devices;
// import frc.robot.Controller.Button;
// import frc.systems.drive.controllers.SwerveController;
// import frc.systems.drive.pivot.Pivot;
// import frc.utilities.LogUtil;
// import frc.utilities.ProximitySensor;
// import frc.utilities.Vector2;
// import frc.utilities.ProximitySensor.UnitEnum;

// public class HabPidController implements IHabController {
// 	/**
// 	* Enum for each state of the automatic hab sequence
// 	*/
// 	public static enum HabState {
// 		/**
// 		* Starting state for hab 
// 		*/
// 		START,
// 		/**
// 		* Lowers both hab lifts to raise the robot
// 		*/ 
// 		BOTH_LIFTS_DOWN,
// 		/**
// 		* Drives the robot onto the hab
// 		*/
// 		DRIVE_FORWARD_1,
// 		/**
// 		* Slows down after driving onto the hab
// 		*/
// 		DRIVE_FORWARD_SLOWER_1,
// 		/**
// 		* Raises the front hab lift
// 		*/
// 		FRONT_LIFT_UP,
// 		/**
// 		* Drives the robot all the way onto the hab
// 		*/
// 		DRIVE_FORWARD_2,
// 		/**
// 		* Slows down after completely moving onto the hab
// 		*/
// 		DRIVE_FORWARD_SLOWER_2,
// 		/**
// 		* Raises the back hab lift
// 		*/
// 		BACK_LIFT_UP,
// 		/**
// 		* Drives forward once on the hab
// 		*/
// 		DRIVE_FORWARD_SLOWER_3,
// 		/**
// 		* Lifts the robot once on the hab (used for buddy climbs)
// 		*/
// 		LIFT_AND_STOP,
// 		/**
// 		* Applies voltage to hab motors to keep the robot up
// 		*/
// 		HOLD_VOLTAGE_AT_ENDGAME,
// 		/**
// 		* Resets all encoders, stops hab lifts, and returns to the start
// 		*/
// 		RESET;
// 	}
// 	/**
// 	* Enum to set heights for the hab lift
// 	*
// 	* @param height value to set hab height
// 	*/
// 	private static enum HabHeight {
// 		/**
// 		* Hab level two height
// 		*/
// 		L2 (18400),
// 		/**
// 		* Hab level two to level three height
// 		*/
// 		LMID (38000),
// 		/**
// 		* Hab level three height
// 		*/
// 		L3 (44000),
// 		/**
// 		* Lift while on hab three height
// 		*/
// 		LIFT_AND_STOP (21500);

// 		private int height;

// 		private HabHeight (int height) {
// 			this.height = height;
// 		}
// 	}

// 	private Hab hab;
// 	private Controller habController;
// 	private HabState habState;
// 	private HabState nextHabState;
// 	private HashMap<Pivot, Vector2> pivotMap;
// 	private ProximitySensor proxFront;
// 	private ProximitySensor proxBack;
// 	private HabHeight habHeight;
// 	private int count = 0;
// 	private PIDController pid;
// 	private PIDController frontPid;
// 	private PIDController backPid;
// 	private PIDSource pSrc;
// 	private PIDOutput pOut;
// 	private double driveForward1StartTime;
// 	private double driveForward2StartTime;
// 	private double holdCurrent = 0.2;

// 	private SwerveController swerveController;

// 	public HabPidController(Hab hab, SwerveController sc) {
// 		this.hab = hab;
// 		this.swerveController = sc;

// 		habController = Devices.getHabController();

// 		habHeight = HabHeight.L2;
// 		habState = null;
// 		nextHabState = HabState.START;
// 		pivotMap = Devices.getPivotMap();

// 		proxFront = Devices.getFrontProx();
// 		proxBack = Devices.getBackProx();

// 		driveForward1StartTime = 0;
// 		driveForward2StartTime = 0;

// 		pSrc = new PIDSource() {

// 			@Override public void setPIDSourceType(PIDSourceType pidSource) { }

// 			@Override public PIDSourceType getPIDSourceType() { return PIDSourceType.kDisplacement; }

// 			@Override
// 			public double pidGet() {
// 				double dif = -(hab.getFrontEncoderCounts() - hab.getRearEncoderCounts());
// 				return dif;
// 			}

// 		};

// 		pOut = new PIDOutput () {

// 			@Override
// 			public void pidWrite(double output) {
// 				hab.setRearLiftSpeed(output, false);
				
// 			}

// 		};
// 		/**
// 		* PID to set the back hab height to the frot hab height
// 		*/
// 		pid = new PIDController(3.5e-4, 0, 1e-3, 0.0, pSrc, pOut, 0.02);
// 		pid.disable();
// 	}
// 	/**
// 	* Drives all four pivots forward at a given speed
// 	*
// 	* @param speed accepts a speed from (-1, 1)
// 	*/
// 	private void drivePivots (double speed) {
// 		for (Pivot p : pivotMap.keySet()) {
// 			p.setTargetAngleD(90);
// 			p.setSpeed(speed);
// 		}
// 	}
// 	/**
// 	* Activates the objects in the class
// 	*/
// 	@Override
// 	public void activate () {
// 		LogUtil.log(getClass(), "Activate");
// 		/**
// 		* Enables the pivots and point them forward
// 		*/
// 		for (Pivot p : pivotMap.keySet()) {
// 			p.enable();
// 			p.setTargetAngleD(90);
// 		}
// 		/**
// 		* Sets the hab lifts to a speed of 0
// 		*/
// 		hab.setFrontLiftSpeed(0.0, false);
// 		hab.setRearLiftSpeed(0.0, false);
// 		/**
// 		* Disengages the hab brakes and sets the dog to hab mode
// 		*/
// 		hab.disengageFrontBrake();
// 		hab.disengageRearBrake();
// 		hab.setDogHabMode();
// 	}
// 	/**
// 	* Deactivates the objects in the class
// 	*/
// 	@Override
// 	public void deactivate () {
// 		LogUtil.log(getClass(), "Deactivate");
// 		/**
// 		* Disables the pivots
// 		*/
// 		for (Pivot p : pivotMap.keySet()) {
// 			p.disable();
// 		}
// 		/**
// 		* Sets the hab lifts to a speed of 0
// 		*/
// 		hab.setFrontLiftSpeed(0.0, false);
// 		hab.setRearLiftSpeed(0.0, false);
// 		/**
// 		* Engages the hab brakes and sets the dog to lift mode
// 		*/
// 		hab.engageFrontBrake();
// 		hab.engageRearBrake();
// 		hab.setDogLiftMode();
// 		/**
// 		* Disables and resets the PID
// 		*/
// 		pid.disable();
// 		pid.reset();
// 	}

// 	@Override
// 	public void update () {
// 		if (++count % 200 == 0) {
// 			LogUtil.log(getClass(), "PID Test Update");
// 			count = 0;
// 		}

// 		boolean onChange = (nextHabState != habState);
// 		habState = nextHabState;

// 		switch (habState) {
// 			case START: {
// 				if (onChange) {
// 					pid.disable();
// 					pid.reset();
// 				}
// 				/**
// 				* Resets the hab encoders
// 				*/
// 				hab.zeroHabEncoderCounts();
// 				/**
// 				* Sets the dog to hab mode and engages the hab brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.engageFrontBrake();
// 				hab.engageRearBrake();
// 				/**
// 				* Sets the hab drive and lifts to a speed of 0
// 				*/
// 				hab.setFrontLiftSpeed(0.0, false);
// 				hab.setRearLiftSpeed(0.0, false);
// 				hab.setHabDriveSpeed(0.0);
// 				/**
// 				* Don't drive the pivots
// 				*/
// 				swerveController.setHabDrivePivots(false);
// 				/**
// 				* If A button pressed, set the hab height to level two height and go to the next state
// 				*/
// 				if (habController.getButton(Button.A)) {
// 					habHeight = HabHeight.L2;
// 					nextHabState = HabState.BOTH_LIFTS_DOWN;
// 				}
// 				/**
// 				* If B button pressed, set the hab height to level three height and go to the next state
// 				*/
// 				else if (habController.getButton(Button.B)) {
// 					habHeight = HabHeight.L3;
// 					nextHabState = HabState.BOTH_LIFTS_DOWN;
// 				}
// 				/**
// 				* If Y button pressed, set the hab height to mid level height and go to the next state
// 				*/
// 				else if (habController.getButton(Button.Y)) {
// 					habHeight = HabHeight.LMID;
// 					nextHabState = HabState.BOTH_LIFTS_DOWN;
// 				}
// 				/**
// 				* If RT pressed, set the hab height to lift and stop height and go to the next state
// 				*/
// 				else if (habController.getButton(Button.RT)) {
// 					habHeight = HabHeight.LIFT_AND_STOP;
// 					nextHabState = HabState.LIFT_AND_STOP;
// 				}
// 			} break;

// 			case BOTH_LIFTS_DOWN: {
// 				if (onChange) {
// 					pid.reset();
// 					pid.enable();
// 					swerveController.setHabDrivePivots(true);
// 				}
// 				/**
// 				* Sets the dog to hab mode and disengages the hab brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.disengageFrontBrake();
// 				hab.disengageRearBrake();
// 				/**
// 				* Moves the front hab down at 85% speed. The back will follow the front with the PID
// 				*/
// 				hab.setFrontLiftSpeed(0.85, false);
// 				/**
// 				* If the hab height is greater than the selected hab height, move to the next state
// 				*/
// 				if (hab.getFrontEncoderCounts() > habHeight.height) {
// 					nextHabState = HabState.DRIVE_FORWARD_1;
// 				}
// 				/**
// 				* If X button pressed, go to the reset state
// 				*/
// 				else if (habController.getButton(Button.X)) {
// 					nextHabState = HabState.RESET;
// 				}
// 			} break;

// 			case DRIVE_FORWARD_1: {
// 				if (onChange) {
// 					pid.disable();
// 				}
// 				/**
// 				* Sets the dog to hab mode and engages the hab brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.engageFrontBrake();
// 				hab.engageRearBrake();
// 				/**
// 				* Sets the front hab lift speed to 0, applies a 20% current to the rear hab lift, and drives the hab wheels at 25% speed
// 				*/
// 				hab.setFrontLiftSpeed(0.0, false);
// 				hab.setRearLiftSpeed(holdCurrent, false); 
// 				hab.setHabDriveSpeed(0.25);
// 				/**
// 				* Drives the pivots at 15% speed
// 				*/
// 				swerveController.setHabDrivePivots(true);
// 				drivePivots(0.15);
// 				/**
// 				* If front proximity sensor is activated, move to the next state
// 				*/
// 				if (proxFront.getDistance(UnitEnum.INCHES) < 5) {
// 					nextHabState = HabState.DRIVE_FORWARD_SLOWER_1;
// 				}
// 				/**
// 				* If X button pressed, go to the reset state
// 				*/
// 				else if (habController.getButton(Button.X)) {
// 					nextHabState = HabState.RESET;
// 				}
// 			} break;

// 			case DRIVE_FORWARD_SLOWER_1: {
// 				if (onChange) {
// 					driveForward1StartTime = System.nanoTime();
// 				}
// 				/**
// 				* Sets the dog to hab mode and engages the hab brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.engageFrontBrake();
// 				hab.engageRearBrake();
// 				/**
// 				* Applies a 20% holding current to the rear hab lift
// 				*/
// 				hab.setRearLiftSpeed(holdCurrent, false); 
// 				/**
// 				* If the time driven forward is less than 5e+8, drive the hab wheels at a 12.5% speed and drive the pivots at a 7.5% speed
// 				*/
// 				if (System.nanoTime() <= driveForward1StartTime + 5e+8) {
// 					hab.setHabDriveSpeed(0.125);
// 					swerveController.setHabDrivePivots(true);
// 					drivePivots(0.075);
// 				/**
// 				* Otherwise, stop driving and move to the next state
// 				*/
// 				} else {
// 					hab.setHabDriveSpeed(0.0);
// 					swerveController.setHabDrivePivots(false);
// 					nextHabState = HabState.FRONT_LIFT_UP;
// 				}			
// 			} break;

// 			case FRONT_LIFT_UP: {
// 				/**
// 				* Sets the dog to hab mode, disengages the front hab brake, and engages the rear brake
// 				*/
// 				hab.setDogHabMode();
// 				hab.disengageFrontBrake();
// 				hab.engageRearBrake();
// 				/**
// 				* Retracts the front hab lift and applies a 20% holding current to the rear lift
// 				*/
// 				hab.setFrontLiftSpeed(-1.0, false);
// 				hab.setRearLiftSpeed(holdCurrent, false); 
// 				/**
// 				* If the front hab is in the robot, go to the next state
// 				*/
// 				if (hab.getFrontEncoderCounts() < 2000) {
// 					nextHabState = HabState.DRIVE_FORWARD_2;
// 				}
// 				/**
// 				* If X button pressed, go to the reset state
// 				*/
// 				else if (habController.getButton(Button.X)) {
// 					nextHabState = HabState.RESET;
// 				}
// 			} break;

// 			case DRIVE_FORWARD_2: {
// 				/**
// 				* Sets the dog to hab mode and engages the hab brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.engageFrontBrake();
// 				hab.engageRearBrake();
// 				/**
// 				* Sets the front lift speed to 0, and applies a 20% holding current to the rear hab lift
// 				*/
// 				hab.setFrontLiftSpeed(0.0, false);
// 				hab.setRearLiftSpeed(holdCurrent, false); 
// 				/**
// 				* Drives the hab wheels at 25% speed and drives the pivots at 15% speed
// 				*/
// 				swerveController.setHabDrivePivots(true);
// 				hab.setHabDriveSpeed(0.25);
// 				drivePivots(0.15);
// 				/**
// 				* If rear proximity sensor is activated, move to the next state
// 				*/
// 				if (proxBack.getDistance(UnitEnum.INCHES) < 5) {
// 					nextHabState = HabState.DRIVE_FORWARD_SLOWER_2;
// 				}
// 			} break; 

// 			case DRIVE_FORWARD_SLOWER_2: {
// 				if (onChange) {
// 					driveForward2StartTime = System.nanoTime();
// 				}
// 				/**
// 				* Sets the dog to hab mode and engages the hab brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.engageFrontBrake();
// 				hab.engageRearBrake();
// 				/**
// 				* Apply a 20% holding current to the rear hab lift
// 				*/
// 				hab.setRearLiftSpeed(holdCurrent, false); 
// 				/**
// 				* If the time driven forward is less than 2e+8, drive the hab wheels at a 12.5% speed and drive the pivots at a 7.5% speed
// 				*/
// 				if (System.nanoTime() <= driveForward2StartTime + 2e+8) {
// 					hab.setHabDriveSpeed(0.125);
// 					swerveController.setHabDrivePivots(true);
// 					drivePivots(0.075);
// 				/**
// 				* Otherwise, set the pivots and hab wheels to 0 and move to the next state
// 				*/
// 				} else {
// 					hab.setHabDriveSpeed(0.0);
// 					swerveController.setHabDrivePivots(false);
// 					nextHabState = HabState.BACK_LIFT_UP;
// 				}
// 			} break;

// 			case BACK_LIFT_UP: {
// 				/**
// 				* Sets the dog to hab mode, engages the front brake, and disengages the rear brake
// 				*/
// 				hab.setDogHabMode();
// 				hab.engageFrontBrake();
// 				hab.disengageRearBrake();
// 				/**
// 				* Sets the front hab to 0, and moves the back hab up at 70% speed
// 				*/
// 				hab.setFrontLiftSpeed(0.0, false);
// 				hab.setRearLiftSpeed(-0.7, false);
// 				/**
// 				* If the hab lift is in the robot, move to the next state
// 				*/
// 				if (hab.getRearEncoderCounts() < 5000) { 
// 					nextHabState = HabState.DRIVE_FORWARD_SLOWER_3;
// 				}					
// 			} break;

// 			case DRIVE_FORWARD_SLOWER_3: {
// 				if (onChange) {
// 					driveForward2StartTime = System.nanoTime();
// 				}
// 				/**
// 				* Sets the rear hab to 0
// 				*/
// 				hab.setRearLiftSpeed(0.0, false); 
// 				/**
// 				* If the time driven forward is less than 1e+9, drive the hab wheels at a 12.5% speed and drive the pivots at a 7.5% speed
// 				*/
// 				if (System.nanoTime() <= driveForward2StartTime + 1e+9) {
// 					hab.setHabDriveSpeed(0.125);
// 					swerveController.setHabDrivePivots(true);
// 					drivePivots(0.075);
// 				/**
// 				* Otherwise, stop driving and go back to start state
// 				*/
// 				} else {
// 					hab.setHabDriveSpeed(0.0);
// 					swerveController.setHabDrivePivots(false);
// 					nextHabState = HabState.START;
// 				}
// 			} break;
// 			/**
// 			* Automatic sequence ends
// 			*/
// 			case LIFT_AND_STOP: {
// 				if (onChange) {
// 					pid.reset();
// 					pid.enable();
// 				}
// 				/**
// 				* Sets the dog to hab mode and disengage the brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.disengageFrontBrake();
// 				hab.disengageRearBrake();
// 				/**
// 				* Moves the front hab down at a speed of 85%, the back will follow in the PID
// 				*/
// 				hab.setFrontLiftSpeed(0.85, false);
// 				/**
// 				* If the habs are at the selected height, move to the next state
// 				*/
// 				if (hab.getFrontEncoderCounts() > habHeight.height) {
// 					nextHabState = HabState.HOLD_VOLTAGE_AT_ENDGAME;
// 				}
// 			} break;

// 			case HOLD_VOLTAGE_AT_ENDGAME: {
// 				if (onChange) {
// 					pid.disable();
// 					pid.reset();
// 				}
// 				/**
// 				* Zeros the hab encoders
// 				*/
// 				hab.zeroHabEncoderCounts();
// 				/**
// 				* Sets the dog to hab mode and engages the brakes
// 				*/
// 				hab.setDogHabMode();
// 				hab.engageFrontBrake();
// 				hab.engageRearBrake();
// 				/**
// 				* Stops the front hab lift, applies a 20% holding current to the rear hab, and stops driving the hab wheels
// 				*/
// 				hab.setFrontLiftSpeed(0.0, false);
// 				hab.setRearLiftSpeed(0.2, false);
// 				hab.setHabDriveSpeed(0.0);
// 				/**
// 				* Don't drive the pivots
// 				*/
// 				swerveController.setHabDrivePivots(false);
// 			}

// 			case RESET: {
// 				if (onChange) {
// 					pid.reset();
// 					pid.enable();
// 				}
// 				/**
// 				* Sets the dog to hab mode and disengages the brakes
// 				*/
// 				hab.setDogHabMode();					
// 				hab.disengageFrontBrake();
// 				hab.disengageRearBrake();
// 				/**
// 				* Moves the front hab lift up at 50% speed, the back follows through the PID
// 				*/
// 				hab.setFrontLiftSpeed(-0.5, false);
// 				/**
// 				* If the habs are in the robot, go back to the start state
// 				*/
// 				if (hab.getFrontEncoderCounts() < 2000) {
// 					nextHabState = HabState.START;
// 				}
// 			} break;
// 		}
// 	}
// }

