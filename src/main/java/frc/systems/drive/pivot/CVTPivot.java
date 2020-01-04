package frc.systems.drive.pivot;

import frc.utilities.MathUtil;

import edu.wpi.first.wpilibj.Servo;

public class CVTPivot extends Pivot {
	
	private Servo servo;

	public  static final double kMinAngle 		= 40; // 50.0;
	public  static final double kMaxAngle 		= 135; // 130.0;
	public  static final double kNeutralAngle 	= (kMaxAngle + kMinAngle) / 2.0; // 120;
	private static final double kRange 			= (kMaxAngle - kMinAngle);
	private static final double kHalfRange 		= kRange / 2.0;

	public CVTPivot (String id) {
		super(id);
		servo = new Servo(config.cvtChannel);
		setTransmission(0.4);
	}
	
	/**
	 * 0 - 1 range
	 * @param transmission
	 */
	public void setTransmission (double transmission) {
		transmission = MathUtil.constrain(transmission, 0.0, 1.0);
		transmission = -(transmission * kRange) + kMaxAngle;
		// transmission = kNeutralAngle + (kHalfRange * transmission);
		// System.out.println(transmission);

		servo.setAngle(transmission);
	}

	public void setServoAngle (double angle) {
		servo.setAngle(angle);
	}
	
	public double getTransmission () {
		return servo.getAngle();
	}

	public double getServoAngle () {
		return servo.getAngle();
	}

}
