package frc.robot.subsystems.swerve.real;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.swerve.SwerveModuleIO;
import frc.robot.subsystems.swerve.SwerveModuleIO.ModuleData;
import frc.robot.utils.Constants;
import frc.robot.utils.Constants.ModuleConstants;
import frc.robot.utils.Constants.Sim;

/* 
Very closely inspired by 6328's Swerve Sim code,
 https://github.com/Mechanical-Advantage/RobotCode2023/blob/main/src/main/java/org/littletonrobotics/frc2023/subsystems/drive/ModuleIOSim.java
*/
public class SwerveModuleSparkMax implements SwerveModuleIO {
    private CANSparkMax driveMotor = new CANSparkMax(0, CANSparkMax.MotorType.kBrushless);
    private CANSparkMax turnMotor = new CANSparkMax(0, CANSparkMax.MotorType.kBrushless);
    private double turnPositionRad = 0;
    private double driveAppliedVolts = 0.0;
    private double turnAppliedVolts = 0.0;

    public SwerveModuleSparkMax() {
        System.out.println("[Init] Creating ModuleIOSim");
    }

    @Override
    public void updateData(ModuleData data) {
        // update sim values
        // how far have we turned in the previous loop?
        double driveRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(driveMotor.getEncoder().getVelocity()); 
        double turnRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(turnMotor.getEncoder().getVelocity());
        double angleDiffRad = turnRadPerSec * Sim.loopPeriodSec;
        
        // update our angle variables
        turnPositionRad += angleDiffRad;
        // keep our absolute position within 0-2 pi
        while (turnPositionRad < 0) {
            turnPositionRad += 2.0 * Math.PI;
        }
        while (turnPositionRad > 2.0 * Math.PI) {
            turnPositionRad -= 2.0 * Math.PI;
        }
        // distance traveled + Rad/Time * Time * diameter
        data.drivePositionM = data.drivePositionM
                + (turnRadPerSec * 0.02 * ModuleConstants.wheelDiameterMeters) / 2;
        data.driveVelocityMPerSec = driveRadPerSec * ModuleConstants.wheelDiameterMeters / 2;
        data.driveAppliedVolts = driveAppliedVolts;
        data.driveCurrentAmps = Math.abs(driveMotor.getOutputCurrent());
        data.driveTempCelcius = 0;

        data.turnAbsolutePositionRad = turnPositionRad;
        data.turnVelocityRadPerSec = turnRadPerSec;
        data.turnAppliedVolts = turnAppliedVolts;
        data.turnCurrentAmps = Math.abs(turnMotor.getOutputCurrent());
        data.turnTempCelcius = 0;


    }
    @Override
    public void setDriveVoltage(double volts) {
        driveAppliedVolts = MathUtil.clamp(volts, -8.0, 8.0);
        driveMotor.setVoltage(driveAppliedVolts);
    }
    @Override
    public void setTurnVoltage(double volts) {
        turnAppliedVolts = MathUtil.clamp(volts, -8.0, 8.0);
        turnMotor.setVoltage(turnAppliedVolts);
    }
}