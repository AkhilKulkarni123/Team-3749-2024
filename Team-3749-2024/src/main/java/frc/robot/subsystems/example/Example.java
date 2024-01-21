package frc.robot.subsystems.example;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.subsystems.example.ExampleIO.ExampleData;

public class Example extends SubsystemBase {

    private ExampleData data = new ExampleData();
    private ExampleIO exampleIO;
    private double currentSum = 0;
    // private Example

    // Constructor
    public Example(){
        if (Robot.isReal()){
            exampleIO = new ExampleSim();
        }
    }

    public double getCurrentSum(){
        return currentSum;
    }

    // runs every 0.02 sec
    @Override
    public void periodic(){
        


    }
    
}
