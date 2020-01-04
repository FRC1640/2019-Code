package frc.systems.vision;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Limelight {

	private NetworkTableEntry tv;
	private NetworkTableEntry tx;
	private NetworkTableEntry ty;
	private NetworkTableEntry ta;
	private NetworkTableEntry tw;
	private NetworkTableEntry th;
	private NetworkTableEntry ta0;
	private NetworkTableEntry ts0;
	private NetworkTableEntry ta1;
	private NetworkTableEntry pipeline;
	private NetworkTableEntry camMode;
	private NetworkTableEntry ledMode;
	private NetworkTableEntry streamMode;
	private NetworkTable table;

	/**
	* Enum for Limelight Targeting Pipelines/Configurations
	*
	* @param value Limelight's pipeline number values in Network Table
	*/
	public enum TargetEnum {

		VISION_TAPE(0),
		CARGO(1);

		public int value;

		TargetEnum (int value) {
			this.value = value;
		}
	}

	/**
	* Enum for Limelight's various camera viewing modes in the camera stream
	*
	* @param value Limelight's stream mode number values in Network Table
	*/
	public enum StreamEnum {
		/**
		* Put the Limelight Camera and USB camera views side by side
		*/
		SIDE_BY_SIDE(0),
		/**
		* Puts Limelight Camera in full view & USB Camera in bottom right corner of camera stream
		*/
		PiP_1(1),
		/**
		* Puts USB Camera in full view & Limelight Camera in bottom right corner of camera stream
		*/
		PiP_2(2);

		public int value;

		StreamEnum (int value) {
			this.value = value;
		}
	}

	/**
	* Enum for possible horizontal directions of the vision tape in relation to camera
	*/
	public static enum DirectionEnum {
		LEFT, RIGHT, NONE
	}

	public static enum LedEnum {
		PIPELINE(0.0),
		FORCE_OFF(1.0),
		FORCE_BLINK(2.0),
		FORCE_ON(3.0);

		public double value;

		LedEnum (double value) {
			this.value = value;
		}
	}

	public Limelight () {
		table = NetworkTableInstance.getDefault().getTable("limelight");
		tv = table.getEntry("tv");
		tx = table.getEntry("tx");
		ta = table.getEntry("ta");
		tw = table.getEntry("thoriz");
		th = table.getEntry("tvert");
		ta0 = table.getEntry("ta0");
		ts0 = table.getEntry("ts0");
		ta1 = table.getEntry("ta1");
		pipeline = table.getEntry("pipeline");
		camMode = table.getEntry("camMode");
		ledMode = table.getEntry("ledMode");
		streamMode = table.getEntry("stream");

		setLEDOn(LedEnum.FORCE_OFF);
		setStreamMode(StreamEnum.PiP_2);
	}

	/**
	* Controls targeting mode/vision pipeline configuration of Limelight
	*
	* @param targetMode Corresponds to what game objects you want to target
	*/
	public void setTargetMode(TargetEnum targetMode) {
		pipeline.setDouble(targetMode.value);
	}

	/**
	* Controls whether or not the Limelight is in streaming/driver view mode or processing mode
	*
	* @param processing Set to true if you want to the Limelight to process the camera feed  
	*/
	public void setProcessing(boolean processing) {
		camMode.setDouble(processing ? 0:1);
	}

	/**
	* Controls LED modes of Limelight
	*
	* @param ledState Change the LED state using LedEnum
	*/
	public void setLEDOn(LedEnum ledState) {
		ledMode.setDouble(ledState.value);
	}

	/**
	* Controls streaming modes of Limelight
	*
	* @param streamMode The camera viewing mode that is output to the stream/Limelight dashboard
	*/
	public void setStreamMode(StreamEnum streamMode) {
		this.streamMode.setDouble(streamMode.value);
	}

	/**
	* @return Returns DirectionEnum representing your Limelights position in relation to the object being tracked
	*/
	public DirectionEnum getTargetSide() {
		/**
		* If statement for when only one side of the vision tape is in view
		*/
		// if (tv.getDouble(0) == 0 && pipeline.getDouble(0.0) == TargetEnum.VISION_TAPE.value) {
		if (tv.getDouble(0) == 0 && pipeline.getDouble(0.0) == TargetEnum.VISION_TAPE.value) {
			if (-ts0.getDouble(0) > 45 && -ts0.getDouble(0) < 90) {
				return DirectionEnum.LEFT;
			} else if (-ts0.getDouble(0) < 45 && -ts0.getDouble(0) > 0) {
				return DirectionEnum.RIGHT;
			}
			return DirectionEnum.NONE;
		}
		if (ta0.getDouble(0) > ta1.getDouble(0) ) { return DirectionEnum.LEFT; }
		else if (ta0.getDouble(0) < ta1.getDouble(0)) { return DirectionEnum.RIGHT; }
		return DirectionEnum.NONE;
	}

	public double getAreaDifference(double differenceThreshold) {
		double difference = ta0.getDouble(0) - ta1.getDouble(0);
		if(Math.abs(difference) < differenceThreshold) {
			return 0.0;
		}
		return difference;
	}

	/**
	* @return Returns Horizontal Offset From Crosshair To Target (-27 degrees to 27 degrees)
	*/
	public double getTargetX(double threshhold) {
		double txK = tx.getDouble(0);
		if (Math.abs(txK) < threshhold) {
			return 0.0;
		}
		return txK;
	}

	/**
	* @return Returns Vertical Offset From Crosshair To Target (-20.5 degrees to 20.5 degrees)
	*/
	public double getTargetY() {
		return ty.getDouble(-9999);
	}

	/**
	* @return Returns Horizontal sidelength of the rough bounding box (0 - 320 pixels)
	*/
	public double getTargetWidth() {
		return tw.getDouble(-9999);
	}

	/**
	* @return Returns Vertical sidelength of the rough bounding box (0 - 320 pixels)
	*/
	public double getTargetHeight() {
		return th.getDouble(-9999);
	}

	/**
	* @return Returns Target Area (0% of image to 100% of image)
	*/
	public double getTargetArea() {
		return ta.getDouble(-9999);
	}
}