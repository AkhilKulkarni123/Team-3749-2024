package frc.robot.subsystems.arm;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.subsystems.arm.ArmIO.ArmData;
import frc.robot.utils.Constants;
import frc.robot.utils.Constants.ArmConstants;
import frc.robot.utils.ShuffleData;

public class Arm extends SubsystemBase {

    private ArmData data = new ArmData();
    private ArmIO armIO;

    private ProfiledPIDController profiledFeedbackController = new ProfiledPIDController(ArmConstants.PID.kP,
            ArmConstants.PID.kI,
            ArmConstants.PID.kD,
            ArmConstants.constraints);

    private ArmFeedforward feedForwardController = new ArmFeedforward(ArmConstants.kS,
            ArmConstants.kG,
            ArmConstants.kV);

    private Mechanism2d mechanism = new Mechanism2d(2.5, 2);
    private MechanismRoot2d mechanismArmPivot = mechanism.getRoot("mechanism arm pivot", 1, 0.5);
    private MechanismLigament2d mechanismArm = mechanismArmPivot
            .append(new MechanismLigament2d("mechanism arm", .93, -90));

    private ShuffleData<Double> positionLog = new ShuffleData<Double>("arm", "position",
            0.0);
    private ShuffleData<Double> velocityLog = new ShuffleData<Double>("arm", "velocity",
            0.0);
    private ShuffleData<Double> accelerationLog = new ShuffleData<Double>("arm", "acceleration",
            0.0);
    private ShuffleData<Double> voltageLog = new ShuffleData<Double>("arm", "voltage",
            0.0);
    private ShuffleData<Double> goalLog = new ShuffleData<Double>("arm", "goal",
            0.0);
    private ShuffleData<Double> setpointPositionLog = new ShuffleData<Double>("arm", "setpoint position",
            0.0);
    private ShuffleData<Double> setpointVelocityLog = new ShuffleData<Double>("arm", "setpoint velocity",
            0.0);

    public Arm() {
        if (Robot.isSimulation()) {
            armIO = new ArmSim();
        }
        // profiledFeedbackController.setGoal(Units.degreesToRadians(-90));
    }

    public Rotation2d getRotation2d() {
        return new Rotation2d(data.positionRad);
    }

    public void setGoal(double positionRad) {
        profiledFeedbackController.setGoal(positionRad);
    }

    public double getGoal() {
        return profiledFeedbackController.getGoal().position;
    }

    public State getSetpoint() {
        return profiledFeedbackController.getSetpoint();
    }

    private void moveToSetpoint() {
        State setpoint = profiledFeedbackController.getSetpoint();
        
        double feedback = profiledFeedbackController.calculate(data.positionRad);
        // using data for one and setpoint for the other feels wrong, but it doesn't
        // work if kg isn't relative to its actual position
        double feedforward = feedForwardController.calculate(data.positionRad, setpoint.velocity);
        setVoltage(feedforward + feedback);
    }

    public void setVoltage(double volts){
        armIO.setVoltage(volts);

    }


    // runs every 0.02 sec
    @Override
    public void periodic() {
        armIO.updateData(data);
        moveToSetpoint();

        positionLog.set(getRotation2d().getRadians());
        velocityLog.set(data.velocityRadPerSec);
        accelerationLog.set(data.accelerationRadPerSecSquared);
        voltageLog.set(data.appliedVolts);
        goalLog.set(profiledFeedbackController.getGoal().position);
        setpointPositionLog.set(profiledFeedbackController.getSetpoint().position);
        setpointVelocityLog.set(profiledFeedbackController.getSetpoint().velocity);

        mechanismArm.setAngle(getRotation2d());
        SmartDashboard.putData("mech", mechanism);
    }

}
