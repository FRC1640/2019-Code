package frc.systems.drive.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Servo;
import frc.robot.Controller;
import frc.robot.Devices;
import frc.robot.Controller.Button;
import frc.robot.Controller.ButtonEvent;
import frc.systems.drive.pivot.CVTPivot;
import frc.systems.drive.pivot.Pivot;
import frc.utilities.CvtData;
import frc.utilities.DataUtil;
import frc.utilities.LogUtil;
import frc.utilities.Vector2;

public class TestCVTController implements IDriveController {

    HashMap<Pivot,Vector2> pivotMap;
    DataUtil data;
    Encoder encoder;
    CVTPivot cvtPiv;
    long startTime;
    PIDController pidController;
    PIDOutput out;
    PIDSource src;
    double setSpeed = 0;
    Controller driverController;
    double prevServoAngle;
    private List<Integer> driverCallbackIds;
    Servo servo;

    public TestCVTController () throws Exception {
        data = new DataUtil(new File("/home/lvuser/data.csv"));
        encoder = new Encoder(0, 1);
        driverController = Devices.getDriverController();
        driverCallbackIds = new ArrayList<>();
        servo = new Servo(8);

        src = new PIDSource(){
        
            @Override
            public void setPIDSourceType(PIDSourceType pidSource) { }
        
            @Override
            public double pidGet() {
                // TODO Auto-generated method stub
                return 3000 - cvtPiv.getNeoSpeed();
            }
        
            @Override
            public PIDSourceType getPIDSourceType() {
                // TODO Auto-generated method stub
                return PIDSourceType.kRate;
            }
        };

        out = new PIDOutput(){
        
            @Override
            public void pidWrite(double output) {
                setSpeed = -output;
                cvtPiv.setSpeed(-output);
            }
        };

        pidController = new PIDController(1.0/22500, 0, 0.00005, src, out);

        for (Pivot piv : Devices.getPivotMap().keySet()) {
            if (piv.getName().equals("FR")) {
                cvtPiv = ((CVTPivot)piv);
            }
        }
    }

    @Override
    public void activate () {
        LogUtil.log(getClass(), "Activating");
        // pidController.enable();
        startTime = System.currentTimeMillis();
        // cvtPiv.setServoAngle(0);

        driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.E, () -> {
            LogUtil.callback(getClass(), "toggle field centric");
            prevServoAngle += 5;
            servo.setAngle(prevServoAngle);
            System.out.println(prevServoAngle);
        }));
        
        driverCallbackIds.add(driverController.registerButtonListener(ButtonEvent.PRESS, Button.W, () -> {
            LogUtil.callback(getClass(), "toggle field centric");
            prevServoAngle -= 5;
            servo.setAngle(prevServoAngle);
            System.out.println(prevServoAngle);
		}));


    }

    @Override
    public void deactivate () {
        LogUtil.log(getClass(), "Deactivating");
        pidController.disable();
    }

    @Override 
    public void update () {

        // System.out.println(prevServoAngle);

        // if (driverController.getButton(Button.E)) {
        //     prevServoAngle += 5;
        //     cvtPiv.setServoAngle(prevServoAngle);
        // }
        // else if (driverController.getButton(Button.W)) {
        //     prevServoAngle -= 5;
        //     cvtPiv.setServoAngle(prevServoAngle);
        // }

        // long currentTime = System.currentTimeMillis() - startTime;
        // cvtPiv.setServoAngle(180);
        // CvtData cData = new CvtData();
        // cData.neoSpeed = cvtPiv.getNeoSpeed();
        // cData.servoAngle = cvtPiv.getServoAngle();
        // cData.wheelCounts = encoder.getDistance();
        // cData.wheelRate = encoder.getRate();
        // cData.timeStamp = currentTime;
        // cData.setSpeed = setSpeed;
        
        // data.addInput(cData);

    }

}