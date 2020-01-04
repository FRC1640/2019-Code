package frc.systems;

import frc.robot.Robot;
import frc.robot.Robot.RobotState;
import frc.utilities.LogUtil;

public abstract class RobotSystem {

	private String sysName;

	public RobotSystem (final String systemName) {

		this.sysName = systemName;
		
		new Thread (new Runnable () {

			@Override
			public void run () {

				LogUtil.log("RobotSystem", systemName + " starting!");

				try { init(); }
				catch (Exception e) { error(e.getMessage()); e.printStackTrace(); return; }

				LogUtil.log("RobotSystem", systemName + " ready!");

				RobotState lastState = null;

				while (true) {
					try {

						RobotState cState = Robot.getState();
						boolean first = (cState != lastState);

						preStateUpdate();

						switch (cState) {

							case DISABLED: {
								if (first) { disable(); disabledInit(); }
								disabledUpdate();
							} break;

							case TELEOP: {
								if (first) { enable(); teleopInit(); }
								teleopUpdate();
							} break;

							case AUTONOMOUS: {
								if (first) { enable(); autonInit(); }
								teleopUpdate();
							} break;

							case TEST: {
								if (first) { enable(); testInit(); }
								testUpdate();
							} break;

						}

						postStateUpdate();

						Thread.sleep(5);

						lastState = cState;

					} catch (Exception e) {
						error(e.getMessage());
						e.printStackTrace();
					}
				}

			}

		}).start();

	}

	public final void callback (String msg) {
		LogUtil.callback(sysName, msg);
	}

	public final void log (String msg) {
		LogUtil.log(sysName, msg);
	}

	public final void warn (String msg) {
		LogUtil.warn(sysName, msg);
	}

	public final void error (String msg) {
		LogUtil.error(sysName, msg);
	}

	public abstract void init ();

	public abstract void preStateUpdate () throws Exception;

	public abstract void postStateUpdate () throws Exception;

	public abstract void disabledInit () throws Exception;

	public abstract void disabledUpdate () throws Exception;

	public abstract void autonInit () throws Exception;

	public abstract void autonUpdate () throws Exception;

	public abstract void teleopInit () throws Exception;

	public abstract void teleopUpdate () throws Exception;

	public abstract void testInit () throws Exception;

	public abstract void testUpdate () throws Exception;
	
	public abstract void enable () throws Exception;

	public abstract void disable () throws Exception;

}