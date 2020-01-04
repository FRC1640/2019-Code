package frc.systems.intake;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class Intake {

	private AtomicBoolean enabled;
	private AtomicBoolean running;
	private WPI_TalonSRX intakeMotor;
	private WPI_TalonSRX grabberMotor;
	/**
	* Enum to set the states of the intake's solenoids
	*
	* @param top takes a boolean to set the state of the intake's top stage
	*
	* @param bot takes a boolean to set the state of the intake's bottom stage
	*/
	static enum IntakeState {
		/**
		* Retracts both stages
		*/
		RETRACTED (false, false),
		/**
		* Retracts top stage and extends bottom stage
		*/
		INTERMEDIATE (false, true),
		/**
		* Extends both stages
		*/
		EXTENDED (true, true),
		;

		private boolean top;
		private boolean bot;

		private IntakeState (boolean top, boolean bot) {
			this.top = top;
			this.bot = bot;
		}
	}

	private IntakeState state;

	public Intake (int mChannel, int tChannel, int bChannel_1, int bChannel_2, int gChannel) {
		retract();
		running = new AtomicBoolean(true);
		enabled = new AtomicBoolean(false);
		/**
		* Initializes solenoids and motors to their respective ports
		*/
		grabberMotor = new WPI_TalonSRX(gChannel);
		intakeMotor = new WPI_TalonSRX(mChannel);
		Solenoid topSol = new Solenoid(tChannel);
		DoubleSolenoid botSol = new DoubleSolenoid(bChannel_1, bChannel_2);
		/**
		* Creates a new thread for the intake system
		*/
		new Thread(() -> {
	
			while (running.get()) {
					topSol.set(state.top);
					botSol.set(state.bot ? Value.kForward : Value.kReverse);
				
				try { Thread.sleep(20); }
				catch (Exception e) { }
			}

			topSol.close();
			botSol.close();

		}).start();
	}
	/**
	* Stops the thread
	*/
	public void terminate () {
		running.set(false);
	}
	/**
	* Retracts the intake
	*/
	public void retract () {
		state = IntakeState.RETRACTED;
	}
	/**
	* Puts the intake over the bumpers
	*/
	public void intermediateExtend () {
		state = IntakeState.INTERMEDIATE;
	}
	/**
	* Extends the intake all the way out
	*/
	public void fullyExtend () {
		state = IntakeState.EXTENDED;
	}
	/**
	* Runs the intake 
	*
	* @param speed sets the speed of the intake from (-1, 1)
	*
	* @param outtakeGrabber outtakes the cargo holder
	*/
	public void setIntakeSpeed (double speed, boolean outtakeGrabber) {
		intakeMotor.set(ControlMode.PercentOutput, speed);
		/**
		* If the speed is under 0, set the cargo holder to negative speed
		*/
		if (speed < 0) { grabberMotor.set(ControlMode.PercentOutput, -speed); }
		/**
		* If the cargo holder isn't spinning, set the cargo holder to 0
		*/
		else if (!outtakeGrabber) { grabberMotor.set(ControlMode.PercentOutput, 0.0); }
	}
	/**
	* Spins the cargo holder
	*
	* @param speed sets the speed of the cargo holder from (-1, 1)
	*/
	public void ejectGrabber (double speed) {
		grabberMotor.set(ControlMode.PercentOutput, -speed);	
	}

	public void enable () {

	} 

	public void disable () {

	}

}