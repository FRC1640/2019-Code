package frc.systems.drive.controllers;

import java.util.ArrayList;
import java.util.List;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Axis;
import frc.robot.Controller.Button;
import frc.robot.Controller.ButtonEvent;
import frc.robot.Robot;
import frc.robot.Robot.RobotState;
import frc.systems.drive.controllers.SwerveController.CvtMode;
import frc.utilities.LogUtil;

public class NormalDriveController implements IDriveController {

	private SwerveController swerveController;
	private Controller driverController;
	private List<Integer> driverCallbackIds;

	public NormalDriveController (SwerveController swerveController) {
		driverController = Devices.getDriverController();
		this.swerveController = swerveController;
		driverCallbackIds = new ArrayList<>();
	}

	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activating");

		driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.A, () -> {
			if (Robot.getState() == RobotState.AUTONOMOUS) {
				LogUtil.callback(getClass(), "switch cvt mode to sandstorm");
				swerveController.setCVTMode(CvtMode.SANDSTORM);
			} else {
				LogUtil.callback(getClass(), "switch cvt mode to torque");
				swerveController.setCVTMode(CvtMode.TORQUE);
			}
		}));

		driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.B, () -> {
			LogUtil.callback(getClass(), "activate driver precision mode");
			swerveController.setCVTMode(CvtMode.PRECISION);
		}));

		driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.X, () -> {
			LogUtil.callback(getClass(), "go to shifting cvt mode");
			swerveController.setCVTMode(CvtMode.SHIFTING);
		}));

		driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.SELECT, () -> {
			LogUtil.callback(getClass(), "toggle field centric");
			swerveController.toggleFieldCentric();
		}));
	}

	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivating");
		for (int id : driverCallbackIds) { driverController.unregisterButtonListener(id); }
	}

	@Override
	public void update() {

		CvtMode cvtMode = swerveController.getCVTMode();
	
			double x1 = driverController.getAxis(Axis.LX);
			double y1 = driverController.getAxis(Axis.LY);
			double x2 = driverController.getAxis(Axis.RX);
			
			if (swerveController == null) {
				LogUtil.error(getClass(), "Swerve Null");
			} else if (cvtMode == null) {
				LogUtil.error(getClass(), "CvtMode Null");
				swerveController.drive(x1, y1, x2);
			} else {
				double multiplier = cvtMode.getSpeedMultiplier();
				double x2Multiplier = multiplier * (cvtMode == CvtMode.SHIFTING ? 0.5 : 1.0);
				swerveController.drive(x1 * multiplier, y1 * multiplier, x2 * x2Multiplier);
			}
	}

}