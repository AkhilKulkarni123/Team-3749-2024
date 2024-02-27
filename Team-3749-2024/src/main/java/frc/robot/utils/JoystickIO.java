package frc.robot.utils;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.Robot;
import frc.robot.commands.GroundIntake;
import frc.robot.commands.arm.Climb;
import frc.robot.commands.arm.GetConstraints;
// import frc.robot.commands.arm.ArmMoveToGoal;
import frc.robot.commands.swerve.SwerveTeleop;
import frc.robot.commands.wrist.getRegressionData;
import frc.robot.subsystems.arm.ShootKinematics;
// import frc.robot.commands.swerve.MoveToPose;
// import frc.robot.commands.swerve.Teleop;
// import frc.robot.commands.swerve.TeleopJoystickRelative;
import frc.robot.subsystems.swerve.Swerve;

/**
 * Util class for button bindings
 *
 * @author Rohin Sood
 */
public class JoystickIO {

    private CommandXboxController pilot;
    private CommandXboxController operator;

    public JoystickIO() {
        pilot = Robot.pilot;
        operator = Robot.operator;
    }

    /**
     * Calls binding methods according to the joysticks connected
     */
    public void getButtonBindings() {
        if (DriverStation.isJoystickConnected(1)) {
            // if both xbox controllers are connected
            pilotAndOperatorBindings();
        } else if (DriverStation.isJoystickConnected(0)) {
            // if only one xbox controller is connected
            pilotBindings();
        } else if (Robot.isSimulation()) {
            // will show not connected if on sim
            simBindings();
        } else {
            // if no joysticks are connected (ShuffleBoard buttons)

        }
        setDefaultCommands();
    }

    /**
     * If both controllers are plugged in (pi and op)
     */
    public void pilotAndOperatorBindings() {
        pilotBindings();
        // op bindings
    }

    public void pilotBindings() {
        // shooter
        pilot.rightTrigger().whileTrue(Commands.run(() -> Robot.shooter.setShooterVelocity(400.0),
                Robot.shooter));
        pilot.rightTrigger().onFalse(Commands.runOnce(() -> Robot.shooter.setVoltage(0, 0),
                Robot.shooter));

        // intake
        pilot.leftTrigger().whileTrue(Commands.run(() -> Robot.intake.setIntakeVelocity(60),
                Robot.intake));
        pilot.leftTrigger().onFalse(Commands.runOnce(() -> Robot.intake.setVoltage(0),
                Robot.intake));

        pilot.leftBumper().whileTrue(new GroundIntake());

        pilot.x().onTrue(Commands.runOnce(() -> Robot.arm.setGoal(Units.degreesToRadians(0))));
        // pilot.b().onTrue(Commands.runOnce(() ->
        // Robot.arm.setGoal(Units.degreesToRadians(6))));

        pilot.y().onTrue(Commands.runOnce(() -> Robot.arm.setGoal(Units.degreesToRadians(40))));
        pilot.back().whileTrue(new Climb());

        // gyro
        pilot.start().onTrue(Commands.runOnce(() -> Robot.swerve.resetGyro()));

        // // 4bar

        pilot.rightBumper().onTrue(Commands.runOnce(() -> Robot.wrist.setGoalGround()));
        pilot.leftBumper().onTrue(Commands.runOnce(() -> Robot.wrist.setGoalStow()));
        operator.a().whileTrue(Commands.run(() -> Robot.swerve.setChassisSpeeds(
                ChassisSpeeds.fromFieldRelativeSpeeds(new ChassisSpeeds(1, 0, 0), Robot.swerve.getRotation2d())),
                Robot.swerve));
    }

    public void simBindings() {
        // pilot.aWhileHeld(new MoveToPose(new Pose2d(5, 5, new Rotation2d())));
    }

    /**
     * Sets the default commands
     */
    public void setDefaultCommands() {

        Robot.swerve.setDefaultCommand(
                new SwerveTeleop(
                        () -> -pilot.getLeftX(),
                        () -> -pilot.getLeftY(),
                        () -> -pilot.getRightX()));
    }
}
