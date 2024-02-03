// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.swerve;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.subsystems.swerve.GyroIO.GyroData;
import frc.robot.subsystems.swerve.sim.GyroSim;
import frc.robot.subsystems.swerve.sim.SwerveModuleSim;
import frc.robot.subsystems.swerve.real.NavX2Gyro;
import frc.robot.subsystems.swerve.real.SwerveModuleRelative;
import frc.robot.utils.Constants;
import frc.robot.utils.ShuffleData;
import frc.robot.utils.Constants.DriveConstants;

/***
 * @author Noah Simon
 * @author Rohin Sood
 * @author Raadwan Masum
 * @author Harkirat
 * 
 *         Subsystem class for swerve drive, used to manage four swerve modules
 *         and set their states. Also includes a pose estimator, gyro, and
 *         logging information
 */
public class Swerve extends SubsystemBase {
  private SwerveModule[] modules = new SwerveModule[4];

  private GyroIO gyro;
  private GyroData gyroData = new GyroData();
  // equivilant to a odometer, but also intakes vision
  private SwerveDrivePoseEstimator swerveDrivePoseEstimator;

  private ShuffleData<Double[]> odometryLog = new ShuffleData<Double[]>("swerve", "odometry",
      new Double[] { 0.0, 0.0, 0.0, 0.0 });
  private ShuffleData<Double[]> desiredOdometryLog = new ShuffleData<Double[]>("swerve", "desiredOdometry",
      new Double[] { 0.0, 0.0, 0.0, 0.0 });
  private ShuffleData<Double[]> realStatesLog = new ShuffleData<Double[]>("swerve", "real states",
      new Double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 });
  private ShuffleData<Double[]> desiredStatesLog = new ShuffleData<Double[]>("swerve", "desired states",
      new Double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 });
  private ShuffleData<Double> yawLog = new ShuffleData<Double>("swerve", "yaw", 0.0);
  private ShuffleData<Double> pitchLog = new ShuffleData<Double>("swerve", "pitch", 0.0);
  private ShuffleData<Double> rollLog = new ShuffleData<Double>("swerve", "roll", 0.0);
  private ShuffleData<Double> headingLog = new ShuffleData<Double>("swerve", "heading", 0.0);
  private ShuffleData<Double> rotationalVelocityLog = new ShuffleData<Double>("swerve", "rotational velocity", 0.0);

  public Pose2d desiredPose = new Pose2d(0, 0, Rotation2d.fromDegrees(0));

  public Swerve() {
    if (!Robot.isReal()) {
      gyro = new GyroSim();
      for (int i = 0; i < 4; i++) {
        modules[i] = new SwerveModule(i, new SwerveModuleSim());
      }
    } else {
      // real swerve module instatiation here
      for (int i = 0; i < 4; i++) {
        gyro = new NavX2Gyro();
        modules[i] = new SwerveModule(i, new SwerveModuleRelative(i));
      }
    }

    swerveDrivePoseEstimator = new SwerveDrivePoseEstimator(Constants.DriveConstants.driveKinematics,
        new Rotation2d(0),
        new SwerveModulePosition[] { modules[0].getPosition(), modules[1].getPosition(),
            modules[2].getPosition(), modules[3].getPosition() },
        new Pose2d(new Translation2d(0, 0), new Rotation2d(0)));

    if (Robot.isSimulation()) {
      // resetOdometry(new Pose2d(new Translation2d(1, 1), new
      // Rotation2d(Units.degreesToRadians(270))));
    }
  }

  public void setChassisSpeeds(ChassisSpeeds chassisSpeeds) {
    // Convert chassis speeds to individual module states
    SwerveModuleState[] moduleStates = DriveConstants.driveKinematics.toSwerveModuleStates(chassisSpeeds);
    // take shortest path to destination
    SwerveDriveKinematics.desaturateWheelSpeeds(moduleStates, Constants.DriveConstants.maxSpeedMetersPerSecond);
    // 6. Output each module states to wheels

    SmartDashboard.putNumber("chassisSpeeds updates", moduleStates[1].speedMetersPerSecond);

    setModuleStates(moduleStates);
  }

  public ChassisSpeeds getChassisSpeeds() {
    SwerveModuleState[] states = new SwerveModuleState[4];
    for (int i = 0; i < 4; i++) {
      states[i] = modules[i].getState();
    }
    ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(DriveConstants.driveKinematics.toChassisSpeeds(states),
        getRotation2d());
    return speeds;
  }

  public void resetGyro() {
    gyro.resetGyro();
  }

  public Rotation2d getRotation2d() {

    Rotation2d rotation = swerveDrivePoseEstimator.getEstimatedPosition().getRotation();
    // return rotation;
    double heading = rotation.getDegrees();

    if (heading < 0) {
      heading += 360;
    }
    return new Rotation2d(heading / 180 * Math.PI);
  }

  public Pose2d getPose() {
    Pose2d estimatedPose = swerveDrivePoseEstimator.getEstimatedPosition();
    return new Pose2d(estimatedPose.getTranslation(), getRotation2d());
  }

  public SwerveDrivePoseEstimator getPoseEstimator() {
    return swerveDrivePoseEstimator;
  }

  public void resetOdometry(Pose2d pose) {
    // convert to -pi to pi
    Rotation2d gyroHeading = new Rotation2d(gyroData.yawDeg / 180 * Math.PI);
    swerveDrivePoseEstimator.resetPosition(gyroHeading,
        new SwerveModulePosition[] { modules[0].getPosition(), modules[1].getPosition(),
            modules[2].getPosition(), modules[3].getPosition() },
        pose);

    desiredOdometryLog
        .set(new Double[] { getPose().getX(), getPose().getY(), getPose().getRotation().getDegrees() });
  }

  public void updateOdometry() {
    // convert to -pi to pi
    Rotation2d gyroHeading = Rotation2d.fromRadians(MathUtil.angleModulus(Units.degreesToRadians(gyroData.yawDeg)));

    swerveDrivePoseEstimator.update(gyroHeading,
        new SwerveModulePosition[] { modules[0].getPosition(), modules[1].getPosition(),
            modules[2].getPosition(), modules[3].getPosition() });

  }

  public void logDesiredOdometry(Pose2d desiredPose) {
    this.desiredPose = desiredPose;
    desiredOdometryLog
        .set(new Double[] { desiredPose.getX(), desiredPose.getY(), desiredPose.getRotation().getDegrees() });

  }

  public void stopModules() {
    for (SwerveModule module : modules) {
      module.stop();
    }
  }

  public void setModuleStates(SwerveModuleState[] desiredStates) {
    SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, DriveConstants.maxSpeedMetersPerSecond);

    for (int i = 0; i < 4; i++) {
      modules[i].setDesiredState(desiredStates[i]);
    }

    updateOdometry();
  }

  public double getVerticalTilt() {
    return gyroData.pitchDeg;
  }

  @Override
  public void periodic() {

    updateOdometry();
    gyro.updateData(gyroData);

    for (int i = 0; i < 4; i++) {
      modules[i].periodic();
    }

    Double[] realStates = {
        modules[0].getState().angle.getDegrees(),
        modules[0].getState().speedMetersPerSecond,
        modules[1].getState().angle.getDegrees(),
        modules[1].getState().speedMetersPerSecond,
        modules[2].getState().angle.getDegrees(),
        modules[2].getState().speedMetersPerSecond,
        modules[3].getState().angle.getDegrees(),
        modules[3].getState().speedMetersPerSecond
    };

    Double[] desiredStates = {
        modules[0].getDesiredState().angle.getDegrees(),
        modules[0].getDesiredState().speedMetersPerSecond,
        modules[1].getDesiredState().angle.getDegrees(),
        modules[1].getDesiredState().speedMetersPerSecond,
        modules[2].getDesiredState().angle.getDegrees(),
        modules[2].getDesiredState().speedMetersPerSecond,
        modules[3].getDesiredState().angle.getDegrees(),
        modules[3].getDesiredState().speedMetersPerSecond
    };

    realStatesLog.set(realStates);
    desiredStatesLog.set(desiredStates);
    rotationalVelocityLog.set(Units.radiansToDegrees(getChassisSpeeds().omegaRadiansPerSecond));
    odometryLog.set(
        new Double[] { getPose().getX(), getPose().getY(), getPose().getRotation().getDegrees() });

    

    yawLog.set(gyroData.yawDeg);
    pitchLog.set(gyroData.pitchDeg);
    rollLog.set(gyroData.rollDeg);
    headingLog.set(getRotation2d().getDegrees());
  }
}