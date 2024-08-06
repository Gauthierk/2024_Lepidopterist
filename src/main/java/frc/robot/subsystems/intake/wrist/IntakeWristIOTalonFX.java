package frc.robot.subsystems.intake.wrist;

import static frc.robot.subsystems.intake.IntakeConstants.*;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import org.littletonrobotics.junction.Logger;

public class IntakeWristIOTalonFX implements IntakeWristIO {
  // Motor and Encoders
  private TalonFX pivotMotor;
  private final ProfiledPIDController pidController;
  private ArmFeedforward feedforward = new ArmFeedforward(0, 0, 0, 0);
  private double setpoint = 0;
  private double motorEncoder;
  private double m_DesiredWristPos;
  private int m_WristSlot;

  public IntakeWristIOTalonFX() {
    pivotMotor = new TalonFX(intakeWristMotorID, intakeWristMotorCANbus);
    setBrake(true);
    pidController =
        new ProfiledPIDController(
            IntakeWristConstants.PIVOT_ARM_PID_REAL[0],
            IntakeWristConstants.PIVOT_ARM_PID_REAL[1],
            IntakeWristConstants.PIVOT_ARM_PID_REAL[2],
            new TrapezoidProfile.Constraints(2.45, 2.45));
    pidController.setTolerance(
        IntakeWristConstants.PIVOT_ARM_PID_TOLERANCE,
        IntakeWristConstants.PIVOT_ARM_PID_VELOCITY_TOLERANCE);
    pivotMotor.setPosition(0.0);
    motorEncoder = pivotMotor.getPosition().getValueAsDouble();
    configurePID();
    m_WristSlot = 0;
  }

  private void configurePID() {
    pidController.setP(IntakeWristConstants.PIVOT_ARM_PID_REAL[0]);
    pidController.setI(IntakeWristConstants.PIVOT_ARM_PID_REAL[1]);
    pidController.setD(IntakeWristConstants.PIVOT_ARM_PID_REAL[2]);
    pidController.enableContinuousInput(
        IntakeWristConstants.PIVOT_ARM_MIN_ANGLE, IntakeWristConstants.PIVOT_ARM_MAX_ANGLE);
  }

  /** Updates the set of loggable inputs. */
  @Override
  public void updateInputs(IntakeWristIOInputs inputs) {
    inputs.angleRads = getAngle();
    inputs.angVelocityRadsPerSec = pivotMotor.getVelocity().getValueAsDouble();
    inputs.appliedVolts =
        pivotMotor.getDutyCycle().getValueAsDouble()
            * pivotMotor.getSupplyVoltage().getValueAsDouble();
    inputs.currentAmps = new double[] {pivotMotor.getSupplyCurrent().getValueAsDouble()};
    inputs.tempCelsius = new double[] {pivotMotor.getDeviceTemp().getValueAsDouble()};
    inputs.setpointAngleRads = setpoint;
    Logger.recordOutput("IntakeWrist/MotorEncoder", motorEncoder);
  }

  /** Run open loop at the specified voltage. */
  @Override
  public void setVoltage(double motorVolts) {
    Logger.recordOutput("IntakeWrist/AppliedVolts", motorVolts);
    pivotMotor.setVoltage(motorVolts);
  }

  /** Returns the current distance measurement. */
  @Override
  public double getAngle() {
    return (Units.rotationsToRadians(motorEncoder));
  }

  /** Go to Setpoint */
  @Override
  public void goToSetpoint(double setpoint) {
    pidController.setGoal(setpoint);
    // With the setpoint value we run PID control like normal
    double pidOutput = MathUtil.clamp(pidController.calculate(getAngle()), -3, 3);
    double feedforwardOutput =
        feedforward.calculate(getAngle(), pidController.getSetpoint().velocity);

    Logger.recordOutput("IntakeWrist/FeedforwardOutput", feedforwardOutput);
    Logger.recordOutput("IntakeWrist/PIDOutput", pidOutput);

    Logger.recordOutput("IntakeWrist/VelocityError", pidController.getVelocityError());

    setVoltage(MathUtil.clamp(pidOutput + feedforwardOutput, -4, 4));
  }

  @Override
  public void holdSetpoint(double setpoint) {
    goToSetpoint(setpoint);
  }

  @Override
  public void setBrake(boolean brake) {
    if (brake) {
      pivotMotor.setNeutralMode(NeutralModeValue.Coast);
    }
  }

  @Override
  public boolean atSetpoint() {
    return Math.abs(getAngle() - setpoint) < IntakeWristConstants.PIVOT_ARM_PID_TOLERANCE;
  }

  @Override
  public void setP(double p) {
    pidController.setP(p);
  }

  @Override
  public void setI(double i) {
    pidController.setI(i);
  }

  @Override
  public void setD(double d) {
    pidController.setD(d);
  }

  @Override
  public void setFF(double ff) {
    // pidController.setFF(ff);
  }

  @Override
  public void setkS(double kS) {
    feedforward = new ArmFeedforward(kS, feedforward.kg, feedforward.kv, feedforward.ka);
  }

  @Override
  public void setkG(double kG) {
    feedforward = new ArmFeedforward(feedforward.ks, kG, feedforward.kv, feedforward.ka);
  }

  @Override
  public void setkV(double kV) {
    feedforward = new ArmFeedforward(feedforward.ks, feedforward.kg, kV, feedforward.ka);
  }

  @Override
  public void setkA(double kA) {
    feedforward = new ArmFeedforward(feedforward.ks, feedforward.kg, feedforward.kv, kA);
  }

  @Override
  public double getkS() {
    return feedforward.ks;
  }

  @Override
  public double getkG() {
    return feedforward.kg;
  }

  @Override
  public double getkV() {
    return feedforward.kv;
  }

  @Override
  public double getkA() {
    return feedforward.ka;
  }

  @Override
  public double getP() {
    return pidController.getP();
  }

  @Override
  public double getI() {
    return pidController.getI();
  }

  @Override
  public double getD() {
    return pidController.getD();
  }

  @Override
  public double getFF() {
    // return pidController.getFF();
    return 0;
  }
}