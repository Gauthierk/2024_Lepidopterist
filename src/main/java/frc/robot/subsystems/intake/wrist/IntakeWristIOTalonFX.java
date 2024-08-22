package frc.robot.subsystems.intake.wrist;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
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

  public IntakeWristIOTalonFX() {
    pivotMotor =
        new TalonFX(IntakeWristConstants.wristMotorID, IntakeWristConstants.wristMotorCANBus);

    var config = new TalonFXConfiguration();

    config.CurrentLimits.SupplyCurrentLimit = 30.0;
    config.CurrentLimits.SupplyCurrentLimitEnable = true;
    config.MotorOutput.NeutralMode = NeutralModeValue.Coast;

    pivotMotor.getConfigurator().apply(config);

    setBrake(true);
    pidController =
        new ProfiledPIDController(
            IntakeWristConstants.intakeWristPIDReal[0],
            IntakeWristConstants.intakeWristPIDReal[1],
            IntakeWristConstants.intakeWristPIDReal[2],
            new TrapezoidProfile.Constraints(2.45, 2.45));

    pidController.setTolerance(
        IntakeWristConstants.intakeWristPositionTolerance,
        IntakeWristConstants.intakeWristVelocityTolerance);
    motorEncoder = pivotMotor.getPosition().getValueAsDouble();
    configurePID();
  }

  private void configurePID() {
    pidController.setP(IntakeWristConstants.intakeWristPIDReal[0]);
    pidController.setI(IntakeWristConstants.intakeWristPIDReal[1]);
    pidController.setD(IntakeWristConstants.intakeWristPIDReal[2]);
    pidController.enableContinuousInput(
        IntakeWristConstants.intakeWristMaxAngle, IntakeWristConstants.intakeWristMaxAngle);
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
    return Math.abs(getAngle() - setpoint) < IntakeWristConstants.intakeWristPositionTolerance;
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
