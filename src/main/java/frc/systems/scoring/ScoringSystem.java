package frc.systems.scoring;

import java.util.HashMap;

import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Axis;
import frc.robot.Controller.Button;
import frc.robot.Controller.ButtonEvent;
import frc.systems.RobotSystem;
import frc.systems.grabber.Grabber;
import frc.systems.intake.Intake;
import frc.systems.lift.controllers.ILiftController;
import frc.systems.lift.controllers.LiftManualController;
import frc.systems.hab.LiftNormalController;
import frc.utilities.TimingUtil2;

public class ScoringSystem extends RobotSystem {

    public static enum ScoringState {
        FLOOR,
        CARGO_SHIP_CARGO,
        LOADING_STATION_CARGO,
        ROCKET_CARGO_LOW,
        ROCKET_CARGO_MID,
        ROCKET_CARGO_HIGH,
    }

    private static enum LiftControllers { MANUAL, NORMAL }

    private ScoringState scoringState;
    private Intake intake;
    // private Lift lift;
    private Grabber grabber;
    private Controller driveController;
    private Controller opController;
    private ILiftController liftController;
    private HashMap<LiftControllers,ILiftController> ctrlMap;
    private double speed = 1.0;

    public ScoringSystem () {
        super ("Scoring System");
    }

    @Override
    public void init() {
        scoringState = ScoringState.ROCKET_CARGO_LOW;
        intake = Devices.getIntake();
        grabber = Devices.getGrabber();
        driveController = Devices.getDriverController();
        opController = Devices.getOperatorController();
        ctrlMap = new HashMap<>();

        // ctrlMap.put(LiftControllers.MANUAL, new LiftManualController(opController, lift));
		// ctrlMap.put(LiftControllers.NORMAL, new LiftNormalController(opController, lift));

        // liftController = ctrlMap.get(LiftControllers.MANUAL);
        
        driveController.registerButtonListener(ButtonEvent.PRESS, Button.RT, () -> {
            callback("intake fully extend");
			intake.fullyExtend(); 
		});
		driveController.registerButtonListener(ButtonEvent.RELEASE, Button.RT, () -> {
            callback("intake intermediate extend");
			intake.intermediateExtend();
		});
		driveController.registerButtonListener(ButtonEvent.PRESS, Button.N, () -> {
            callback("intake intermediate extend");
            // System.out.println("Extend Intake");
			intake.intermediateExtend();
		});
		driveController.registerButtonListener(ButtonEvent.PRESS, Button.S, () -> {
            callback("intake retract");
            // System.out.println("Retract Intake");
            intake.retract();
        });

        /**
		 * DRIVER CONTROLS
		 */

		driveController.registerButtonListener(ButtonEvent.PRESS, Button.LT, () -> {
            callback("toggle grabber extended");
            grabber.toggleExtend();
        });
		driveController.registerButtonListener(ButtonEvent.PRESS, Button.LB, () -> {
            callback("toggle claw");
            grabber.toggleClaw();
        });

		/**
		 * OPERATOR CONTROLS
		 */

		opController.registerButtonListener(ButtonEvent.PRESS, Button.LT, () -> {
            callback("toggle grabber extended");
            grabber.toggleExtend();
        });
		opController.registerButtonListener(ButtonEvent.PRESS, Button.RT, () -> {
            callback("toggle claw");
            grabber.toggleClaw();
        });
    }

    @Override
    public void preStateUpdate() throws Exception {

    }

    @Override
    public void postStateUpdate() throws Exception {

    }

    @Override
    public void disabledInit() throws Exception {

    }

    @Override
    public void disabledUpdate() throws Exception {

    }

    @Override
    public void autonInit() throws Exception {

    }

    @Override
    public void autonUpdate() throws Exception {

    }

    @Override
    public void teleopInit() throws Exception {

    }

