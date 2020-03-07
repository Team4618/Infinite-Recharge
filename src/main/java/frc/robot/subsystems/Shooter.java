package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;

public class Shooter {
    public WPI_TalonSRX top;
    public WPI_TalonSRX bottom;
    public WPI_TalonSRX pivot;
    public AnalogInput potentiometer;
    Vision vision;

    private boolean goingToMax = false;
    public boolean locked = false;

    private double[] errors = { 0, 0 };
    private double[] previousErrors = { 0, 0 };
    private WPI_TalonSRX[] motors = { top, bottom };

    public Shooter() {
        top = new WPI_TalonSRX(11);
        top.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        top.setSensorPhase(true);

        bottom = new WPI_TalonSRX(10);
        bottom.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        bottom.setSensorPhase(false);
        // bottom.setInverted(false);
        pivot = new WPI_TalonSRX(12);
        potentiometer = new AnalogInput(1);
        vision = new Vision();
    }

    public void shoot() {
        // MAGIC EMPIRICAL DATA TO SEE HOW FAST WE NEED TO SHOOT

        // 1 and 1 are completley arbitary and probably realllly small
        double topSpeed = topPID(1);
        double bottomSpeed = bottomPID(1);

        top.set(topSpeed);
        bottom.set(bottomSpeed);

    }

    public void setSpeed(double speed) {
        top.set(speed);
        bottom.set(speed);
    }

    public void printSpeeds() {
        System.out.println("Bottom speed: " + top.get());
        System.out.println("Top speed: " + bottom.get());
    }

    public double topPID(double target) {
        return PIDShooter(target, 0);
    }

    public double bottomPID(double target) {
        return PIDShooter(target, 1);
    }

    private double PIDShooter(double target, int motor) {
        double currentSpeed = motors[motor].getSensorCollection().getQuadratureVelocity() * 1d / 409.6d; // 10(ms to s)
                                                                                                         // / 4096
                                                                                                         // ticks/rotation

        double error = target - currentSpeed;

        errors[motor] += error * 0.2d;

        double p = 1;

        double i = 0;

        double d = 0;

        double pid = p * error + i * errors[motor] + d * (error - previousErrors[motor]) / 0.2;

        previousErrors[motor] = error;

        return pid;
    }

    public void pivotPID(double target, boolean lock) { // target should be a value between 0.1 and 1.3
        if (target < 0.1 || target > 1.3)
            return;

        double current = potentiometer.getVoltage();

        double error = current - target;

        double p = 1.38;

        double out = p * error / 1.2d; // 1.2 so we can factor it between -1 and 1

        pivot.set(out);

        locked = lock && (error <= 0.1); // error is kinda arbitrary

    }

    public void pidPivotToTarget() {
        double percent = vision.getAngle();

        double current = potentiometer.getVoltage();

        if (percent == 500) {// scan for thingy
            if (goingToMax) {
                if (atMax()) {
                    goingToMax = false;
                    pivotPID(current - 0.2d, false);
                } else {

                    pivotPID(current + 0.2d >= 1.3 ? 1.3 : current + 0.2d, false);
                }
            } else {
                if (atMin()) {
                    goingToMax = true;
                    pivotPID(current + 0.2d, false);
                } else {
                    pivotPID(current - 0.2d <= 0.1 ? 0.1 : current - 0.2d, false);
                }
            }
        }

        pivotPID(current + (percent * 0.32d), true);
    }

    public boolean atMin() {
        return potentiometer.getAverageVoltage() <= 0.11; // we say its zeroed when it reaches about 0.1
    }

    public boolean atMax() {
        return potentiometer.getAverageVoltage() >= 1.29; // about 1.3
    }
}