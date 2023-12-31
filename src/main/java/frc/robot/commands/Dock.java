package frc.robot.commands;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Drivetrain;

public class Dock extends CommandBase{

    private final PIDController m_PID = new PIDController(0.025, 0, 0);
    private final Drivetrain m_drive;
    private boolean m_finished = false;
    private final Timer m_timer = new Timer();
    private double timeBalance = 0.0;
    private boolean balance = false;

    public Dock(Drivetrain drive){
        m_drive = drive;
        addRequirements(m_drive);
    }

      // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_finished = false;
    balance = false;
    m_timer.reset();
    m_timer.start();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    double pitch = m_drive.getPitch();
    double pidOutput = m_PID.calculate(pitch, 0);

    //if the current state is not balanced and the pitch angle gets within 
    //7deg of horizontal then record the current time and set state to balanced
    if(Math.abs(pitch) <= 7.0 && !balance){
        timeBalance = m_timer.get();
        balance = true;
    }
    //Otherwise if it has been over 1 second and the state is set to balanced but the pitch angle now 
    //exceeds 7deg then set state to not balanced and begin allowing PID to correct angle
    else if(Math.abs(pitch) > 7.0 && balance && m_timer.get() - timeBalance > 1.0){
        balance = false;
        m_drive.drive(-1.0*pidOutput, 0.0, 0.0, false, false);
    }
    //Otherwise if the state is not balanced and the pitch exceeds 7deg then allow PID to correct angle
    else if(Math.abs(pitch) > 7.0 && !balance){
        m_drive.drive(-1.0*pidOutput, 0.0, 0.0, false, false);
    }
    //Otherwise if the pitch is less than 7deg set the speed output to 0, 
    //while this is true, if it also passes 1s from when the pitch was last balanced then
    //end the command because we are stablilized on the charge station
    else if(Math.abs(pitch) <= 7.0){  
        m_drive.drive(0, 0, 0, false, false);
        if(m_timer.get() - timeBalance > 1.0){
            m_finished = true;
        }
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_drive.stop();
    m_timer.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return m_finished;
  }




    
}
