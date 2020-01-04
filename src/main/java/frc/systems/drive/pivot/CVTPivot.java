package frc.systems.drive.pivot;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Button;
import frc.robot.Controller.ButtonEvent;
import frc.systems.drive.controllers.NormalDriveController;
import frc.utilities.MathUtil;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Servo;

public class CVTPivot extends Pivot {
	
	private Servo servo;

	public  static final double kMinAngle 		= 25; // 50.0;
	public  static final double kMaxAngle 		= 145; // 130.0;
	public  static final double kNeutralAngle 	= (kMaxAngle + kMinAngle) / 2.0; // 120;
	private static final double kRange 			= (kMaxAngle - kMinAngle);
	private static final double kHalfRange 		= kRange / 2.0;
	
	PIDController pidController;
    PIDOutput out;
    PIDSource src;
    double speed = 0;
    Encoder encoder;
	double eSpeed = 3000;
	boolean isPID = false;
	CVTPivot self;

	public CVTPivot (String id) {
		super(id);
		servo = new Servo(config.cvtChannel);
		setTransmission(0.0);

		// encoder = new Encoder(0, 1);

        src = new PIDSource(){
        
            @Override
            public void setPIDSourceType(PIDSourceType pidSource) { }
        
            @Override
            public double pidGet() {
                // TODO Auto-generated method stub
                return eSpeed - getNeoSpeed();
            }
        
            @Override
            public PIDSourceType getPIDSourceType() {
                // TODO Auto-generated method stub
                return PIDSourceType.kRate;
            }
        };

        out = new PIDOutput(){
        
            @Override
            public void pidWrite(double output) {
                setMotorDirect(-output);
            }
        };

		pidController = new PIDController(1.0/22500, 0, 0.00005, src, out);
		
		pidController.disable();

	}
	
	/**
	 * 0 - 1 range
	 * @param transmission
	 */
	public void setTransmission (double transmission) {
		transmission = MathUtil.constrain(transmission, 0.0, 1.0);
		transmission *= kRange;
		// transmission = -(transmission * kRange) + kMaxAngle;
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

	public void setSpeed (double speed, boolean isSlow) {

		if (getNeoSpeed() > 3500 && isSlow == false) {
			setTransmission(Math.pow(speed, 8));
		} 
		else if (isSlow == true) {
			setTransmission(0.0); 
		}
		else {
			setTransmission(0.0); 
		}
		super.setSpeed(speed);
		
		// double neoSpeed = getNeoSpeed();

		// 	if (servo.getAngle() < 150) {

		// 		pidController.disable();
		// 		setMotorDirect(speed);
		// 		System.out.println("1");

		// 	}

		// 	else if (Math.abs(neoSpeed) < Math.abs(eSpeed) - 500) {

		// 		pidController.disable();
		// 		setMotorDirect(speed);
		// 		setServoAngle(155);
		// 		System.out.println("2");

		// 	}

		// 	else if (Math.abs(neoSpeed) >= Math.abs(eSpeed) - 500) {

		// 		pidController.enable();
		// 		setServoAngle(0);
		// 		System.out.println("3");

		// 	}
		// 	// System.out.println(getServoAngle());
		// 	System.out.println(getNeoSpeed());
	}

}
