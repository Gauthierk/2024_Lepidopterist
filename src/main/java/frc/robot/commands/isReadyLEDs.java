// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.subsystem_ShooterWheels;
import frc.robot.subsystems.subsystem_ShooterWrist;
import frc.robot.subsystems.subsystem_LED;

public class isReadyLEDs extends Command {
  /** Creates a new isReadyLEDs. */
  private final subsystem_ShooterWheels m_wheels;
  private final subsystem_ShooterWrist m_wrist;
  private final subsystem_LED m_LED;

  public isReadyLEDs(subsystem_ShooterWheels wheels, subsystem_ShooterWrist wrist, subsystem_LED led) {
    // Use addRequirements() here to declare subsystem dependencies.
    m_wheels = wheels;
    m_wrist = wrist;
    m_LED = led;
    addRequirements(m_LED);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {

  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    if (m_wheels.is_flywheel_at_desired_speed() && m_wrist.isWristAtDesiredPosition()) {
      m_LED.setGreenLED();
      return true;
    } else {
      m_LED.setStandbyLED();
      return false;
    }

  }
}
