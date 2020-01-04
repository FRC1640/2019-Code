package frc.systems.hab;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Axis;
import frc.robot.Controller.Button;
import frc.robot.Controller.ButtonEvent;
import frc.systems.grabber.Grabber;
import frc.systems.hab.Hab;
import frc.systems.hab.IHabController;
import frc.systems.hab.Hab.LiftTarget;
import frc.utilities.LogUtil;
import frc.utilities.MathUtil;
import frc.utilities.TimingUtil2;

public class LiftNormalController implements IHabController {

	private boolean prevManualMode = false;
	private boolean manualMode = false;

	Grabber grabber;
	Controller opController;
	Hab hab;

	private PIDController pid;
	public double pidGet = 0;
	public double pidOut = 0;

	public LiftNormalController () {

		hab = Devices.getHab();
		grabber = Devices.getGrabber();
		hab = Devices.getHab();
		opController = Devices.getOperatorController();

	}

	@Override
	public void update() {
		/**
		* If LB pressed, move intake to middle position and go to the floor lift height
		*/
		if (opController.getButton(Button.LB)) {		
			Devices.getIntake().intermediateExtend();
			hab.setLiftTarget(LiftTarget.FLOOR_OR_CARGO_FROM_FLOOR);
		}
		/**
		* If S button pressed, go to loading station cargo lift height, after 750 milliseconds, bring the intake fully in
		*/
		else if (opController.getButton(Button.S)) {
			hab.setLiftTarget(LiftTarget.CARGO_LOADING_STATION);
			TimingUtil2.registerOneTimeCallback(750, () -> Devices.getIntake().retract());
		}
		/**
		* If W button pressed, go to level one cargo/hatch lift height
		*/
		else if (opController.getButton(Button.W)) {
			hab.setLiftTarget(LiftTarget.L1_CARGO);
		}
		/**
		* If E button pressed, go to level two cargo/hatch lift height
		*/
		else if (opController.getButton(Button.E)) {
			hab.setLiftTarget(LiftTarget.L2_CARGO);
		}
		/**
		* If N button pressed, go to level three cargo/hatch lift height
		*/
		else if (opController.getButton(Button.N)) {
			hab.setLiftTarget(LiftTarget.L3_CARGO);
		}

	}
	/**
	* Activates the objects in the class
	*/
	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activate");
		/**
		* Sets the dog to lift mode and engage the hab brakes
		*/
		hab.setDogLiftMode();
		hab.engageFrontBrake();
		hab.engageRearBrake();
		/**
		* Sets the lift speeds to 0
		*/
		hab.setFrontLiftSpeed(0.0, false);
		hab.setRearLiftSpeed(0.0, false);
		/**
		* Sets the config values
		*/
		hab.resetConfigs();
		hab.setPersistantConfigs();
		hab.setLiftMotionMagicConfigs();
	}
	/**
	* Deactivates the objects in the class
	*/
	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivate");
		/**
		* Sets the dog to lift mode and engage the hab brakes
		*/
		hab.setDogLiftMode();
		hab.engageFrontBrake();
		hab.engageRearBrake();
		/**
		* Sets the lift speeds to 0
		*/
		hab.setFrontLiftSpeed(0.0, false);
		hab.setRearLiftSpeed(0.0, false);
		/**
		* Resets the config values
		*/
		hab.resetConfigs();
	}

}