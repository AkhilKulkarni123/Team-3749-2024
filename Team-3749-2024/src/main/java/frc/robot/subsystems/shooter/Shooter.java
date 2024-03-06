package frc.robot.subsystems.shooter;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.subsystems.shooter.ShooterConstants.ShooterStates;
import frc.robot.subsystems.shooter.ShooterIO.ShooterData;
import frc.robot.subsystems.wrist.WristConstants.WristStates;
import frc.robot.utils.ShuffleData;

public class Shooter extends SubsystemBase {

  private ShooterIO shooterIO;
  private ShooterData data = new ShooterData();
  private ShooterStates state = ShooterStates.STOP;
  private boolean intakeSpedUp = false;

  private PIDController bottomFeedback = new PIDController(
      ShooterConstants.shooterBottomPID.kP,
      ShooterConstants.shooterBottomPID.kI,
      ShooterConstants.shooterBottomPID.kD);

  private PIDController topFeedback = new PIDController(
      ShooterConstants.shooterTopPID.kP,
      ShooterConstants.shooterTopPID.kI,
      ShooterConstants.shooterTopPID.kD);

  private SimpleMotorFeedforward topShooterFF = new SimpleMotorFeedforward(
      0,
      ShooterConstants.topkV,
      0);

  private SimpleMotorFeedforward bottomShooterFF = new SimpleMotorFeedforward(
      0,
      ShooterConstants.bottomkV,
      0);

  private ShuffleData<Double> topShooterVelocityLog = new ShuffleData<Double>(this.getName(), "top shooter velocity",
      0.0);
  private ShuffleData<Double> bottomShooterVelocityLog = new ShuffleData<Double>(this.getName(),
      "bottom shooter velocity", 0.0);
  private ShuffleData<Double> topShootervoltageLog = new ShuffleData<Double>(this.getName(), "top shooter voltage",
      0.0);
  private ShuffleData<Double> bottomShootervoltageLog = new ShuffleData<Double>(this.getName(),
      "bottom shooter voltage", 0.0);
  private ShuffleData<Double> topShootercurrentLog = new ShuffleData<Double>(this.getName(), "top shooter current",
      0.0);
  private ShuffleData<Double> bottomShootercurrentLog = new ShuffleData<Double>(this.getName(),
      "bottom shooter current", 0.0);
  private ShuffleData<String> stateLog = new ShuffleData<String>(this.getName(), "state",
      ShooterStates.STOP.name());

  public Shooter() {
    shooterIO = new ShooterSparkMax();
    if (Robot.isSimulation()) {
      shooterIO = new ShooterSim();
    }
  }

  public double getVelocityRadPerSec() {
    return (data.topShooterVelocityRadPerSec + data.bottomShooterVelocityRadPerSec) / 2;
  }

  public ShooterStates getState() {
    return state;
  }

  public void setShooterVelocity(double velocityRadPerSec) {

    double topVoltage = topFeedback.calculate(
        data.topShooterVelocityRadPerSec,
        velocityRadPerSec) +
        topShooterFF.calculate(velocityRadPerSec);

    double bottomVoltage = bottomFeedback.calculate(
        data.bottomShooterVelocityRadPerSec,
        velocityRadPerSec) +
        bottomShooterFF.calculate(velocityRadPerSec);

    // double topVoltage = kVData.get() *velocityRadPerSec;
    // double bottomVoltage = kVData.get()
    setVoltage(topVoltage, bottomVoltage);
  }

  public void setVoltage(double topVolts, double bottomVolts) {
    shooterIO.setVoltage(topVolts, bottomVolts);
  }

  public void stop() {
    intakeSpedUp = false;

    shooterIO.setVoltage(0, 0);

  }

  public void runShooterState() {
    switch (state) {
      case STOP:
        stop();
        break;
      case INTAKE:
        intake();
        break;
      case INDEX:
        index();
        break;
      case SPOOL:
        setShooterVelocity(ShooterConstants.shooterVelocityRadPerSec);
        break;
    }
  }

  public void setState(ShooterStates state) {
    this.state = state;
  }

  private void intake() {
    // this is just for the setpoint checker below
    setVoltage(-0.2, -0.2);
    if (getVelocityRadPerSec() > 2) {
      intakeSpedUp = true;
    }
    if (getVelocityRadPerSec() < 0.05 && intakeSpedUp) {
      Robot.intake.setHasPiece(true);
      state = ShooterStates.INDEX;
      // state
    }

  }

  private void index() {
    setVoltage(-1.2, -1.2);
    if (Math.abs(getVelocityRadPerSec()) > 20) {
      state = ShooterStates.STOP;
      Robot.intake.setIndexedPiece(true);
    }

  }

  @Override
  public void periodic() {
    shooterIO.updateData(data);
    runShooterState();

    topShooterVelocityLog.set(data.topShooterVelocityRadPerSec);
    bottomShooterVelocityLog.set(data.bottomShooterVelocityRadPerSec);

    topShootervoltageLog.set(data.topShooterVolts);
    bottomShootervoltageLog.set(data.bottomShooterVolts);

    topShootercurrentLog.set(data.topShooterCurrentAmps);
    bottomShootercurrentLog.set(data.bottomShooterCurrentAmps);

    stateLog.set(state.name());
  }

}
