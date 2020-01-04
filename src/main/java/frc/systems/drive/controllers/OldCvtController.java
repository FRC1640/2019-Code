// package frc.systems.drive.controllers;

// import java.util.Collection;

// import frc.systems.drive.pivot.Pivot;

// public class OldCvtController {

// 	private static final double kMaxMotor = (1.0/3);
// 	private static final double kMaxServo = (2.0/3);
// 	private static final double kIdealMotor = 0.5;
// 	private static final int kMinSpeedThreshold = 15000;

// 	public static double calculateMotorSpeed(double drive) {
// 		double direction = Math.signum(drive);
// 		drive = Math.abs(drive);
// 		if (drive <= kMaxMotor) {
// 			return direction*drive*(kIdealMotor/kMaxMotor);
// 		} else if (drive <= kMaxServo) {
// 			return direction*kIdealMotor;
// 		} else {
// 			return (drive-kMaxServo)*kIdealMotor/(1-kMaxServo) + direction*kIdealMotor;
// 		}
// 	}

// 	public static double calculateServoAngle(double drive, double minSpeed) {		
// 		drive = Math.abs(drive);
// 		// if(getCurrent() > MIN_CURRENT_THRESHOLD){
		
// 		//TODO reimplement minspeed
// 		//TODO get this working
// 		if (minSpeed < kMinSpeedThreshold) {
// 			return 0.0;
// 		}
// 		if (drive <= kMaxMotor)
// 			return 0.0;
// //			return kNormalAngle;
// 		else if (drive >= kMaxServo)
// 			return 1.0;
// //			return kMaxAngle;
// 		else
// 			//TODO make this readable
// 			return (drive - kMaxMotor)/(kMaxServo - kMaxMotor);
// //			return (drive - kMaxMotor) * (kMaxAngle - kNormalAngle)
// //					/ (kMaxServo - kMaxMotor) + kNormalAngle;
// 	}

// 	public static double getMinSpeed(Collection<Pivot> pivotSet) {
// 		double minVel = Double.MAX_VALUE;
// 		for (Pivot p : pivotSet) {
// 			double v = Math.abs(p.getInstantVelocity());
// 			if (v == 0.0) { continue; }
// 			minVel = Math.min(minVel, v);
// 		}
// 		if (minVel == Double.MAX_VALUE) { minVel = 0.0; }
		
// 		return minVel;
// 	}

// }