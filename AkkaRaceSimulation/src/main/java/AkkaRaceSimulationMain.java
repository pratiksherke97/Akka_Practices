import akka.actor.typed.ActorSystem;
import behavior.RaceController;

public class AkkaRaceSimulationMain {

  public static void main(String[] args) {

    ActorSystem<RaceController.Command> raceController = ActorSystem
        .create(RaceController.create(), "RaceController");

    raceController.tell(new RaceController.StartCommand());

  }
}
