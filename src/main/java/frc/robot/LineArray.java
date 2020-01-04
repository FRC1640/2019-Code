package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;

public class LineArray {

	public static final double separation = 1.5;

	public static enum LAIx {
		L3 (separation * -1.5),
		L2 (separation * -1.5),
		L1 (separation * -1.25),
		C  (0.0),
		R1 (separation * 1.25),
		R2 (separation * 1.5),
		R3 (separation * 1.5)
		;

		private double error;

		LAIx (double error) { this.error = error; }
	}

	private DigitalInput[] sensorArray;

	public LineArray () {
		sensorArray = new DigitalInput[LAIx.values().length];

		sensorArray[LAIx.L3.ordinal()] = new DigitalInput(6);
		sensorArray[LAIx.L2.ordinal()] = new DigitalInput(5);
		sensorArray[LAIx.L1.ordinal()] = new DigitalInput(4);
		sensorArray[LAIx.C.ordinal()]  = new DigitalInput(3);
		sensorArray[LAIx.R1.ordinal()] = new DigitalInput(2);
		sensorArray[LAIx.R2.ordinal()] = new DigitalInput(1);
		sensorArray[LAIx.R3.ordinal()] = new DigitalInput(0);
	}

	public boolean read (LAIx ix) {
		return sensorArray[ix.ordinal()].get();
	}

	/**
	 * Get a "continuous" value for the reading.
	 * 
	 * @return A continuous number... or NaN if no sensors activated.
	 */
	public double getNormalized () {
		int activated = 0; // should be max of 2
		double accumulator = 0;
		for (LAIx ix : LAIx.values()) {
			if (read(ix)) {
				activated++;
				accumulator += ix.error;
			}
		}
		if (activated == 0) { return Double.NaN; }
		if (!centerOnly()) { accumulator += 0.1; }
		return accumulator / activated;
	}

	public String print () {
		String str = "";
		for (LAIx ix : LAIx.values()) {
			str += (read(ix) ? "1" : "0") + " ";
		}
		return str;
	}

	public int getSimplified () {
		if (read(LAIx.L1) || read(LAIx.L2) || read(LAIx.L3)) { return -1; }
		else if (read(LAIx.R1) || read(LAIx.R2) || read(LAIx.R3)) { return 1; }
		return 0;
	}

	public boolean centerOnly () {
		return getSimplified() == 0 && read(LAIx.C);
	}

	public boolean noSensors () {
		return getSimplified() == 0 && !read(LAIx.C);
	}

	// public boolean cargoAlign () {
	// 	return read(LAIx.R1);
	// }

}