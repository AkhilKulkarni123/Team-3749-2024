package frc.robot.utils;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.Robot;
// import frc.robot.commands.arm.ArmMoveToGoal;
import frc.robot.commands.swerve.Teleop;
import frc.robot.commands.arm.ArmMoveToGoal;
import frc.robot.commands.arm.GetConstraints;
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

    private Xbox pilot;
    private Xbox operator;


    public JoystickIO(Xbox pilot, Xbox operator) {
        this.pilot = pilot;
        this.operator = operator;
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

        //op bindings
    }


    public void pilotBindings() {
        //sysid config

        //pilot commandse
    }

    public void simBindings() {
        // pilot.aWhileHeld(new MoveToPose(new Pose2d(5, 5, new Rotation2d())));
    }

    /**
     * Sets the default commands
     */
    public void setDefaultCommands() {
        Robot.arm.setDefaultCommand(new ArmMoveToGoal(() -> true));

        // Robot.arm.setDefaultCommand(new ArmMoveToGoal(() -> Robot.wrist.getIsGroundIntake()));

        Robot.swerve.setDefaultCommand(
                new Teleop(() -> -pilot.getLeftX(), () -> -pilot.getLeftY(), () -> -pilot.getRightX()));
    }
}