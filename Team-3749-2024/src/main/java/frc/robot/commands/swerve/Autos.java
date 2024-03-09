package frc.robot.commands.swerve;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.GeometryUtil;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.Robot;
import frc.robot.utils.MiscConstants;

public class Autos {

  public static Command getStraightLine() {
    return AutoUtils.getAutoPath(
        "straight line",
        new Pose2d(4, 5, new Rotation2d()));
  }

  public static Command get4Piece() {
    Pose2d startingPos = new Pose2d(new Translation2d(1.318, 5.436), Rotation2d.fromRadians(0));

    return AutoUtils.getChoreoAutoPath(
        "middle-speaker-3xwing_speaker", startingPos);
  }

  public static Command get4PieceNoRotation() {
    Pose2d startingPos = new Pose2d(new Translation2d(1.318, 5.436), Rotation2d.fromRadians(0));

    return AutoUtils.getChoreoAutoPath(
        "no rotation", startingPos);
  }

  public static Command getTroll() {
    return new SequentialCommandGroup(
        AutoUtils.getCycle(0),
        new WaitCommand(3),
        AutoUtils.getTroll(),
        AutoUtils.getAutoPath(
            "top-speaker-troll",
            new Pose2d(1.32, 5.44, new Rotation2d())));
  }
}
// public static Command getSide() {
// // return AutoUtils.getCycle(0);
// return new SequentialCommandGroup(AutoUtils.getCycle(0), new WaitCommand(4)
// AutoUtils.getAutoPath("4 piece middle solo", new Pose2d(1.32, 5.44, new
// Rotation2d())),
// AutoUtils.getCycle(8));
// }
