package frc.robot.drive.pivot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import frc.utilities.FileUtil;

// @RunWith(PowerMockRunner.class)
// @PrepareForTest({FileUtils.class})
public class PivotConfigTest {
	
	@Test
	public void testConfigsGetLoaded () {

		// Setup...
		String path = "testpath";
		// PowerMockito.mockStatic(FileUtils.class);
		// when(FileUtils.readFile(path)).thenReturn(testJsonString());

		// // Run the test...
		// PivotConfig.loadConfigs(path);
		// PivotConfig testCfg = PivotConfig.getCfg("1");

		// // Run assertions...
		// assertTrue("Calling getCfg() should return a PivotConfig instance", testCfg instanceof PivotConfig);
		// assertEquals("ids should match", "1", testCfg.id);
		// assertEquals("names should match", "FL", testCfg.name);
		// assertEquals("x positions should match", 2.0, testCfg.position.getX(), 1e-14);
		// assertEquals("y positions should match", 4.0, testCfg.position.getY(), 1e-14);
		// assertEquals("min voltages should match", 0.20019529200000002, testCfg.minVoltage, 1e-14);
		// assertEquals("max voltages should match", 4.737548343, testCfg.maxVoltage, 1e-14);
		// assertEquals("drive channels should match", 13, testCfg.driveChannel);
		// assertEquals("steer channels should match", 10, testCfg.steerChannel);
		// assertEquals("resolver channels should match", 0, testCfg.resolverChannel);
		// assertEquals("cvt channels should match", 0, testCfg.cvtChannel);
		// assertEquals("offsets should match", 90.0, testCfg.offset, 1e-14);
		// assertEquals("reverse drives should match", false, testCfg.reverseDrive);
		// assertEquals("reverse steers should match", false, testCfg.reverseSteer);
		// assertEquals("reverse angles should match", true, testCfg.reverseAngle);
	}

	// private String testJsonString () {
	// 	return "{\n" +
	// 				"\"1\" : {\n" +
	// 					"\"name\" : \"FL\",\n" +
	// 					"\"position\" : {\n" +
	// 						"\"x\" : 2.0,\n" +
	// 						"\"y\" : 4.0\n" +
	// 					"},\n" +
	// 					"\"minVoltage\" : 0.20019529200000002,\n" +
	// 					"\"maxVoltage\" : 4.737548343,\n" +
	// 					"\"driveChannel\" : 13,\n" +
	// 					"\"steerChannel\" : 10,\n" +
	// 					"\"resolverChannel\" : 0,\n" +
	// 					"\"cvtChannel\" : 0,\n" +
	// 					"\"offset\" : 90,\n" +
	// 					"\"reverseDrive\" : false,\n" +
	// 					"\"reverseSteer\" : false,\n" +
	// 					"\"reverseAngle\" : true,\n" +
	// 				"}\n" +
	// 			"}";
	// }

}