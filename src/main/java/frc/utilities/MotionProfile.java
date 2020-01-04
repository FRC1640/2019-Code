package frc.utilities;

public class MotionProfile {

	private double acc;
	private double vel;
	private double dec;

	public static MotionProfile createTrapezoidalProfile (double acc, double vel, double dec) {
		MotionProfile mp = new MotionProfile();

		mp.acc = acc;
		mp.vel = vel;
		mp.dec = dec;

		return mp;
	}

	public static MotionProfile createTrapezoidalProfile (double vel, double acc) {
		return createTrapezoidalProfile(acc, vel, acc);
	}

	private MotionProfile () {
		acc = vel = dec = 0.0;
	}

	public MotionPath createPath (double d, double v0, double vf) {
		double vInf 	= Math.sqrt((vf * vf - dec * (v0 * v0 / acc + 2 * d)) / (1 - dec/acc));
		double vConst 	= Math.min(vInf, vel);

		MotionPath mp = new MotionPath();

		mp.v0		= v0;
		mp.vf		= vf;
		mp.vc		= vConst;
		mp.tAcc 	= (1/acc) * (vConst - v0);
		mp.tDec 	= (1/dec) * (vf - vConst);
		mp.dAcc 	= (vConst * vConst - v0 * v0) / (2 * acc);
		mp.dDec 	= (vf * vf - vConst * vConst) / (2 * dec);
		mp.dConst 	= (vInf > vConst) ? d - mp.dAcc - mp.dDec : 0;
		mp.tConst 	= (vInf > vConst) ? mp.dConst / vConst : 0;

		return mp;
	}

	public class MotionPath {

		private double v0, vf, vc;
		private double tAcc, tConst, tDec;
		private double dAcc, dConst, dDec;
		private double vSign = Math.signum(vel);

		private MotionPath () { }

		public double getExpectedDisplacement (double time) {
			double t = time;
			if (t < tAcc) { return ( 0.5 * acc * t * t ) + ( v0 * t ); }
			else if (t < tAcc + tConst) { return ( vc * t ) + dAcc; }
			else { return ( 0.5 * dec * t * t ) + ( vc * t ) + ( dAcc + dConst ); }
		}

		public double getExpectedPower (double time) {
			return (time < tAcc + tConst) ? 1.0 * vSign : 0.0;
		}

	}

}