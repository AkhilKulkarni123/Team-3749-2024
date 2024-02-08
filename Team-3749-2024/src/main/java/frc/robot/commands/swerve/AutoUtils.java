package frc.robot.commands.swerve;

import java.util.HashMap;
import java.util.function.Consumer;
import com.choreo.lib.Choreo;
import com.choreo.lib.ChoreoTrajectory;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.FollowPathHolonomic;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.util.PathPlannerLogging;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Robot;
import frc.robot.subsystems.swerve.Swerve;
import frc.robot.utils.Constants;

public class AutoUtils {
  private static Swerve swerve = Robot.swerve;
  public static Consumer<Pose2d> pathTargetPose = pose -> swerve.logDesiredOdometry(pose);

  static SendableChooser<Command> autoChooser;
  static SendableChooser<Alliance> allianceChooser;

  public static void initPPUtils() {
    PathPlannerLogging.setLogTargetPoseCallback(pathTargetPose);

    AutoBuilder.configureHolonomic(
        swerve::getPose,
        (Pose2d pose) -> {
        },
        swerve::getChassisSpeeds,
        swerve::setChassisSpeeds,
        Constants.AutoConstants.cfgHolonomicFollower,
        () -> {
          // get alliance
          if (DriverStation.getAlliance().isEmpty())
            return true;

          Alliance robotAlliance = DriverStation.getAlliance().get();

          if (robotAlliance == Alliance.Red) {
            return false;
          } else {
            return true;
          }
        },
        swerve);

    autoChooser = AutoBuilder.buildAutoChooser("Test");

    SmartDashboard.putData("Choose Auto", autoChooser);
  }

  public static void initPathCommands(HashMap<String, Command> commandList) {
    commandList.forEach((String cmdName, Command cmd) -> {
      NamedCommands.registerCommand(cmdName, cmd);
    });
  }

  public static Command getAutoPath() {
    return autoChooser.getSelected().andThen(() -> swerve.stopModules());
  }

  public static Command getAutoPath(String autoPathName) {
    return AutoBuilder.buildAuto(autoPathName).andThen(() -> swerve.stopModules());
  }

  public static Command followPathCommand(PathPlannerPath path) {
    return new FollowPathHolonomic(
        path,
        swerve::getPose,
        swerve::getChassisSpeeds, swerve::setChassisSpeeds,
        Constants.AutoConstants.cfgHolonomicFollower,
        () -> {
          if (DriverStation.getAlliance().isEmpty())
            return true;

          Alliance robotAlliance = DriverStation.getAlliance().get();

          if (robotAlliance == Alliance.Red) {
            return false;
          } else {
            return true;
          }
        },
        swerve);
  }

  public static Command getPathFindToPoseCommand(Pose2d targetPose, PathConstraints constraints,
      double endingVelocity) {

    return AutoBuilder.pathfindToPose(targetPose, constraints, endingVelocity);
  }

  public static Command pathFindToThenFollowTraj(String trajName, PathConstraints constraints) {
    ChoreoTrajectory traj = AutoUtils.getTraj(trajName);
    PathPlannerPath ppPath = PathPlannerPath.fromChoreoTrajectory(trajName);

    // Note from Neel --
    // Should I try to find the direction the robot is heading in and
    // calculate the deltas so that the ending velocity passed into pathFind will
    // be exactly what the inital state will sxet the speeds to.

    Command returnCommand = getPathFindToPoseCommand(traj.getInitialPose(),
        constraints, 0);
    Command pathCommand = followPathCommand(ppPath);

    return returnCommand.andThen(pathCommand);
  }

  public static ChoreoTrajectory getTraj(String trajName) {
    return Choreo.getTrajectory(trajName);
  }

  public static Command timeCommand(Command cmd) {
    Timer timer = new Timer();

    return cmd
        .beforeStarting(() -> timer.start())
        .andThen(() -> {
          timer.stop();
        });
  }
}