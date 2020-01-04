package frc.systems.drive.controllers;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import frc.robot.Devices;
import frc.systems.drive.controllers.SwerveController.SwerveMode;
import frc.systems.vision.Limelight;
import frc.systems.vision.Limelight.LedEnum;
import frc.systems.vision.Limelight.TargetEnum;
import frc.utilities.LogUtil;

public class LimelightAlignmentAutoController implements IDriveController {

	private enum AutoState {
		DRIVE_TO_TARGET,
		DONE
	};

	private SwerveController swerveController;
	private Limelight limelight;

	private AutoState currentState, nextState;
	private SwerveMode prevSwerveMode;

	private PIDController cameraPid;

	private double cameraX2;
	private double targetDriveAngleD;
	
	private Runnable onComplete;

	public LimelightAlignmentAutoController (SwerveController sc, Runnable onComplete) {
		swerveController = sc;
		limelight = Devices.getLimelight();
		this.onComplete = onComplete;

		cameraPid = new PIDController(0.01, 0.0, 0.0, new PIDSource() {
		
			@Override public void setPIDSourceType(PIDSourceType pidSource) { }

			@Override public PIDSourceType getPIDSourceType() { return PIDSourceType.kDisplacement; }
		
			@Override
			public double pidGet() {
				return -limelight.getTargetX(0.0);
			}
		
		}, (double output) -> {
			cameraX2 = output;
		});
		cameraPid.disable();
	}

	@Override
	public void activate() {
		LogUtil.log(getClass(), "Activating");
		prevSwerveMode = swerveController.getSwerveMode();
		currentState = null;
		nextState = AutoState.DRIVE_TO_TARGET;
		cameraX2 = 0.0;
		LogUtil.log(getClass(), "Target drive angle: " + targetDriveAngleD);
		limelight.setTargetMode(TargetEnum.VISION_TAPE);
		limelight.setProcessing(true);
	}

	@Override
	public void deactivate() {
		LogUtil.log(getClass(), "Deactivating");
		swerveController.setSwerveMode(prevSwerveMode);
		cameraPid.disable();
		limelight.setLEDOn(LedEnum.FORCE_OFF);
	}

	@Override
	public void update() {

		boolean onChange = (nextState != currentState);
		currentState = nextState;

		if (onChange) {
			LogUtil.log(getClass(), "NextState: " + currentState.toString());
		}

		switch (currentState) {

			case DRIVE_TO_TARGET: {
				if (onChange) {
					limelight.setLEDOn(LedEnum.FORCE_ON);
					cameraPid.reset();
					cameraPid.enable();
					swerveController.setSwerveMode(SwerveMode.ROBOT_CENTRIC);
				}
				// swerveController.drive(0.0, 0.75, cameraX2);
				if (limelight.getTargetArea() > 2.5) {
					nextState = AutoState.DONE;
				}
			} break;

			case DONE: {
				if (onChange) {
					cameraPid.disable();
					limelight.setLEDOn(LedEnum.FORCE_OFF);
					onComplete.run();
				}
				// swerveController.drive(0.0, 0.0, 0.0);
			} break;

		}

	}

}