package frc.utilities;

public class Vector2 {

	private static double TWO_PI = Math.PI * 2;

	private double x, y;

	public Vector2 (double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 (Vector2 v) {
		this(v.x, v.y);
	}

	public Vector2 () {
		this(0.0, 0.0);
	}

	public void reset () {
		x = 0.0;
		y = 0.0;
	}

	public Vector2 set (double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector2 set (Vector2 v) {
		x = v.x;
		y = v.y;
		return this;
	}

	public double getX () {
		return x;
	}

	public double getY () {
		return y;
	}

	public Vector2 copy () {
		return new Vector2(this);
	}

	public double magnitude () {
		return Math.sqrt(x*x + y*y);
	}

	public double angleR () {
		return (Math.atan2(y,x) + TWO_PI) % TWO_PI;
	}

	public double angleD () {
		return Math.toDegrees(angleR());
	}

	public Vector2 multiply (double scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}

	public Vector2 add (Vector2 v) {
		x += v.x;
		y += v.y;
		return this;
	}

	public Vector2 subtract (Vector2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}

	public Vector2 negate () {
		multiply(-1.0);
		return this;
	}

	public Vector2 unit () {
		if (magnitude() != 0) {
			multiply(1.0/magnitude());
		}
		return this;
	}

	public Vector2 rotateR (double angleR) {
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		double nx = x * cos - y * sin;
		double ny = x * sin + y * cos;
		x = nx;
		y = ny;
		return this;
	}

	public Vector2 rotateD (double angleD) {
		return rotateR(Math.toRadians(angleD));
	}

	public static Vector2 add (Vector2 v1, Vector2 v2) {
		return v1.copy().add(v2);
	}

	public static Vector2 subtract (Vector2 v1, Vector2 v2) {
		return v1.copy().subtract(v2);
	}

}