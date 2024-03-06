package frc.robot.subsystems.intake;

import com.pathplanner.lib.util.PIDConstants;

import frc.robot.Robot;

public final class IntakeConstants {

    public static final int intakeId = 20;

    public static final double gearRatio = 5;
    private static final PIDConstants simIntakePID = new PIDConstants(0, 0, 0);
    private static final PIDConstants realIntakePID = new PIDConstants(
            0.01,
            0,
            0);
    public static final PIDConstants intakePID = Robot.isReal()
            ? realIntakePID
            : simIntakePID;
    public static final double kV = 0.102;

    public static final double intakeVelocityRadPerSec = 125;
    public static final double outtakeVelocityRadPerSec = -30;

    public enum IntakeStates {
        STOP,
        INTAKE,
        OUTTAKE,
        INDEX,
        FEED
    }
}