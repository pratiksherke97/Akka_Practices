package behavior;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Random;

public class Racer extends AbstractBehavior<Racer.Command> {

  public interface Command extends Serializable {}

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  public static class StartCommand implements Command {
    private static final long serialVersionUID = 1l;

    private int raceLength;

  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  public static class PositionCommand implements Command {
    private static final long serialVersionUID = 1l;

    private ActorRef<RaceController.Command> controller;

  }

  private final double defaultAverageSpeed = 48.2;
  private int averageSpeedAdjustmentFactor;
  private Random random;

  private double currentSpeed = 0;

  private double getMaxSpeed() {
    return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
  }

  private double getDistanceMovedPerSecond() {
    return currentSpeed * 1000 / 3600;
  }

  private void determineNextSpeed(int raceLength, int currentPosition) {
    if (currentPosition < (raceLength / 4)) {
      currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
    }
    else {
      currentSpeed = currentSpeed * (0.5 + random.nextDouble());
    }

    if (currentSpeed > getMaxSpeed())
      currentSpeed = getMaxSpeed();

    if (currentSpeed < 5)
      currentSpeed = 5;

    if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
      currentSpeed = getMaxSpeed() / 2;
    }
  }

  private Racer(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(Racer::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return notStarted();
  }

  public Receive<Command> notStarted() {

    return newReceiveBuilder()
        .onMessage(StartCommand.class, message -> {
          this.random = new Random();
          this.averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
          return running(message.getRaceLength(), 0);
        })
        .onMessage(PositionCommand.class, message -> {
          message.getController().tell(new RaceController.RacerUpdateCommand(getContext().getSelf(), 0));
          return Behaviors.same();
        }).build();

  }

  public Receive<Command> running(int raceLength, int currentPosition) {

    return newReceiveBuilder()
        .onMessage(PositionCommand.class, message -> {
          int newPosition = currentPosition;
          determineNextSpeed(raceLength, currentPosition);
          newPosition += getDistanceMovedPerSecond();
          if (newPosition > raceLength )
            newPosition  = raceLength;
          message.getController().tell(new RaceController.RacerUpdateCommand(getContext().getSelf(), (int)newPosition));

          if(newPosition != raceLength){
            return running(raceLength, newPosition);
          }
          return completed(raceLength);
        }).build();

  }

  public Receive<Command> completed(int raceLength) {

    return newReceiveBuilder()
        .onMessage(PositionCommand.class, message -> {
          message.getController().tell(new RaceController.RacerUpdateCommand(getContext().getSelf(), raceLength));
          message.getController().tell(new RaceController.RacerFinishedCommand(getContext().getSelf()));
          return waitingToStop();
        }).build();
  }

  public Receive<Command> waitingToStop() {
    return newReceiveBuilder()
        .onAnyMessage(message -> Behaviors.same())
        .onSignal(PostStop.class, signal -> {
          getContext().getLog().info("I'm about to terminate!");
          return Behaviors.same();
        })
        .build();
  }

}


