// Copyright (c) FIRST and other WPILib contributors.

// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

// import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;

import edu.wpi.first.math.geometry.Pose2d;
// import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
// import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.photonvision.PhotonCamera;
// import org.photonvision.targeting.PhotonTrackedTarget;

import org.photonvision.PhotonUtils;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
// import frc.robot.Constants;
import frc.robot.Constants.VisionConstants;
// import edu.wpi.first.apriltag.AprilTagFields;
/*
// import edu.wpi.first.math.geometry.Rotation3d;
//import com.revrobotics.CANSparkMax;
// import edu.wpi.first.wpilibj.smartdashboard.Field2d;
// import edu.wpi.first.apriltag.AprilTagFieldLayout;
// import edu.wpi.first.math.Matrix;
// import edu.wpi.first.math.VecBuilder;
// import edu.wpi.first.math.geometry.Rotation2d;
// import edu.wpi.first.math.geometry.Translation2d;
// import edu.wpi.first.math.numbers.N1;
// import edu.wpi.first.math.numbers.N3;
// import edu.wpi.first.wpilibj.smartdashboard.Field2d;
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// import edu.wpi.first.math.controller.PIDController;
// import edu.wpi.first.math.util.Units;
// import edu.wpi.first.units.Distance;
// import edu.wpi.first.wpilibj.XboxController;
// import edu.wpi.first.wpilibj.TimedRobot;
// import edu.wpi.first.wpilibj.XboxController;
// import edu.wpi.first.wpilibj2.command.Command;
// import edu.wpi.first.wpilibj2.command.CommandScheduler;
// import java.util.Optional;
// import org.photonvision.EstimatedRobotPose;
// import org.photonvision.PhotonPoseEstimator;
// import org.photonvision.PhotonPoseEstimator.PoseStrategy;
// import org.photonvision.simulation.PhotonCameraSim;
// import org.photonvision.simulation.SimCameraProperties;
// import org.photonvision.simulation.VisionSystemSim;
// import frc.robot.Robot;
// import frc.robot.Constants.SwerveConstants;
// import frc.robot.Constants.SwerveConstants;
 */
public class subsystem_Vision extends SubsystemBase {
  /** Creates a new subsystem_Vision. */

  private PhotonCamera m_Camera;
  private boolean m_Detected;
  private PhotonPipelineResult m_Result;
  private double m_Distance;
  private double m_Yaw;
  private double m_Timestamp;
  // private Pose2d m_RobotPose;
  private Transform3d m_RobotToCam;

  public subsystem_Vision() {
    //m_Camera = new PhotonCamera(VisionConstants.kCameraName);
    m_Camera = new PhotonCamera(VisionConstants.kCameraName);
    m_Camera.setPipelineIndex(1);
    m_RobotToCam = VisionConstants.kRobotToCam;
 
    m_Detected = false;
    m_Result = new PhotonPipelineResult();
    m_Distance = 0.0;
    m_Yaw = 0.0;
    m_Timestamp = 0.0;
    // m_RobotPose = new Pose2d();
  }
  public Pose2d pullPose2d(Rotation2d gyroAngle) {
    if(m_Detected){
      // double camHeight = VisionConstants.cam_height;
      int fiducialID = m_Result.getBestTarget().getFiducialId();
      SmartDashboard.putNumber("Target ID", fiducialID);
      double targetHeight = VisionConstants.kTagLayout.getTagPose(fiducialID).get().getZ();
      // double camPitch = VisionConstants.cam_pitch;  
      double targetPitch = m_Result.getBestTarget().getPitch() * Math.PI / 180.0;
      Rotation2d targetYaw = Rotation2d.fromDegrees(-m_Result.getBestTarget().getYaw());
      Pose2d targetPose = VisionConstants.kTagLayout.getTagPose(fiducialID).get().toPose2d();
      Transform3d inverted = m_RobotToCam.inverse();
      Transform2d cameraToRobot = new Transform2d(inverted.getX(), inverted.getY(), inverted.getRotation().toRotation2d());
      return PhotonUtils.estimateFieldToRobot(VisionConstants.camHeight, targetHeight, 
                                              VisionConstants.camPitch, targetPitch, 
                                              targetYaw, gyroAngle, 
                                              targetPose, cameraToRobot);


    }
    return new Pose2d();
  }
  /*
  // public double[] getEstimatedGlobalPose() {
  //   PhotonPipelineResult screenshot = m_Camera.getLatestResult();
  //   if (screenshot.hasTargets()) {
  //     PhotonTrackedTarget m_Target = screenshot.getBestTarget();
  //     if (m_Target.getPoseAmbiguity() < 0.2) {
  //       int targetID = m_Target.getFiducialId();
  //       Pose3d targetPose = VisionConstants.kTagLayout.getTagPose(targetID).get();
  //       Transform3d camToTarget = m_Target.getBestCameraToTarget();
  //       Pose3d camPose = targetPose.transformBy(camToTarget.inverse());
  //       Pose3d visionMeasurement = camPose.transformBy(m_RobotToCam.inverse());
  //       Pose2d visionMeasurement2D = visionMeasurement.toPose2d();
  //       return new double[]{visionMeasurement2D.getX(), 
  //                           visionMeasurement2D.getY(),
  //                           visionMeasurement2D.getRotation().getDegrees(),
  //                           screenshot.getTimestampSeconds()};
  //     }
  //   }
  //   return new double[]{0.0, 0.0};
  // }
   */

  public boolean isDetected(){
    return m_Detected;
  }

  public double getVisionDistance(){
    return m_Distance;
  }

  public double getVisionYaw(){
    return m_Yaw;
  }

  public PhotonPipelineResult getVisionResult(){
    return m_Result;
  }

  public double getTimestampMS(){
    return m_Timestamp;
  }

  public void smartdashboard(Field2d field) {
    SmartDashboard.putData("field", field);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    m_Result = m_Camera.getLatestResult();
    m_Detected = m_Result.hasTargets();
    if (m_Detected) {
      m_Timestamp = m_Result.getTimestampSeconds();
      m_Yaw = m_Result.getBestTarget().getYaw();

      // PhotonPipelineResult result = m_Camera.getLatestResult();
      // Transform3d cameraToTarget = result.getBestTarget().getBestCameraToTarget();
      // getEstimatedGlobalPose();
      // m_Distance = PhotonUtils.calculateDistanceToTargetMeters(VisionConstants.camHeight,
      //                                                         VisionConstants.targetHeight,
      //                                                         VisionConstants.camPitch,
      //                                                         m_Result.getBestTarget().getPitch());
    }
  }
}