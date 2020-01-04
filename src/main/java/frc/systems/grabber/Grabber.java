package frc.systems.grabber;

import java.util.concurrent.atomic.AtomicBoolean;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Solenoid;
import frc.utilities.LogUtil;
import frc.utilities.TimingUtil2;
import frc.systems.hab.Hab;
import frc.systems.hab.Hab.LiftTarget;
import frc.robot.Devices;

public class Grabber {

	private AtomicBoolean running;
	private AtomicBoolean grabberExtended;
	private AtomicBoolean clawClosed;

	private NetworkTableEntry clawState;

	public Grabber (int grabberChannel, int clawChannel) {

		grabberExtended = new AtomicBoolean(false);
		clawClosed = new AtomicBoolean(false);

		running = new AtomicBoolean(true);

		clawState = NetworkTableInstance.getDefault().getTable("SmartDashboard").getEntry("clawState");

		/**
		* Creates a new thread for the grabber system
		*/

		new Thread(() -> {
			/**
			* Initializes solenoids to their respective ports
			*/
			Solenoid extender = new Solenoid(grabberChannel);
			Solenoid claw = new Solenoid(clawChannel);
			
			while (running.get()) {
				extender.set(grabberExtended.get());
				claw.set(clawClosed.get());
				
				try { Thread.sleep(20); }
				catch (Exception e) { }
			}

			extender.close();
			claw.close();

		}).start();
	}
	/**
	* Stops the thread
	*/
	public void terminate () {
		running.set(false);
	}
	/**
	* Extends the hatch grabber extending mechanism
	*/
	public void extend () {
		grabberExtended.set(true);
	}
	/**
	* Retracts the hatch grabber extending mechanism
	*/
	public void retract () {
		grabberExtended.set(false);
	}
	/**
	* Toggles the hatch grabber extending mechanism
	*/
	public void toggleExtend () {
		grabberExtended.set(!grabberExtended.get());
	}
	/**
	* Toggles the hatch grabber grabbing mechanism
	*/
	public void toggleClaw () {
		clawClosed.set(!clawClosed.get());
		clawState.setBoolean(clawClosed.get());
	}
	/**
	* Opens the hatch grabber grabbing mechanism
	*/
	public void open () {
		clawClosed.set(false);
	}
	/**
	* Closes the hatch grabber grabbing mechanism
	*/
	public void close () {
		clawClosed.set(true);
	}
	/**
	* Sets the hatch grabber grabbing mechanism to open
	*/
	public void setClawOpen (boolean open) {
		clawClosed.set(open);
		clawState.setBoolean(clawClosed.get());
	}
	/**
	* Sets the hatch grabber extending mechanism to extended
	*/
	public void setGrabberExtended (boolean extended) {
		grabberExtended.set(extended);
	}
	/**
	* Grabs a hatch from the loading station
	*/
	public void timedDeployAndGrab() {
		Hab habLift = Devices.getHab();
		close();

		LogUtil.log(getClass(), "auto-grab hatch");
		/**
		* After 25 milliseconds, extend the hatch grabber
		*/
		TimingUtil2.registerOneTimeCallback(25, () -> {
			extend();
			LogUtil.callback(getClass(), "time - extend");
		});
		/**
		* After 250 milliseconds, open the hatch grabber's claw
		*/
		TimingUtil2.registerOneTimeCallback(250, () -> {
			open();
			LogUtil.callback(getClass(), "time - open");
		});
		/**
		* After 400 milliseconds, raise the lift to the specified height
		*/
		TimingUtil2.registerOneTimeCallback(400, () -> {
			habLift.setLiftTarget(LiftTarget.LOADING_STATION_LIFT_HEIGHT);
			LogUtil.callback(getClass(), "time - slight raise height");
		});
		/**
		* After 600 milliseconds, retract the hatch grabber
		*/
		TimingUtil2.registerOneTimeCallback(600, () -> {
			retract();
			LogUtil.callback(getClass(), "time - retract");
		});
	}
	/**
	* Places a hatch
	*/
	public void timedDeployAndRelease() {
		open();

		LogUtil.log(getClass(), "auto-release hatch");
		/**
		* After 0 milliseconds, extend the hatch grabber
		*/
		TimingUtil2.registerOneTimeCallback(0, () -> {
			extend();
			LogUtil.callback(getClass(), "time - extend");
		});
		/**
		* After 250 milliseconds, close the hatch grabber's claw
		*/
		TimingUtil2.registerOneTimeCallback(250, () -> {
			close();
			LogUtil.callback(getClass(), "time - close");
		});
		/**
		* After 400 milliseconds, retract the hatch grabber
		*/
		TimingUtil2.registerOneTimeCallback(400, () -> {
			retract();
			LogUtil.callback(getClass(), "time - retract");
		});
	}

}