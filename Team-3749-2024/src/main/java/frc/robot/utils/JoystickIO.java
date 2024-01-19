package frc.robot.utils;

import java.util.Map;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Robot;
import frc.robot.commands.WristCommand;

/**
 * Util class for button bindings
 * 
 * @author Rohin Sood
 */
public class JoystickIO {
    private static String[] lastJoystickNames = new String[] { "", "", "", "", "", "" };

    private Xbox pilot; 
    private Xbox operator;

    public JoystickIO(Xbox pilot, Xbox operator) {
        this.pilot = pilot;
        this.operator = operator;
    }

    public static boolean didJoysticksChange() {
        boolean joysticksChanged = false;
        for (int port = 0; port < DriverStation.kJoystickPorts; port++) {
            String name = DriverStation.getJoystickName(port);
            if (!name.equals(lastJoystickNames[port])) {
                joysticksChanged = true;
                lastJoystickNames[port] = name;
            }
        }
        return joysticksChanged;
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

        } else {
            // if no joysticks are connected (ShuffleBoard buttons)
            noJoystickBindings();
        }

        setDefaultCommands();
    }

    /**
     * If both controllers are plugged in (pi and op)
     */
    public void pilotAndOperatorBindings() {


    }

    /**
     * If only one controller is plugged in (pi)
     */
    public void pilotBindings() {
        
    }

    /**
     * If NO joysticks are plugged in (Buttons for commands are runnable in the
     * "Controls" tab in ShuffleBoard)
     */
    public void noJoystickBindings() {
        ShuffleboardTab controlsTab = Shuffleboard.getTab("Controls");

        // Example
        ShuffleboardLayout armCommands = controlsTab
                .getLayout("Arm", BuiltInLayouts.kList)
                .withSize(2, 2)
                .withProperties(Map.of("Label position", "HIDDEN")); // hide labels for commands


    }

    /**
     * Sets the default commands
     */
    public void setDefaultCommands() {
       
       pilot.a().onTrue(new WristCommand());

       pilot.rightTrigger().onTrue(Commands.runOnce(() -> Robot.shintakeSpark.setShooterVelocity(Constants.ShintakeConstants.shooterVelocity)));
       pilot.rightTrigger().onFalse(Commands.runOnce(() -> Robot.shintakeSpark.setShooterVelocity(Constants.ShintakeConstants.shooterIdleVoltage)));

       pilot.leftTrigger().onTrue(Commands.runOnce(() -> Robot.shintakeSpark.setIntakeVelocity(Constants.ShintakeConstants.intakeVelocity)));
       pilot.leftTrigger().onFalse(Commands.runOnce(() -> Robot.shintakeSpark.setIntakeVelocity(Constants.ShintakeConstants.intakeIdleVoltage)));
        
    }
        
}
