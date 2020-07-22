import akka.actor.typed.ActorSystem;
import behavior.ManagerBehavior;

public class Main {

//  public static void main(String[] args) {
//    ActorSystem<String> firstActorSystem = ActorSystem
//        .create(behavior.FirstSimpleBehavior.create(), "FirstActorSystem");
//    firstActorSystem.tell("say hello");
//    firstActorSystem.tell("who are you?");
//    firstActorSystem.tell("create a child");
//    firstActorSystem.tell("Hello are you there?");
//  }

  public static void main(String[] args) {
    ActorSystem<ManagerBehavior.Command> bigPrimes = ActorSystem
        .create(ManagerBehavior.create(), "BigPrimes");
    bigPrimes.tell(new ManagerBehavior.InstructionCommand("start"));
  }
}
