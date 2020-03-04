package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;

public class Shooter {
    private WPI_TalonSRX top;
    private WPI_TalonSRX bottom;
    private WPI_TalonSRX pivot;
    private AnalogInput potentiometer;

    public Shooter() {
        top = new WPI_TalonSRX(10);
        bottom = new WPI_TalonSRX(11);
        pivot = new WPI_TalonSRX(12);
        potentiometer = new AnalogInput(1);
    }

    public void shoot() {
        // MAGIC VISON TRACKING
        // MAGIC MOVING OF THE PIVOT

        // MAGIC PID OF SHOOTER
        double topSpeed = 1;
        double bottomSpeed = 1;

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
}