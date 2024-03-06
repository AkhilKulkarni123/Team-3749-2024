package frc.robot.commands.superstructure;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Robot;
import frc.robot.subsystems.wrist.WristConstants.WristStates;
import frc.robot.utils.SuperStructureStates;

public class SuperStructureCommands {

    private GroundIntake groundIntake = new GroundIntake();
    private Stow stow = new Stow();
    private ScoreAmp scoreAmp= new ScoreAmp();
    private ScoreSubwoofer scoreSubwoofer = new ScoreSubwoofer();
    private Reset reset = new Reset();
    private SuperStructureCommandInterface currentCommand = stow;

    public SuperStructureCommands() {
    }

    private void switchCommands(SuperStructureCommandInterface command) {
        // System.out.println("ground intake");
        if (currentCommand != command) {
            currentCommand.reset();
            currentCommand = command;
            currentCommand.start();
        }
        
    }

    public void execute() {
        SmartDashboard.putString("state", Robot.state.name());
        SmartDashboard.putString("current command", currentCommand.getClass().getName());
        SuperStructureStates state = Robot.state;
       
        switch (Robot.state) {
            case STOW:
                switchCommands(stow);
                break;
            case GROUND_INTAKE:
                switchCommands(groundIntake);
                break;
            case AMP:
                switchCommands(scoreAmp);
                break;
            case SUBWOOFER:
                switchCommands(scoreSubwoofer);
                break;
            case RESET:
                switchCommands(reset);
                break;
            
        }
        currentCommand.execute();
    }

}