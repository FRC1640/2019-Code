package frc.systems.hab;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Solenoid;
import frc.utilities.LogUtil;

public class Hab {

	public static final double COUNTS_PER_INCH = 4 * 4096 / 18;
	public static final double INCHES_PER_COUNT = 1.0 / COUNTS_PER_INCH;

	/**
	* Enum for lift motion magic values
	*
	* @param velocity Motion Magic's cruise velocity
	* 
	* @param acceleration Motion Magic's acceleration and deceleration velocities
	*/

	public static enum ProfileState {
		UP(4800, 4500), //5235, 5608
		DOWN(4500, 4300); // 3298, 2842

		int velocity;
		int acceleration;

		private ProfileState (int v, int a) {
			velocity = v;
			acceleration = a;
		}
	}

	/**
	* Enum for lift heights corresponding to scoring heights on the field
	*
	* @param height Value to set lift height
	*/

	// These MUST be ordered from SMALLEST to LARGEST
	public static enum LiftTarget {
		/**
		* Cargo intaking height/lowest height
		*/
		FLOOR_OR_CARGO_FROM_FLOOR (0), 
		/**
		* Match start height with hatch
		*/
		START_HEIGHT (11170), 
		/**
		* Rocket level one cargo/hatch scoring height
		*/
		L1_CARGO (12500),
		/**
		* Cargo from loading station height
		*/
		CARGO_LOADING_STATION (29000),
		/**
		* Rocket level two cargo/hatch scoring height
		*/
		L2_CARGO (40760),
		/**
		* Rocket level three cargo/hatch scoring height
		*/
		L3_CARGO (68320),
		/**
		* Raise lift after taking hatch from loading station height
		*/
		LOADING_STATION_LIFT_HEIGHT (15000),
		;

		int height;

		LiftTarget(int height) {
			this.height = height;
		}

		public int getHeight() {
			synchronized (this) {
				return height;
			}
		}
	}

	final Object liftLock = new Object();

	ProfileState profileState;
	LiftTarget liftTarget;
	LiftTarget prevLiftTarget;

	private WPI_TalonSRX habFront1;
	private WPI_TalonSRX habFront2;
	private WPI_TalonSRX habRear;
	private WPI_TalonSRX habDrive;

	private Solenoid brakeFront;
	private Solenoid brakeRear;
	private Solenoid dog;

	private AnalogInput proxFront;
	private AnalogInput proxRear;

	// negative number raises lift . . . i.e. lowers robot
	// positive number lowers lift . . . i.e. raises robot

	// prox ~1.6 - 2.x . . .  (assume anything greater than one)

	public Hab () {
		/**
		* Initializes motors to respective ports
		*/
		habFront1 = new WPI_TalonSRX(3); 
		habFront2 = new WPI_TalonSRX(2);
		habRear = new WPI_TalonSRX(12);
		habDrive = new WPI_TalonSRX(13);
		/**
		* Initializes solenoids to respective ports
		*/
		brakeFront = new Solenoid(3);
		brakeRear = new Solenoid(4);
		dog = new Solenoid(5);
		/**
		* Defaults motors to neutral mode break
		*/
		habFront1.setNeutralMode(NeutralMode.Brake);
		habFront2.setNeutralMode(NeutralMode.Brake);
		habRear.setNeutralMode(NeutralMode.Brake);

		setPersistantConfigs();
		zeroHabEncoderCounts();

		habFront1.getSensorCollection().setQuadraturePosition(LiftTarget.START_HEIGHT.height, 20);
	
		profileState = ProfileState.UP;

		prevLiftTarget = liftTarget = LiftTarget.L1_CARGO;
	}
	/**
	* Resets Motion Magic config values for motors
	*/
	public void resetConfigs () {
		habFront1.configFactoryDefault();
		habFront2.configFactoryDefault();
		habRear.configFactoryDefault();
		habDrive.configFactoryDefault();
	}

	/********************************************************/

