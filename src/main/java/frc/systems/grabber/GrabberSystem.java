package frc.systems.grabber;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Robot;
import frc.robot.Controller.Button;
import frc.robot.Controller.ButtonEvent;
import frc.systems.RobotSystem;
import frc.systems.hab.Hab;
import frc.systems.hab.Hab.LiftTarget;
import frc.utilities.TimingUtil2;

public class GrabberSystem extends RobotSystem {
	/**
	* Enum to set the states of the grabber's solenoids
	*
	* @param grabber takes a boolean to set the state of the grabber's extender
	*
	* @param claw takes a boolean to set the state of the grabber's claw
	*/
	public static enum HatchState {
		/**
		* Extends the grabber and open the claw
		*/
		GRABBER_OUT_CLAW_OPEN (true, true),
		/**
		* Extends the grabber and close the claw
		*/
		GRABBER_OUT_CLAW_CLOSED (true, false),
		/**
		* Retracts the grabber and open the claw
		*/
		GRABBER_IN_CLAW_OPEN (false, true),
		/**
		* Retracts the grabber and close the claw
		*/
		GRABBER_IN_CLAW_CLOSED (false, false);

		boolean grabberState;
		boolean clawState;

		private HatchState (boolean grabber, boolean claw) {
			this.grabberState = grabber;
			this.clawState = claw;
		}
	}

	private Grabber grabber;
	private Controller driveController;
	private Controller opController;

	private Hab habLift;

	public GrabberSystem () {
		super("Grabber System");
		grabber = Devices.getGrabber();
		habLift = Devices.getHab();
	}

	@Override
	public void init() {

		driveController = Devices.getDriverController();
		// opController = Devices.getOperatorController();

		/**
		 * DRIVER CONTROLS
		 * 
		 * If LT pressed, automatically grab a hatch, and record last action as hatch scored
		 * 
		 * If LB pressed, automatically place a hatch
		 */
		driveController.registerButtonListener(ButtonEvent.PRESS, Button.LT, () -> {
			callback("run timed deploy and grab");
			grabber.timedDeployAndGrab();
			Robot.setHatchAction();
		});
		driveController.registerButtonListener(ButtonEvent.PRESS, Button.LB, () -> {
			callback("run timed deploy and release");
			grabber.timedDeployAndRelease();
		}); 
		

		/**
		 * OPERATOR CONTROLS
		 * 
		 * If LT pressed, toggle the hatch grabber's extender
		 * 
		 * If RT pressed, toggle the hatch grabber's claw, and record last action as hatch scored
		 */

		opController.registerButtonListener(ButtonEvent.PRESS, Button.LT, () -> {
			callback("toggle extend");
			grabber.toggleExtend();
		});
		opController.registerButtonListener(ButtonEvent.PRESS, Button.RT, () -> {
			callback("toggle claw");
			grabber.toggleClaw();
			Robot.setHatchAction();
		});
	}

	@Override
	public void preStateUpdate() throws Exception {

	}

	@Override
	public void postStateUpdate() throws Exception {

	}

	@Override
	public void disabledInit() throws Exception {

	}

	@Override
	public void disabledUpdate() throws Exception {

	}

	@Override
	public void autonInit() throws Exception {

	}

	@Override
	public void autonUpdate() throws Exception {

	}

	@Override
	public void teleopInit() throws Exception {

	}

	@Override
	public void teleopUpdate() throws Exception {
		
	}

	@Override
	public void testInit() throws Exception {

	}

	@Override
	public void testUpdate() throws Exception {

	}

	@Override
	public void enable() throws Exception {

	}

	@Override
	public void disable() throws Exception {

	}

	public void timedDeployAndGrab() {
		
	}

}