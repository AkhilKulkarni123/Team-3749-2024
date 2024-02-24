package frc.robot.subsystems.arm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.utils.Constants;
import edu.wpi.first.wpilibj.Filesystem;

public class ShootKinematics {
    private static final Translation2d redSpeakerPosition = Constants.ArmConstants.redSpeakerPosition;
    private static final Translation2d blueSpeakerPosition = Constants.ArmConstants.blueSpeakerPosition;

    private static final Translation2d[] redStagePoints = Constants.ArmConstants.redStagePoints;
    private static final Translation2d[] blueStagePoints = Constants.ArmConstants.blueStagePoints;

    // 10.00 m = 1000
    // angle 0.0 = impossible to shoot from here
    private static final double[] distToAngle = new double[1001];
    private static double maxDist = 0.0;

    public static Pose2d shootingPose2DCalculate(Pose2d currentPose2d){
        Rotation2d angle;

        Translation2d distanceVector = currentPose2d.getTranslation().minus(getSpeakerPosition());

        angle = new Rotation2d(Math.atan2(Math.abs(distanceVector.getY()), Math.abs(distanceVector.getX())));
        // System.out.println("Angle: " + angle);
        // System.out.println("MAX ANGLE: " + Constants.ArmConstants.maxAngle);

        //double distAngle = getAngle(distanceVector.getNorm());
        
        // Case 0: We are in angle
        if (angle.getDegrees() < Constants.ArmConstants.maxAngle && distanceVector.getNorm() <= maxDist){ 
            // System.out.println("Case 0");
            return moveOutOfStage(changeRotation(currentPose2d.getTranslation(), distanceVector));
            // return changeRotation(currentPose2d.getTranslation(), distanceVector);
        } 
        // Case 1: We are out of angle
        else if (angle.getDegrees() >= Constants.ArmConstants.maxAngle) {
            // System.out.println("Case 1");

            // TODO: Check if positive/negative x coord check is correct
            Translation2d radiusVector;

            if ((distanceVector.getAngle().getRadians() < 0 && DriverStation.getAlliance().get() == Alliance.Red) || (distanceVector.getAngle().getRadians() > 0 && DriverStation.getAlliance().get() == Alliance.Blue)) {
                // System.out.println("case 1a");
                radiusVector = new Translation2d(Math.cos(Constants.ArmConstants.maxAngleRad), Math.sin(Constants.ArmConstants.maxAngleRad));
            } else {
                // System.out.println("case 1b");
                radiusVector = new Translation2d(Math.cos(-Constants.ArmConstants.maxAngleRad), Math.sin(-Constants.ArmConstants.maxAngleRad));
            }
 
            Translation2d perpVector = projection(distanceVector, radiusVector).minus(distanceVector);
            Translation2d goal = perpVector.plus(currentPose2d.getTranslation());

            // Case 3: We are out of range and out of angle
            Translation2d newDistanceVector = goal.minus(getSpeakerPosition());
            if (newDistanceVector.getNorm() > maxDist) {
                // System.out.println("Special Case 3");
                goal = getSpeakerPosition().plus(newDistanceVector.div(newDistanceVector.getNorm()).times(maxDist));
            }

            return moveOutOfStage(changeRotation(goal, goal.minus(getSpeakerPosition())));
            // return changeRotation(goal, goal.minus(getSpeakerPosition()));
        }
        // Case 2: We are out of range
        else if (distanceVector.getNorm() > maxDist) {
            // System.out.println("Case 2");
            Translation2d goal = getSpeakerPosition().plus(distanceVector.div(distanceVector.getNorm()).times(maxDist));
            return moveOutOfStage(changeRotation(goal, goal.minus(getSpeakerPosition())));
        }

        return null;
    }

    private static Pose2d changeRotation(Translation2d currentTranslation2d, Translation2d distanceVector){
        // System.out.println(distanceVector.getAngle().getDegrees());
        return new Pose2d(currentTranslation2d, new Rotation2d(Math.PI + distanceVector.getAngle().getRadians()));
        // Ok basically the Rotation2d angle is pi + angle only if the currenttranslation is above the speaker
        // Otherwise the angle is pi - angle if the currenttranslation is below the speaker
    }

    // Case 5 Check if we are in stage and move accordingly
    private static Pose2d moveOutOfStage(Pose2d poseInRadius){
        // System.out.println("Case 5");
        Translation2d[] stagePoints = getStagePoints();

        Translation2d distanceVector = poseInRadius.getTranslation().minus(stagePoints[0]);
        double angle = Math.abs(distanceVector.getAngle().getRadians());

        if (angle < Math.PI/6 && poseInRadius.getTranslation().getX() < stagePoints[1].getX()){
            Translation2d perpVector = projection(distanceVector, stagePoints[2].minus(stagePoints[0])).minus(distanceVector);
            Translation2d nearestShootPoint = poseInRadius.getTranslation().plus(perpVector);
            
            return changeRotation(nearestShootPoint, nearestShootPoint.minus(getSpeakerPosition()));
        }

        return poseInRadius;
    }

    private static Translation2d projection(Translation2d vector, Translation2d target) {
        // return target.div(Math.pow(target.getNorm(),2)).times(dotProduct(target, vector));
        return target.times(dotProduct(target, vector) / (Math.pow(target.getNorm(), 2)));
    }

    private static double dotProduct(Translation2d v1, Translation2d v2) {
        return v1.getX() * v2.getX() + v1.getY() * v2.getY();
    }

    private static double getAngle(double dist) {
        int distNum = (int)(Math.round(dist * 100.0));
        if (distNum < 0 || distNum > 1000) {
            return -1;
        }
        return distToAngle[distNum];
    }

    private static Translation2d getSpeakerPosition() {
        try {
            return (DriverStation.getAlliance().get() == Alliance.Red) ? redSpeakerPosition : blueSpeakerPosition;
        } catch (Exception e) {
            return blueSpeakerPosition;
        }
    }

    private static Translation2d[] getStagePoints(){
        try {
            return (DriverStation.getAlliance().get() == Alliance.Red) ? redStagePoints : blueStagePoints;
        } catch (Exception e) {
            return blueStagePoints;
        }
    }

    public static void loadDistCSV() throws FileNotFoundException, IOException {
        Path csvPath = Filesystem.getDeployDirectory().toPath().resolve("angles.csv");
        loadDistCSV(csvPath.toFile());
    }

    public static void loadDistCSV(File file) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            double curDist = Double.parseDouble(values[0]);
            maxDist = Math.max(maxDist, curDist);
            distToAngle[(int)(curDist * 100)] = Double.parseDouble(values[1]);
        }

        maxDist -= Constants.ArmConstants.distMargin;

        reader.close();
    }

    public static double getArmAngleGivenPose(Pose2d currentPose2d) throws FileNotFoundException, IOException{
        double distance = currentPose2d.getTranslation().minus(blueSpeakerPosition).getNorm();
        return getAngle(distance);
    }

    // for testing load csv & other functionality
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // loadDistCSV(new File("src/main/deploy/angles.csv"));
        // for (double i = 0.9; i < maxDist; i += 0.01) {
        //     i = Math.round(i*100)/100.0;
        //     double temp = i + Math.random()*.01;
        //     temp = Math.round(temp*1000)/1000.0;
        //     System.out.println(temp + " " + getAngle(temp));
        // }
        
        // returns -1 if out of range, returns 0 if too close
        loadDistCSV(new File("src/main/deploy/angles.csv"));
        Pose2d currentPose2d = new Pose2d(0, 5, new Rotation2d(0));
        System.out.println(getArmAngleGivenPose(currentPose2d));
    }
}