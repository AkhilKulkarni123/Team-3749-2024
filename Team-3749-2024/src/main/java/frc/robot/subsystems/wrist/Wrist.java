package frc.robot.subsystems.wrist;

import java.util.HashMap;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.subsystems.wrist.WristIO.WristData;
import frc.robot.utils.Constants;

public class Wrist extends SubsystemBase {

    private WristIO wristModule;
    private WristData data = new WristData();
    private ProfiledPIDController wristController = new ProfiledPIDController(Constants.WristConstants.PID.kP,
            Constants.WristConstants.PID.kI, Constants.WristConstants.PID.kD,
            Constants.WristConstants.trapezoidConstraint);
    private SimpleMotorFeedforward wristFF = new SimpleMotorFeedforward(1, 0);
    private HashMap<Boolean, Double> setpointToggle = new HashMap<Boolean, Double>();
    private boolean isGroundIntake = false;
    private Mechanism2d mechanism = new Mechanism2d(2.5, 2);
    private MechanismRoot2d mechanismArmPivot = mechanism.getRoot("mechanism arm pivot", 1, 0.5);
    private MechanismLigament2d mechanismArm = mechanismArmPivot
            .append(new MechanismLigament2d("mechanism arm", .93, 0));

    public Wrist() {
        setpointToggle.put(true, Constants.WristConstants.groundGoal);
        setpointToggle.put(false, Constants.WristConstants.stowGoal);
        wristModule = new WristSparkMax();
        if (Robot.isSimulation()) {
            wristModule = new WristSim();
        }
    }

    public void toggleWristGoal() {
        this.isGroundIntake = !this.isGroundIntake;
        wristController.setGoal(setpointToggle.get(this.isGroundIntake));
    }

    public State getWristGoal() {
        return wristController.getGoal();
    }

    public State getWristSetpoint() {
        return wristController.getSetpoint();
    }

    public void moveWristToAngle() {

        SmartDashboard.putNumber("wristGoal", getWristGoal().position);
        wristModule.setVoltage(
                wristController.calculate(data.positionRad) +
                        wristFF.calculate(wristController.getSetpoint().velocity));
        mechanismArm.setAngle(-Math.toDegrees(data.positionRad));
        // System.out.println(wristController.getGoal().position);
    }

    @Override
    public void periodic() {
        SmartDashboard.putData("Mech2d", mechanism);
        wristModule.updateData(data);
    }

}
