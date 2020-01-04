package frc.systems.vision;

import org.opencv.core.Mat;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import frc.systems.RobotSystem;

public class VisionSystem extends RobotSystem {

	UsbCamera usbCamera;
	MjpegServer mjpegServer;
	CvSink cvSink;
	CvSource outputStream;
	Mat source;
	Mat output;

	public VisionSystem() {
		super("Vision System");
	}

	@Override
	public void init() {
		try {
			usbCamera = CameraServer.getInstance().startAutomaticCapture();
		} catch (Exception err) {
			err.printStackTrace();
		}
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

	}

	@Override
	public void autonUpdate() throws Exception {

	}

	@Override
	public void teleopInit() throws Exception {

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
