package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;

public class Shooter {
    public WPI_TalonSRX top;
    public WPI_TalonSRX bottom;
    public WPI_TalonSRX pivot;

    public Shooter() {
        top = new WPI_TalonSRX(10);
        top.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        top.setSensorPhase(true);

        bottom = new WPI_TalonSRX(11);
        bottom.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        bottom.setSensorPhase(false);
        // bottom.setInverted(false);
        // pivot = new WPI_TalonSRX(12);
        // potentiometer = new AnalogInput(1);
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