// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LEDConstants;
import frc.robot.Constants;
import frc.robot.Constants.LEDConstants.LEDState;
import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.led.CANdle.LEDStripType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;


public class subsystem_LED extends SubsystemBase {
  /** Creates a new subsystem_LED. */
  CANdle m_candle;
  LEDState m_LEDState = LEDState.Standby;
  private String state_machine;
  private boolean state = true;//should led be on or off

  public subsystem_LED() {
    m_candle = new CANdle(Constants.LEDConstants.CANdle_ID);
    m_candle.configLEDType(LEDStripType.GRB);
    state_machine = "";
    m_candle.configBrightnessScalar(0.5);
    // m_candle.configLOSBehavior(true); uncomment if wanted, but not sure if needed
  }

  public LEDState getLEDState(){
    return m_LEDState;
  }
  public void set_state_machine_method(String newState){
    state_machine = newState;
  }
  public Command set_stateCommand(String newState){
    return this.runOnce(() -> set_state_machine_method(newState));
  }
  public String get_state_machine_method(){
return state_machine;
  }
  public Command get_stateCommand(){
    return this.runOnce(() -> get_state_machine_method());
  }

  // public boolean getState(){
  //   return state;
  // }
  // public void enableMethod(boolean enabling){
  //     state = enabling;
  // }

  public void setGreenLED(){
    m_LEDState = LEDState.Green;
  }

public Command command_coop(){
  return this.runOnce(()-> setGreenLED());
}
public Command command_off(){
  return this.runOnce(()-> setStandbyLED());
}

  public void setStandbyLED(){
    m_LEDState = LEDState.Standby;
  }
  // public Command offLED(){
  //   state=false;
  //   return this.runOnce(() -> setStandbyLED());
  // }
  // public Command enable(){
  //   return this.runOnce(() -> enableMethod());
  // }

  @Override
  public void periodic() {
    SmartDashboard.putBoolean("led state", state);
    switch(m_LEDState){
        case Standby:
          m_candle.setLEDs(0, 0, 0);
          break;
        case Green:
          m_candle.setLEDs(0, 255, 0);
          break;
      }
    // This method will be called once per scheduler run
  }
}