package frc.systems.intake;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Robot;
import frc.robot.Controller.Button;
import frc.robot.Controller.ButtonEvent;
import frc.systems.RobotSystem;

public class IntakeSystem extends RobotSystem {

	private Intake intake;
	private Controller driverController;
	private Controller opController;
	private double intakeSpeed;
	private double outtakeSpeed;

	public IntakeSystem () {
		super("Intake System");
		intake = Devices.getIntake();
	}

	@Override
	public void init() {
		driverController = Devices.getDriverController();
		opController = Devices.getOperatorController();
		intake = Devices.getIntake();
		intakeSpeed = .75;
		outtakeSpeed = 1.0;

		/**
		 * DRIVER CONTROLLER SETTINGS
		 */
		/**
		* If RT pressed, put the intake all the way out
		*/
		driverController.registerButtonListener(ButtonEvent.PRESS, Button.RT, () -> {
			callback("fully extend intake");
			intake.fullyExtend();
		});
		/**
		* If RT released, put the intake above the bumper
		*/
		driverController.registerButtonListener(ButtonEvent.RELEASE, Button.RT, () -> {
			callback("intermediate extend intake");
			intake.intermediateExtend();
		});
		/**
		* If W button pressed, put the intake all the way out
		*/
		driverController.registerButtonListener(ButtonEvent.PRESS, Button.W, () -> {
			callback("fully extend intake");
			intake.fullyExtend(); 
		});
		/**
		* If W button released, put the intake above the bumper
		*/
		driverController.registerButtonListener(ButtonEvent.RELEASE, Button.W, () -> {
			callback("intermediate extend intake");
			intake.intermediateExtend(); 
		});
		/**
		* If N button pressed, put the intake above the bumper
		*/
		driverController.registerButtonListener(ButtonEvent.PRESS, Button.N, () -> {
			callback("intermediate extend intake");
			intake.intermediateExtend();
		});
		/**
		* If S button pressed, put the intake all the way in
		*/
		driverController.registerButtonListener(ButtonEvent.PRESS, Button.S, () -> {
			callback("retract intake");
			intake.retract();
		});
	}

	@Override
	public void teleopUpdate() throws Exception { 
		/**
		* If RT pressed, run the intake and set the last scoring action as cargo
		*/
		if (driverController.getButton(Button.RT)) {
			intake.setIntakeSpeed(-1.0, true);
			Robot.setCargoAction();
		/**
		* If E button pressed, run the cargo holder and set the last scoring action as cargo
		*/
		} else if (driverController.getButton(Button.E)) {
			intake.ejectGrabber(-intakeSpeed);
			Robot.setCargoAction();
		/**
		* If RB pressed on either controller, run the intake, run the cargo holder, and set the last scoring action as hatch
		*/
		} else if (driverController.getButton(Button.RB) || opController.getButton(Button.RB)) {
			intake.setIntakeSpeed(outtakeSpeed, true);
			intake.ejectGrabber(outtakeSpeed);
			Robot.setHatchAction();
		/**
		* If W button pressed, run the intake and set the last scoring action as hatch
		*/
		} else if (driverController.getButton(Button.W)) {
			intake.setIntakeSpeed(outtakeSpeed, true);
			Robot.setHatchAction();
		/**
		* Otherwise, set the intake speed to 0
		*/
		} else {
			intake.setIntakeSpeed(0.0, false);
		}
	}

	@Override
	public void enable() throws Exception {
		intake.enable();
	}

	@Override
	public void disable() throws Exception {
		intake.disable();
	}

	@Override public void preStateUpdate() throws Exception {

	} 
	@Override public void postStateUpdate() throws Exception { }
	@Override public void disabledInit() throws Exception { }
	@Override public void disabledUpdate() throws Exception { }
	@Override public void autonInit() throws Exception { }
	@Override public void autonUpdate() throws Exception { }
	@Override public void teleopInit() throws Exception { }
	@Override public void testInit() throws Exception { }
	@Override public void testUpdate() throws Exception { }

}