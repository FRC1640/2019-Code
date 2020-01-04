package frc.systems.lift.controllers;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Axis;
import frc.systems.hab.Hab;
import frc.systems.hab.IHabController;
import frc.utilities.LogUtil;

public class LiftManualController implements IHabController {

	private Controller opController;
	private Hab hab;

	public LiftManualController () {
		opController = Devices.getOperatorController();
		hab = Devices.getHab();	
	}

	@Override
	public void update() {
		double speed = opController.getAxis(Axis.LY);
		/**
		* If going up, 70% speed, otherwise, 40% speed
		*/
		if (speed > 0) {
			speed *= 0.85; 
		}
		else {
			speed *= 0.4;
		}
		/**
		* Set the lift speed to the variable "speed"
		*/
		hab.setRaw(speed);
	}

	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activate");
	}

	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivate");
	}

}