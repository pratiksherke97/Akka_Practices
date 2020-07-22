package behavior;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

  private WorkerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create(){
    return Behaviors.setup(WorkerBehavior::new);
  }

  @Override
  public Receive<Command> createReceive() {

    return handleInitialReceive();
  }

  /**
   * Runs only at the initial receive.
   *
   *  @return
   */
  public Receive<Command> handleInitialReceive() {

    return newReceiveBuilder()
        .onAnyMessage(
            command -> {
              BigInteger integer = new BigInteger(2000, new Random());
              BigInteger prime = integer.nextProbablePrime();
              command.getSender().tell(new ManagerBehavior.ResultCommand(prime));

              return handleSubsequentReceive(prime);
            })
        .build();
  }

  /**
   * Runs for all the subsequent receives.
   *
   *  @return
   */
  private Behavior<Command> handleSubsequentReceive(BigInteger prime) {

    return newReceiveBuilder()
        .onAnyMessage(command -> {
          command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
          return Behaviors.same();
        })
        .build();


  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  public static class Command implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;
    private ActorRef<ManagerBehavior.Command> sender;

  }

}