	public void setPidTestConfigs () {
		habFront1.setNeutralMode(NeutralMode.Brake);
		habFront2.setNeutralMode(NeutralMode.Brake);
	}
	/**
	* Configure lift Motion Magic values
	*/
	public void setPersistantConfigs () {
		habFront2.set(ControlMode.Follower, 3);

		habFront1.setNeutralMode(NeutralMode.Brake);
		habFront2.setNeutralMode(NeutralMode.Brake);
		habRear.setNeutralMode(NeutralMode.Brake);

		habFront1.setSensorPhase(false);
		habFront2.setSensorPhase(false);
		habRear.setSensorPhase(true);

		habFront1.setInverted(false);
		habFront2.setInverted(false);
		habRear.setInverted(false);

		habFront2.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 20);
		habRear.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 20);

		habFront1.configPeakOutputForward(1, 20);
		habFront2.configPeakOutputForward(1, 20);
		habRear.configPeakOutputForward(1, 20);

		habFront1.configPeakOutputReverse(-1, 20);
		habFront2.configPeakOutputReverse(-1, 20);
		habRear.configPeakOutputReverse(-1, 20);
	} 
	/**
	* Sets Motion Magic P, I, D, F values for up and down
	*/
	public void setLiftMotionMagicConfigs () {

		habFront1.selectProfileSlot(0, 0);
		habFront1.config_kF(0, 0.223); // .223
		habFront1.config_kP(0, 0.17); // .17
		habFront1.config_kI(0, 0.00001); //0.001
		habFront1.config_kD(0, 0.0005);

		habFront1.selectProfileSlot(1, 0);
		habFront1.config_kF(1, 0.15);
		// habFront1.config_kF(1, 0.1);
		habFront1.config_kP(1, 0.1); 
		habFront1.config_kI(1, 0.0001); 
		// habFront1.config_kI(1, 0.001); 
		habFront1.config_kD(1, 0);

		habFront1.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
		/**
		* Sets Cruise and Acceleration to enum values in Profile State
		*/
		habFront1.configMotionCruiseVelocity(profileState.velocity);
		habFront1.configMotionAcceleration(profileState.acceleration);
	}

	public void setPidConfigs () {
		setPersistantConfigs();
	}

	public void setManualConfigs () {
		setPersistantConfigs();
	}

	/********************************************************/
	/**
	* @return Returns the analog voltage of the front proximity sensor
	*/
	public double getFrontProxVoltage () {
		return proxFront.getVoltage();
	}
	/**
	* @return Returns the analog voltage of the rear proximity sensor
	*/
	public double getRearProxVoltage () {
		return proxRear.getVoltage();
	}
	/**
	* @return Returns the current of the rear hab lift motor
	*/
	public double getRearLiftMotorCurrent() {
		return habRear.getOutputCurrent();
	}

	/********************************************************/

	public void zeroHabEncoderCounts () {
		habFront2.getSensorCollection().setQuadraturePosition(0, 20);
		habRear.getSensorCollection().setQuadraturePosition(0, 20);
	}

	public void zeroLiftEncoder () {
		habFront1.getSensorCollection().setQuadraturePosition(0, 20);
	}
	/**
	* @return Returns the lift height in encoder counts
	*/
	public int getLiftHeightInCounts () {
		return habFront1.getSelectedSensorPosition(0);
	}
	/**
	* @return Returns the lift height in inches
	*/
	public double getLiftHeightInInches () {
		return INCHES_PER_COUNT * getLiftHeightInCounts();
	}
	/**
	* @return Returns the front hab height in encoder counts
	*/
	public int getFrontEncoderCounts () {
		return habFront2.getSelectedSensorPosition();
		// return 0;
	}
	/**
	* @return Returns the rear hab height in encoder counts
	*/
	public int getRearEncoderCounts () {
		return habRear.getSelectedSensorPosition();
	}

	/********************************************************/
	/**
	* Sets front hab lift speed
	* @param speed Accepts a speed of (-1, 1)
	* @param override Set to true to prevent the lift from going past a certain number of encoder counts
	*/
	public void setFrontLiftSpeed (double speed, boolean override) {
		/**
		* If-statment checks if lift should move based on speed, override setting, and encoder position
		*/
		if (getFrontEncoderCounts() <= 250 && speed <= 0 && !override) {
			speed = 0;
		}
		/**
		* Sets hab speeds
		*/
		habFront1.set(ControlMode.PercentOutput, speed);
		habFront2.set(ControlMode.PercentOutput, speed);
	}
	/**
	* Sets rear hab lift speed
	*
	* @param speed Accepts a speed of (-1, 1)
	*
	* @param override Set to true to prevent the lift from going past a certain number of encoder counts
	*/
	public void setRearLiftSpeed (double speed, boolean override) {
		/**
		* If-statment checks if lift should move based on speed, override setting, and encoder position
		*/
		if (getRearEncoderCounts() <= 250 && speed <= 0 && !override) {
			speed = 0;
		}
		/**
		* Sets hab speed
		*/
		habRear.set(ControlMode.PercentOutput, speed);
	}
	/**
	* Sets the speed of the hab wheels
	*
	* @param speed accepts a speed of (-1, 1)
	*/
	public void setHabDriveSpeed (double speed) {
		habDrive.set(ControlMode.PercentOutput, speed);
	}

	/********************************************************/
	/**
	* Sets front hab brake to enagaged
	*/
	public void engageFrontBrake () {
		brakeFront.set(false);
	}
	/**
	* Sets front hab brake to disenagaged
	*/
	public void disengageFrontBrake () {
		brakeFront.set(true);
	}
	/**
	* @return checks if front brake is engaged
	*/
	public boolean isFrontBrakeEngaged () {
		return !brakeFront.get();
	}

	/********************************************************/
	/**
	* Sets rear hab brake to enagaged
	*/
	public void engageRearBrake () {
		brakeRear.set(false);
	}
	/**
	* Sets rear hab brake to disenagaged
	*/
	public void disengageRearBrake () {
		brakeRear.set(true);
	}
	/**
	* @return checks if rear brake is engaged
	*/
	public boolean isRearBrakeEngaged () {
		return !brakeRear.get();
	}

	/********************************************************/
	/**
	* Sets dog solenoid to the lift position (run the lift and not the hab)
	*/
	public void setDogLiftMode () {
		dog.set(false);
	}
	/**
	* Sets dog solenoid to the hab position (run the hab and not the lift)
	*/
	public void setDogHabMode () {
		dog.set(true);
	}
	/**
	* @return checks if the dog is in lift mode
	*/
	public boolean isDogLiftMode () {
		return !dog.get();
	}
	/**
	* @return checks if dog is in hab mode
	*/
	public boolean isDogHabMode () {
		return dog.get();
	}

	/********************************************************/
	/**
	* Gets the current lift target
	*/
	public LiftTarget getLiftTarget () {
		synchronized (liftLock) { return liftTarget; }
	}
	/**
	* @return returns the target height of the lift from the lift height enum
	*/
	public double getTargetHeightInInches () {
		return getLiftTarget().getHeight();
	}
	/**
	* Sets lift target to the lowest lift height
	*/
	public void resetLiftTarget () {
		// setTarget(LiftTarget.FLOOR_OR_CARGO_FROM_FLOOR);
		setLiftTarget(LiftTarget.values()[0]);
	}
	/**
	* Sets the lift target based on the heights from the lift heights enum
	*
	* @param target accepts a lift target from the heights enum
	*/
	public void setLiftTarget (LiftTarget target) {
		synchronized (liftLock) { liftTarget = target; }

		// System.out.format("target height %.3f" + target.height);
		if (prevLiftTarget != target) { LogUtil.log(getClass(), "Target: " + target.toString()); }
		prevLiftTarget = target;

		double dHeight = target.height - getLiftHeightInCounts();
		/**
		* If the height is greater than 0, move the lift up based on the Motion Magic velocities
		*/
		if (dHeight > 0) {
			habFront1.selectProfileSlot(0, 0);
			profileState = ProfileState.UP;
		/**
		* If the height is greater less 0, move the lift down based on the Motion Magic velocities
		*/
		} else {
			habFront1.selectProfileSlot(1, 0);
			profileState = ProfileState.DOWN;
		}
		/**
		* Assign the lift motors to their respective Motion Magic velocities
		*/
		habFront1.configMotionCruiseVelocity(profileState.velocity);
		habFront1.configMotionAcceleration(profileState.acceleration);

		habFront1.set(ControlMode.MotionMagic, getLiftHeightInCounts() + dHeight);
	}
	/**
	* @param speed sets the lift speed at a value (-1, 1)
	*/
	public void setRaw (double speed) {
		habFront1.set(ControlMode.PercentOutput, speed);
	}
	/**
	* @return checks if the lift height is at its target within a range of 1000
	*/
	public boolean isLiftAtTarget () {
		return Math.abs(getLiftHeightInCounts() - getLiftTarget().height) <= 1000;
	}

}