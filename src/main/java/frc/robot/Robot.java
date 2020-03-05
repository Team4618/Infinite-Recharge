/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.subsystems.*;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Servo;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.revrobotics.ColorSensorV3;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  private XboxController controller;
  private Joystick joystick;

  // shooter
  private Shooter shooter;
  private double shooterSpeed;

  // disk spinner
  final Color colourBlue = new Color(0, 1, 1);
  final Color colourGreen = new Color(0, 1, 0);
  final Color colourYellow = new Color(1, 1, 0);
  final Color colourRed = new Color(1, 0, 0);
  private boolean spinning;
  private int colourCount = 0;
  private Color lastColour = null;
  private Color colourToFind;
  private ColorSensorV3 colourSensor = new ColorSensorV3(I2C.Port.kOnboard);
  private WPI_VictorSPX diskSpinner = new WPI_VictorSPX(30);

  // drive
  private DifferentialDrive robotDrive;
  private Drive drive;

  // intake
  private WPI_VictorSPX[] intakeMotors = { new WPI_VictorSPX(41), new WPI_VictorSPX(42) };
  private WPI_VictorSPX intakeWrist = new WPI_VictorSPX(40);
  // private AnalogInput wristPotentiometer = new AnalogInput(0);
  private boolean runBallTrack = false;
  private boolean intaking = false;
  private boolean lastDIO0 = true;
  private boolean lastDIO1 = true;
  private long ballTrackTimer = -1;
  private DigitalInput dio0 = new DigitalInput(0);
  private DigitalInput dio1 = new DigitalInput(1);

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    controller = new XboxController(0);
    joystick = new Joystick(1);
    drive = new Drive();

    robotDrive = new DifferentialDrive(drive.leftTalon, drive.rightTalon);

    shooter = new Shooter();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */

  DigitalInput[] dis = { new DigitalInput(2), new DigitalInput(3) };
  AnalogInput[] ans = { new AnalogInput(0), new AnalogInput(1) };

  @Override
  public void robotPeriodic() {

    SmartDashboard.putBoolean("DIO 0", dio0.get());
    SmartDashboard.putBoolean("DIO 1", dio1.get());

    for (int i = 0; i < 2; i++) {
      SmartDashboard.putBoolean("DIO " + (i + 2), dis[i].get());
    }

    for (int i = 0; i <= 1; i++) {
      SmartDashboard.putNumber("Analog " + i, ans[i].getVoltage());
    }

    SmartDashboard.putNumber("Shooter Top", shooter.top.getSensorCollection().getQuadratureVelocity() * 10f / 4096f);
    SmartDashboard.putNumber("Shooter Bottom",
        shooter.bottom.getSensorCollection().getQuadratureVelocity() * 10f / 4096f);
    SmartDashboard.putNumber("Drive Left", drive.leftTalon.getSensorCollection().getQuadraturePosition() / 4096f);
    SmartDashboard.putNumber("Drive Right", drive.rightTalon.getSensorCollection().getQuadraturePosition() / 4096f);

    SmartDashboard.putNumber("Shooter Top Setpoint", shooter.top.getClosedLoopTarget() * 10f / 4096f);
    SmartDashboard.putNumber("Shooter Bottom Setpoint", shooter.bottom.getClosedLoopTarget() * 10f / 4096f);

    SmartDashboard.putNumber("Shooter controller speed", shooterSpeed);

  }

  @Override
  public void autonomousInit() {
    spinning = false;
    colourCount = 0;
    shooterSpeed = 0;
    runBallTrack = false;
    lastDIO0 = true;
    lastDIO1 = true;
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
  }

  /**
   * This function is called periodically during operator control.
   */

  // CONTROLS
  // X button: intake
  // Y button: nothin
  // A button: nothin
  // B button: shooter *eventually*???
  // 2 stick drive - left stick Y, right stick X
  //
  // OVERRIDES
  // Button 1: shooter (y)
  // Button 2: intake (y)
  // Button 3: ball track (x)
  // Button 4 : disk spinner (y)

  @Override
  public void teleopPeriodic() {

    // controller and joystick axises
    double leftXValue = controller.getRawAxis(0);
    double leftYValue = controller.getRawAxis(1);
    double rightXValue = controller.getRawAxis(4);
    double rightYValue = controller.getRawAxis(5);
    double joystickX = joystick.getRawAxis(0);
    double joystickY = joystick.getRawAxis(1);

    // System.out.println("Controller Value: " + leftYValue);
    // System.out.println("Set value: " + toSet);
    // System.out.println("Got value: " + twoWire.get());
    // System.out.println();

    //
    // shooter controls
    //
    if (controller.getBumper(Hand.kLeft)) { // only change speed when left bumper is pressed
      shooterSpeed = rightYValue;
    }

    // manual override
    if (joystick.getRawButton(1)) {
      shooter.setSpeed(joystickY);
    } else {
      /*
       * shooter.top.set(ControlMode.Velocity, (1 * shooterSpeed) * (4096f / 10f));
       * shooter.bottom.set(ControlMode.Velocity, (1 * -shooterSpeed) * (4096f /
       * 10f));
       */
    }

    //
    // intake controls
    //

    // intake
    if (joystick.getRawButton(2)) { // intake override
      intakeMotors[0].set(joystickY);
    } else {
      if (controller.getXButtonPressed()) {
        intaking = !intaking;
      }

      intakeMotors[0].set(intaking ? -0.5d : 0);
    }

    // ball track
    if (joystick.getRawButton(3)) { // override
      intakeMotors[1].set(joystickX);
    } else {

      boolean currentDIO0 = dio0.get();
      if (lastDIO0 != currentDIO0) {// its changed
        if (!currentDIO0) { // ball just entered, run the ball track if not already
          runBallTrack = true;
        }
      }

      boolean currentDIO1 = dio1.get();
      if (lastDIO1 != currentDIO1) {
        if (!currentDIO1) { // we just activated, start the timer
          ballTrackTimer = System.currentTimeMillis();
        }
      }

      double volts = RobotController.getBatteryVoltage();
      double wait = 12.6d * (270d / volts); // wait for 270 milis at 12.6V (what was tested at), otherwise wait more

      if (ballTrackTimer > -1 && System.currentTimeMillis() >= (ballTrackTimer + Math.round(wait))) {
        runBallTrack = false;
        ballTrackTimer = -1;
      }

      intakeMotors[1].set(runBallTrack ? 0.5d : 0);
      lastDIO0 = currentDIO0;
      lastDIO1 = currentDIO1;
    }

    //
    // colour picker thingy controls
    //

    /*
     * String gameColour = DriverStation.getInstance().getGameSpecificMessage();
     * Color readColour = colourSensor.getColor();
     * 
     * if (gameColour.length() > 0) { System.out.println(gameColour); }
     * 
     * if (joystick.getRawButton(4)) { // manual override
     * diskSpinner.set(joystickY); } else {
     * 
     * if (controller.getYButtonPressed()) { // start the spinning process // in if
     * statement so spinning doesn't accidentally get set to false spinning = true;
     * 
     * if (gameColour.length() > 0) { switch (gameColour.charAt(0)) { case 'B':
     * colourToFind = colourBlue; break; case 'G': colourToFind = colourGreen;
     * break; case 'R': colourToFind = colourRed; break; case 'Y': colourToFind =
     * colourYellow; break; default: // well fuck me i guess colourToFind = null;
     * break; } } }
     * 
     * if (spinning) { if (gameColour.length() <= 0) { // phase 1, we just want to
     * spin // basically spin it until the colour changes 24 times
     * 
     * // check if colour has changed since last time if (readColour == lastColour)
     * { colourCount++; }
     * 
     * if (colourCount <= 24) { // we've done 3 rotations, stop diskSpinner.set(0d);
     * spinning = false; colourCount = 0; }
     * 
     * } else if (colourToFind != null) { // phase 2, we need to match colour if
     * (readColour == colourToFind) { // we've found the colour, stop spinning
     * diskSpinner.set(0d); spinning = false; } } }
     * 
     * diskSpinner.set(spinning ? 0.5d : 0d); }
     */

    if (!controller.getBumper(Hand.kLeft)) {
      robotDrive.arcadeDrive(-rightXValue, -leftYValue); // make robot move
    }

    shooter.printSpeeds();
    System.out.println("Sent speed: " + shooterSpeed);
    System.out.println();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
