// package frc.utilities;

// import edu.wpi.first.wpilibj.AnalogInput;

// public class ProximitySensor {

// 	AnalogInput sensor;

// 	public enum UnitEnum {
// 		RAW(0.0),
// 		INCHES(2.54),
// 		CENTIMETERS(1.0);

// 		public double value;

// 		UnitEnum (double value) {
// 			this.value = value;
// 		}
// 	}

// 	public ProximitySensor(int port) {
// 		sensor = new AnalogInput(port);
// 	}


// 	/**
// 	 * Roughly calculates proximity sensor distance in desired unit using an exponential curve
// 	 * 
// 	 * @param units An enum value that simply changes the output multiplier
// 	 * @return Returns a double in your desired unit of measurement.
// 	 */
// 	public double getDistance(UnitEnum units) {
// 		if(units == UnitEnum.RAW) {
// 			return sensor.getValue();
// 		}

// 		if (sensor.getValue() < 200 || sensor.getValue() > 2500) {
// 			return 9999;
// 		} else {
// 			// return sensor.getValue();
// 			return exponential(sensor.getValue(), 43.780245315476435, -0.0015322683498001503)/units.value;
// 		}
// 		// return 43.780245315476435 * Math.exp(0.0015322683498001503*sensor.getValue());
// 		// return sensor.getValue();
// 	}

// 	private double exponential (double x, double a, double b) {
// 		return a * Math.pow(Math.E, b * x);
// 	 }

// }