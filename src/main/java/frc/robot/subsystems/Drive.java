package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

public class Drive {

    public WPI_TalonSRX leftTalon;
    private WPI_VictorSPX[] leftVictors = new WPI_VictorSPX[3];

    public WPI_TalonSRX rightTalon;
    private WPI_VictorSPX[] rightVictors = new WPI_VictorSPX[3];

    public Drive() {
        leftTalon = new WPI_TalonSRX(1);

        leftVictors[0] = new WPI_VictorSPX(2);
        leftVictors[1] = new WPI_VictorSPX(3);

        leftVictors[0].follow(leftTalon);
        leftVictors[1].follow(leftTalon);

        rightTalon = new WPI_TalonSRX(4);

        rightVictors[0] = new WPI_VictorSPX(5);
        rightVictors[1] = new WPI_VictorSPX(6);

        rightVictors[0].follow(rightTalon);
        rightVictors[1].follow(rightTalon);
    }

    public void setMotorPercent(double percent) {
        setMotorPercents(percent, percent);
    }

    public void setMotorPercents(double left, double right) {
        leftTalon.set(left);
        rightTalon.set(right);
    }

}