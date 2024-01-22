package frc.robot.utils;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.subsystems.example.ExampleIO.ExampleData;

public interface CurrentBudgettedSubsystem  {

    /** Updates the set of loggable inputs. */
    public default void reduceCurrentSum(IntSupplier currentReductionSupplier) {

    }
    
}
