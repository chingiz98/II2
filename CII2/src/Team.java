import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class Team extends Agent {
    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new TeamBehaviour());
    }

    private class TeamBehaviour extends CyclicBehaviour {

        @Override
        public void action() {

        }
    }
}