    @Override
    public void teleopUpdate() throws Exception {
        // System.out.println(lift.getLiftHeightInInches());
        // System.out.println(scoringState);
        
        if (driveController.getButton(Button.RT)) {
			intake.setIntakeSpeed(-speed, true);
		}
		else if (driveController.getButton(Button.RB)) { //||opController.getButton(Button.RB)
			intake.setIntakeSpeed(speed, true);
			intake.ejectGrabber(speed);
		}
		else {
			intake.setIntakeSpeed(0.0, false);
		}

        if (opController.getButton(Button.SELECT)) { //different button
            // lift.setRaw(opController.getAxis(Axis.LY));
        }
        else {
            switch (scoringState) {
                case FLOOR: {
                    intake.intermediateExtend();
                    // TimingUtil2.getInstance().registerOneTimeCallback(1000, () -> { lift.setTarget(LiftTarget.FLOOR_OR_CARGO_FROM_FLOOR);});
                    if (opController.getButton(Button.W)) {
                        scoringState = ScoringState.ROCKET_CARGO_MID;
                    }
                    if (opController.getButton(Button.N)) {
                        scoringState = ScoringState.ROCKET_CARGO_HIGH;
                    }
                    if (opController.getButton(Button.Y)) {
                        scoringState = ScoringState.CARGO_SHIP_CARGO;
                    }
                    if (opController.getButton(Button.LB)) {
                        scoringState = ScoringState.LOADING_STATION_CARGO;
                    }
                    if (opController.getButton(Button.E)) {
                        scoringState = ScoringState.ROCKET_CARGO_LOW;
                    }
                    // intake.setIntakeSpeed(driveController.getAxis(Axis.LT) - driveController.getAxis(Axis.RT), driveController.getButton(Button.A));
                    // if (driveController.getButton(Button.Y)) { intake.ejectGrabber(speed); }
                } break;
                case CARGO_SHIP_CARGO: {
                    // lift.setTarget(LiftTarget.CARGO_SHIP_CARGO);
                    TimingUtil2.registerOneTimeCallback(1000, () -> { intake.retract();});
                    if (opController.getButton(Button.LB)) {
                        scoringState = ScoringState.LOADING_STATION_CARGO;
                    }
                    if (opController.getButton(Button.RB)) {
                        scoringState = ScoringState.ROCKET_CARGO_LOW;
                    }
                    if (opController.getButton(Button.W)) {
                        scoringState = ScoringState.ROCKET_CARGO_MID;
                    }
                    if (opController.getButton(Button.E)) {
                        scoringState = ScoringState.ROCKET_CARGO_HIGH;
                    }
                    if (opController.getButton(Button.S)) {
                        scoringState = ScoringState.FLOOR;
                    }
                    // intake.setIntakeSpeed(driveController.getAxis(Axis.LT) - driveController.getAxis(Axis.RT), driveController.getButton(Button.A));
                    // if (driveController.getButton(Button.Y)) { intake.ejectGrabber(speed); }
                } break;
                case LOADING_STATION_CARGO: {
                    // lift.setTarget(LiftTarget.CARGO_LOADING_STATION);
                    TimingUtil2.registerOneTimeCallback(1000, () -> { intake.retract();});
                    if (opController.getButton(Button.RB)) {
                        scoringState = ScoringState.ROCKET_CARGO_LOW;
                    }
                    if (opController.getButton(Button.W)) {
                        scoringState = ScoringState.ROCKET_CARGO_MID;
                    }
                    if (opController.getButton(Button.E)) {
                        scoringState = ScoringState.ROCKET_CARGO_HIGH;
                    }
                    if (opController.getButton(Button.Y)) {
                        scoringState = ScoringState.CARGO_SHIP_CARGO;
                    }
                    if (opController.getButton(Button.S)) {
                        scoringState = ScoringState.FLOOR;
                    }
                    // intake.setIntakeSpeed(driveController.getAxis(Axis.LT) - driveController.getAxis(Axis.RT), driveController.getButton(Button.A));
                    // if (driveController.getButton(Button.Y)) { intake.ejectGrabber(speed); }
                } break;
                case ROCKET_CARGO_LOW: {
                    // lift.setTarget(LiftTarget.L1_CARGO);
                    TimingUtil2.registerOneTimeCallback(1000, () -> { intake.retract();});
                    if (opController.getButton(Button.RB)) {
                        scoringState = ScoringState.LOADING_STATION_CARGO;
                    }
                    if (opController.getButton(Button.W)) {
                        scoringState = ScoringState.ROCKET_CARGO_MID;
                    }
                    if (opController.getButton(Button.E)) {
                        scoringState = ScoringState.ROCKET_CARGO_HIGH;
                    }
                    if (opController.getButton(Button.Y)) {
                        scoringState = ScoringState.CARGO_SHIP_CARGO;
                    }
                    if (opController.getButton(Button.S)) {
                        scoringState = ScoringState.FLOOR;
                    }
                    if (driveController.getButton(Button.LT)) {
                        grabber.toggleExtend();
                    }
                    if (driveController.getButton(Button.RB)) {
                        grabber.toggleClaw();
                    }
                    // if (opController.getButton(Button.LT)) {
                    //     grabber.toggleExtend();
                    // }
                    // if (opController.getButton(Button.RT)) {
                    //     grabber.toggleClaw();
                    // }
                    // intake.setIntakeSpeed(driveController.getAxis(Axis.LT) - driveController.getAxis(Axis.RT), driveController.getButton(Button.A));
                    // if (driveController.getButton(Button.Y)) { intake.ejectGrabber(speed); }
                } break;
                case ROCKET_CARGO_MID: {
                    // lift.setTarget(LiftTarget.L2_CARGO);
                    TimingUtil2.registerOneTimeCallback(1000, () -> { intake.retract();});
                    if (opController.getButton(Button.RB)) {
                        scoringState = ScoringState.LOADING_STATION_CARGO;
                    }
                    if (opController.getButton(Button.W)) {
                        scoringState = ScoringState.ROCKET_CARGO_LOW;
                    }
                    if (opController.getButton(Button.E)) {
                        scoringState = ScoringState.ROCKET_CARGO_HIGH;
                    }
                    if (opController.getButton(Button.Y)) {
                        scoringState = ScoringState.CARGO_SHIP_CARGO;
                    }
                    if (opController.getButton(Button.S)) {
                        scoringState = ScoringState.FLOOR;
                    }
                    if (driveController.getButton(Button.LT)) {
                        grabber.toggleExtend();
                    }
                    if (driveController.getButton(Button.RB)) {
                        grabber.toggleClaw();
                    }
                    // if (opController.getButton(Button.LT)) {
                    //     grabber.toggleExtend();
                    // }
                    // if (opController.getButton(Button.RT)) {
                    //     grabber.toggleClaw();
                    // }
                    // intake.setIntakeSpeed(driveController.getAxis(Axis.LT) - driveController.getAxis(Axis.RT), driveController.getButton(Button.A));
                    // if (driveController.getButton(Button.Y)) { intake.ejectGrabber(speed); }
                } break;
                case ROCKET_CARGO_HIGH: {
                    // lift.setTarget(LiftTarget.L3_CARGO);
                    TimingUtil2.registerOneTimeCallback(1000, () -> { intake.retract();});
                    if (opController.getButton(Button.RB)) {
                        scoringState = ScoringState.LOADING_STATION_CARGO;
                    }
                    if (opController.getButton(Button.W)) {
                        scoringState = ScoringState.ROCKET_CARGO_LOW;
                    }
                    if (opController.getButton(Button.E)) {
                        scoringState = ScoringState.ROCKET_CARGO_MID;
                    }
                    if (opController.getButton(Button.Y)) {
                        scoringState = ScoringState.CARGO_SHIP_CARGO;
                    }
                    if (opController.getButton(Button.S)) {
                        scoringState = ScoringState.FLOOR;
                    }
                    if (driveController.getButton(Button.LT)) {
                        grabber.toggleExtend();
                    }
                    if (driveController.getButton(Button.RB)) {
                        grabber.toggleClaw();
                    }
                    // if (opController.getButton(Button.LT)) {
                    //     grabber.toggleExtend();
                    // }
                    // if (opController.getButton(Button.RT)) {
                    //     grabber.toggleClaw();
                    // }
                    // intake.setIntakeSpeed(driveController.getAxis(Axis.LT) - driveController.getAxis(Axis.RT), driveController.getButton(Button.A));
                    // if (driveController.getButton(Button.Y)) { intake.ejectGrabber(speed); }
                } break;
            }
        }

        if (opController.getButton(Button.START)) {
            // lift.zeroLiftEncoder();
		}

        // ILiftController newLiftController;

        // if (opController.getButton(Button.SELECT)) {
		// 	newLiftController = ctrlMap.get(LiftControllers.NORMAL);
        // }
        // else {
        //     newLiftController = ctrlMap.get(LiftControllers.MANUAL);
        // }

        // if (newLiftController != liftController) {
		// 	liftController.disable();
		// 	newLiftController.enable();
		// 	liftController = newLiftController;
		// }

        // liftController.update();
        
        
    }

    @Override
    public void testInit() throws Exception {

    }

    @Override
    public void testUpdate() throws Exception {

    }

    @Override
    public void enable() throws Exception {
        // lift.enable();
    }

    @Override
    public void disable() throws Exception {
        // lift.disable();
    }

}