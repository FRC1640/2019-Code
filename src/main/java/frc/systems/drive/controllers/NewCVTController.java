// package frc.systems.drive.controllers;

// import edu.wpi.first.wpilibj.Encoder;
// import edu.wpi.first.wpilibj.PIDController;
// import edu.wpi.first.wpilibj.PIDOutput;
// import edu.wpi.first.wpilibj.PIDSource;
// import edu.wpi.first.wpilibj.PIDSourceType;
// import frc.robot.Controller;
// import frc.robot.Devices;
// import frc.robot.Controller.Axis;
// import frc.systems.drive.DriveSystem.DriveController;
// import frc.systems.drive.pivot.CVTPivot;
// import frc.systems.drive.pivot.Pivot;
// import frc.utilities.LogUtil;

// public class NewCVTController {

//     PIDController pidController;
//     PIDOutput out;
//     PIDSource src;
//     CVTPivot piv;
//     double speed = 0;
//     Encoder encoder;
//     double eSpeed = 3000;
//     Controller controller;

//     public NewCVTController () {

//         encoder = new Encoder(0, 1);
//         controller = Devices.getDriverController();

//         src = new PIDSource(){
        
//             @Override
//             public void setPIDSourceType(PIDSourceType pidSource) { }
        
//             @Override
//             public double pidGet() {
//                 // TODO Auto-generated method stub
//                 return 3000 - piv.getNeoSpeed();
//             }
        
//             @Override
//             public PIDSourceType getPIDSourceType() {
//                 // TODO Auto-generated method stub
//                 return PIDSourceType.kRate;
//             }
//         };

//         out = new PIDOutput(){
        
//             @Override
//             public void pidWrite(double output) {
//                 piv.setSpeed(-output);
//             }
//         };

//         pidController = new PIDController(1.0/22500, 0, 0.00005, src, out);

//         for (Pivot piv : Devices.getPivotMap().keySet()) {
//                 piv = ((CVTPivot)piv);
//         }

//     }
    
//     public void update () {

//         speed = piv.getNeoSpeed();

//         if (Math.abs(speed) < Math.abs(eSpeed) - 500) {

//             piv.setServoAngle(155);

//         }

//         else if (Math.abs(speed) >= Math.abs(eSpeed) - 500) {

//             pidController.enable();
//             piv.setServoAngle(0);

//         }

//         else if (speed > eSpeed) {

//             pidController.disable();

//         }

//     }

// }