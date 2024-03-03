package frc.robot.subsystems.shooter;

import com.pathplanner.lib.util.PIDConstants;

import frc.robot.Robot;

public final class ShooterConstants {

    public static final int shooterBottomId = 19;
    public static final int shooterTopId = 18;

    public static final double shooterVelocityRadPerSec = 100;

    private static final PIDConstants simShooterTopPID = new PIDConstants(
            0,

            0,
            0);
    private static final PIDConstants realShooterTopPID = new PIDConstants(
            0.004,
            0,
            0);
    public static final PIDConstants shooterTopPID = Robot.isReal()
            ? realShooterTopPID
            : simShooterTopPID;

    private static final PIDConstants simShooterBottomPID = new PIDConstants(
            0,
            0,
            0);
    private static final PIDConstants realShooterBottomPID = new PIDConstants(
            0.004,
            0,
            0);
    public static final PIDConstants shooterBottomPID = Robot.isReal()
            ? realShooterBottomPID
            : simShooterBottomPID;

    public static final double topkV = 0.0201; // volts/radPerSec

    public static final double bottomkV = 0.0201;
}


