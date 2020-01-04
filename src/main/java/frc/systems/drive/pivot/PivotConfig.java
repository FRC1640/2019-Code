package frc.systems.drive.pivot;

import java.util.HashMap;

import org.json.JSONObject;

import frc.utilities.FileUtil;
import frc.utilities.LogUtil;
import frc.utilities.Vector2;

public class PivotConfig {

	String id;
	String name;
	Vector2 position;

	double minVoltage;
	double maxVoltage;

	int driveChannel;
	int steerChannel;
	int resolverChannel;
	int cvtChannel;

	double offset;
	double rpmMax;

	boolean reverseDrive;
	boolean reverseSteer;
	boolean reverseAngle;

	private PivotConfig () { }

	private static HashMap<String,PivotConfig> cfgMap;

	public static PivotConfig getCfg (String id) {
		return cfgMap.get(id);
	}

	public static void loadConfigs (String path) {
		cfgMap = new HashMap<>();

		String json = FileUtil.readFile(path);
		JSONObject cfgObject = new JSONObject(json);
		
		for (String key : cfgObject.keySet()) {
			JSONObject jo = cfgObject.getJSONObject(key);
			JSONObject posObj = jo.getJSONObject("position");

			PivotConfig pc = new PivotConfig();
			pc.id = key;
			pc.name = jo.getString("name");
			pc.position = new Vector2(posObj.getDouble("x"), posObj.getDouble("y"));

			pc.minVoltage = jo.getDouble("minVoltage");
			pc.maxVoltage = jo.getDouble("maxVoltage");

			pc.driveChannel = jo.getInt("driveChannel");
			pc.steerChannel = jo.getInt("steerChannel");
			pc.resolverChannel = jo.getInt("resolverChannel");
			pc.cvtChannel = jo.getInt("cvtChannel");

			pc.offset = jo.getDouble("offset");
			pc.rpmMax = jo.getDouble("rpmMax");

			pc.reverseDrive = jo.getBoolean("reverseDrive");
			pc.reverseSteer = jo.getBoolean("reverseSteer");
			pc.reverseAngle = jo.getBoolean("reverseAngle");

			cfgMap.put(pc.id, pc);
		}
	}
	
	public void printConfig () {
		LogUtil.log(getClass(), "ID: " + id);
		LogUtil.log(getClass(), "\tName: " + name);
		LogUtil.log(getClass(), "\tPosition: ( " + position.getX() + ", " + position.getY() + " )");
		LogUtil.log(getClass(), "\tMin Voltage: " + minVoltage);
		LogUtil.log(getClass(), "\tMax Voltage: " + maxVoltage);
		LogUtil.log(getClass(), "\tDrive Channel: " + driveChannel);
		LogUtil.log(getClass(), "\tSteer Channel: " + steerChannel);
		LogUtil.log(getClass(), "\tResolver Channel: " + resolverChannel);
		LogUtil.log(getClass(), "\tCVT Channel: " + cvtChannel);
	}

}