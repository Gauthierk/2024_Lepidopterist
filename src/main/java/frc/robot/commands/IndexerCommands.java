package frc.robot.commands;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.subsystems.indexer.Indexer;

public class IndexerCommands extends Command {
  public static final int beakBreak2Port = 1;
  private static DigitalInput beambreak = new DigitalInput(beakBreak2Port);

  public static Command runIndexerUntilBeamBreak(Indexer indexer) {
    return Commands.sequence(
        indexer.speedCommand(() -> 0.4),
        new WaitUntilCommand(() -> !beambreak.get()),
        new WaitUntilCommand(2),
        indexer.speedCommand(() -> 0));
  }

  public static Command stopIndexer(Indexer indexer) {
    return indexer.stop();
  }

  public static Command runIndexer(Indexer indexer, double speed) {
    return indexer.speedCommand(() -> speed);
  }
}