package frc.systems.drive.controllers;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Axis;
import frc.systems.drive.controllers.SwerveController.CvtMode;
import frc.systems.drive.controllers.SwerveController.SwerveMode;
import frc.utilities.LogUtil;

public class OperatorDriveController implements IDriveController {

	private SwerveController swerveController;
	private Controller opController;
	private SwerveMode prevDriveMode;
	/**
    * @param swerveController passes in the swerve controller class
    */
	public OperatorDriveController (SwerveController swerveController) {
		opController = Devices.getOperatorController();
		this.swerveController = swerveController;
	}
	/**
    * Sets the previous drive mode to the current swervemode and sets the swerve mode to robot centric
    */
	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activating");
		prevDriveMode = swerveController.getSwerveMode();
		swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
	}
	/**
    * Sets the swerve mode to prev drive mode
    */
	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivating");
		swerveController.setSwerveMode(prevDriveMode);
	}
	
	@Override
	public void update() {
		double multiplier = CvtMode.PRECISION.getSpeedMultiplier();
		/**
		* Sets the variables equal to the controller axis multiplied by the specified multiplier 
		*/
		double x1 = opController.getAxis(Axis.LX) * multiplier;
		double y1 = opController.getAxis(Axis.LY) * multiplier;
		double x2 = opController.getAxis(Axis.RX) * multiplier;
		/**
		* Sets the axis of the drive to the variables
		*/
		swerveController.drive(x1, y1, x2);
	}

}