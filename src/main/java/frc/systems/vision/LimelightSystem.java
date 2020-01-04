package frc.systems.vision;

import org.opencv.core.Mat;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import frc.robot.Devices;
import frc.systems.RobotSystem;
import frc.systems.vision.Limelight.LedEnum;
import frc.systems.vision.Limelight.StreamEnum;

public class LimelightSystem extends RobotSystem {

	private Limelight limelight;

	public LimelightSystem() {
		super("Limelight System");
		limelight = Devices.getLimelight();
	}

	@Override
	public void init() {
		limelight.setLEDOn(LedEnum.FORCE_OFF);
		limelight.setStreamMode(StreamEnum.PiP_2);
		// limelight.setProcessing(false);
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
		teleopInit();
	}

	@Override
	public void autonUpdate() throws Exception {

	}

	@Override
	public void teleopInit() throws Exception {
		limelight.setLEDOn(LedEnum.FORCE_OFF);
		limelight.setStreamMode(StreamEnum.PiP_2);

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

}
