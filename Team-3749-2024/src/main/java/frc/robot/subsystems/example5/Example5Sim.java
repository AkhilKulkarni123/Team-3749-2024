package frc.robot.subsystems.example5;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import frc.robot.utils.Constants.ElectricalConstants;
import frc.robot.utils.Constants.Sim;

public class Example5Sim implements Example5IO {

    private FlywheelSim flywheel = new FlywheelSim(DCMotor.getNEO(1),1, 0.01);
    private double appliedVolts = 0.0;
    private double maxVolts = ElectricalConstants.example5VoltageLimit;
    private double maxOutput = 0;

    public Example5Sim() {
        System.out.println("[Init] Creating ExampleIOSim");
    }
    
    @Override
    public void updateData(Example5Data data){

        // update sim values every 0.02 sec
        flywheel.update(Sim.loopPeriodSec);

        // distance traveled + Rad/Time * Time * diameter
        data.positionRad = data.positionRad
                + (flywheel.getAngularVelocityRadPerSec() * 0.02 );

        data.velocityRadPerSec = flywheel.getAngularVelocityRadPerSec() ;

        data.appliedVolts = appliedVolts;

        data.currentAmps = Math.abs(flywheel.getCurrentDrawAmps());

        data.tempCelcius = 0;
    }
    @Override
    public void setVoltage(double volts) {
        appliedVolts = MathUtil.clamp(volts, -maxVolts * maxOutput, maxVolts*maxOutput);
        flywheel.setInputVoltage(appliedVolts);
    }

    @Override
    public void setMaxOutput(double maxOutput){
        this.maxOutput = maxOutput;
    }
}