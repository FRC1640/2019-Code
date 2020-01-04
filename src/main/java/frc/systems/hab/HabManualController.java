package frc.systems.hab;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Axis;
import frc.robot.Controller.Button;
import frc.utilities.LogUtil;

public class HabManualController implements IHabController {

	private Hab hab;
	private Controller habController;

	private int count = 0;

	public HabManualController (Hab hab) {
		this.hab = hab;
		habController = Devices.getHabController();
	}
	/**
	* Activates the objects in the class
	*/
	@Override
	public void activate () {
		LogUtil.log(getClass(), "Activate");
		/**
		* Resets config values
		*/
		hab.resetConfigs();
		hab.setManualConfigs();
		/**
		* Sets the dog to hab mode
		*/
		hab.setDogHabMode();
	}

	@Override
	public void deactivate () {
		LogUtil.log(getClass(), "Deactivate");
	}

	@Override
	public void update () {
		if (++count % 200 == 0) {
			LogUtil.log(getClass(), "Update");
			count = 0;
		}
		/**
		* Assigns variables to axis and buttons+
		*/
		double ry = habController.getAxis(Axis.RY);

		boolean bS = habController.getButton(Button.S);
		boolean bN = habController.getButton(Button.N);
		boolean bW = habController.getButton(Button.W);
		boolean override = habController.getButton(Button.B);
		/**
		* If E button pressed, reset hab encoder
		*/
		if (habController.getButton(Button.E)) {
			hab.zeroHabEncoderCounts();
		}
		/**
		* If W button pressed, disengage brakes and set dog to hab mode
		*/
		if (bW) {
			hab.disengageFrontBrake();
			hab.disengageRearBrake();
			hab.setDogHabMode();
		/**
		* If N button pressed, disengage front brake, engage rear brake, and set dog to hab mode
		*/
		} else if (bN) {
			hab.disengageFrontBrake();
			hab.engageRearBrake();
			hab.setDogHabMode();
		/**
		* If S button pressed, engage front brake, disengage rear brake, and set dog to hab mode
		*/
		} else if (bS) {
			hab.engageFrontBrake();
			hab.disengageRearBrake();
			hab.setDogHabMode();
		/**
		* Otherwise, engage brakes and set dog to hab mode
		*/
		} else {
			hab.engageFrontBrake();
			hab.engageRearBrake();
			hab.setDogLiftMode();
		}
		/**
		* If S button pressed or W button pressed, assign the rear hab lift speed to the right y-axis, don't move past 0
		*/
		if (bS || bW) {
			hab.setRearLiftSpeed(ry * 0.25, override);
		/**
		* Otherwise, set rear lift speed to 0
		*/
		} else {
			hab.setRearLiftSpeed(0, false);
		}
		/**
		* If N button pressed or W button pressed, assign the rear front lift speed to the right y-axis, don't move past 0
		*/
		if (bN || bW) {
			hab.setFrontLiftSpeed(ry, override);
		/**
		* Otherwise, set front lift speed to 0
		*/
		} else {
			hab.setFrontLiftSpeed(0, false);
		}
		/**
		* Assigns hab wheel speed to left y-axis
		*/
		hab.setHabDriveSpeed(habController.getAxis(Axis.LY));

	}

}