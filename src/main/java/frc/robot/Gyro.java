package frc.robot;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;

public class Gyro {

	private AHRS gyro;

	public Gyro () {
		gyro = new AHRS(SPI.Port.kMXP);
		gyro.reset();
	}

	public double getYaw () {
		double val;
		synchronized (gyro) { val = gyro.getYaw(); }
		// return (-val) % 360;
		return (-val + 360) % 360;
	}

	public void resetGyro () {
		synchronized (gyro) { gyro.reset(); }
	}

	public double getPitch () {
		synchronized (gyro) { return gyro.getPitch(); }
	}

	public double getRoll () {
		synchronized (gyro) { return gyro.getRoll(); }
	}
}