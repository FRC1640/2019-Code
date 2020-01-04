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
import frc.systems.drive.pivot.CVTPivot;
import frc.systems.drive.pivot.Pivot;
import frc.utilities.LogUtil;

public class NormalDriveController implements IDriveController {

	private SwerveController swerveController;
	private Controller driverController;
	private List<Integer> driverCallbackIds;
	double prevServoAngle;
	boolean servoSlow = false;

	public NormalDriveController (SwerveController swerveController) {
		driverController = Devices.getDriverController();
		this.swerveController = swerveController;
		driverCallbackIds = new ArrayList<>();
	}

	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activating");

		driverController.registerButtonListener(ButtonEvent.PRESS, Button.A, () -> {
			setServoSlow(true);
		});
		driverController.registerButtonListener(ButtonEvent.PRESS, Button.X, () -> {
			setServoSlow(false);
		});

		// driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.A, () -> {
		// 	if (Robot.getState() == RobotState.AUTONOMOUS) {
		// 		LogUtil.callback(getClass(), "switch cvt mode to sandstorm");
		// 		swerveController.setCVTMode(CvtMode.SANDSTORM);
		// 	} else {
		// 		LogUtil.callback(getClass(), "switch cvt mode to torque");
		// 		swerveController.setCVTMode(CvtMode.TORQUE);
		// 	}
		// }));

		// driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.B, () -> {
		// 	LogUtil.callback(getClass(), "activate driver precision mode");
		// 	swerveController.setCVTMode(CvtMode.PRECISION);
		// }));

		// driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.X, () -> {
		// 	LogUtil.callback(getClass(), "go to shifting cvt mode");
		// 	swerveController.setCVTMode(CvtMode.SHIFTING);
		// }));

		// driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.SELECT, () -> {
		// 	LogUtil.callback(getClass(), "toggle field centric");
		// 	swerveController.toggleFieldCentric();
		// }));

		// driverController.registerButtonListener(ButtonEvent.PRESS, Button.E, () -> {
		// 	prevServoAngle += 5;
		// 	System.out.println(prevServoAngle);
        // });
        
        // driverController.registerButtonListener(ButtonEvent.PRESS, Button.W, () -> {
		// 	prevServoAngle -= 5;
		// 	System.out.println(prevServoAngle);
		// });
	}

	public void setServoSlow(boolean servoSlow) {
		this.servoSlow = servoSlow;
	}

	public boolean getServoSlow() {
		return servoSlow;
	}

	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivating");
		for (int id : driverCallbackIds) { driverController.unregisterButtonListener(id); }
	}

	@Override
	public void update() {

		// double x1 = driverController.getAxis(Axis.LY);
		// double y1 = driverController.getAxis(Axis.RX);
		// double x2 = driverController.getAxis(Axis.RX);

		// swerveController.testDrive(x1, y1);

		CvtMode cvtMode = swerveController.getCVTMode();
	
			double x1 = driverController.getAxis(Axis.LX);
			double y1 = driverController.getAxis(Axis.LY);
			double x2 = driverController.getAxis(Axis.RX);
			double servos = 0;
			
			if (swerveController == null) {
				LogUtil.error(getClass(), "Swerve Null");
			} else if (cvtMode == null) {
				LogUtil.error(getClass(), "CvtMode Null");
				// swerveController.drive(x1, y1, x2);
			} else {
				double multiplier = cvtMode.getSpeedMultiplier();
				double x2Multiplier = multiplier * (cvtMode == CvtMode.SHIFTING ? 1.0 : 1.0);
				swerveController.drive(x1 * multiplier, y1 * multiplier, x2 * x2Multiplier, servos, getServoSlow());

				// Devices.getPivotMap().forEach((piv, vec) -> { 
				// 	((CVTPivot) piv).setServoAngle(prevServoAngle);
				// });
			}

	}

}