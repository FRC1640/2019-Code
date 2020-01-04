package frc.systems.hab;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Axis;

public class LiftManualController implements IHabController {

	private Hab hab;
	private Controller opController;

	public LiftManualController (Hab hab) {
		this.hab = hab;
		opController = Devices.getOperatorController();
	}
	/**
	* Activates the objects in the class 
	*/
	@Override
	public void activate() {
		/**
		* Resets config values
		*/
		hab.resetConfigs();
		hab.setManualConfigs();
		/**
		* Sets dog to lift mode and engages hab brakes
		*/
		hab.setDogLiftMode();
		hab.engageFrontBrake();
		hab.engageRearBrake();
		/**
		* Sets lift speeds to 0
		*/
		hab.setFrontLiftSpeed(0.0, false);
		hab.setRearLiftSpeed(0.0, false);
	}

	@Override
	public void deactivate() {
		hab.resetConfigs();
	}

	@Override
	public void update() {
		double speed = opController.getAxis(Axis.LY);
		/**
		* If going up, 70% speed, otherwise, 40% speed
		*/
		if (speed > 0) {
			speed *= 0.7;
		} else {
			speed *= 0.4;
		}
		/**
		* Set the lift speed to the variable "speed"
		*/
		hab.setRaw(speed);
	}

}