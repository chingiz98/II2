package Players;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class Player extends Agent {
    String name;
    int rating;
    boolean sex;

    CyclicBehaviour behaviour;

    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        name = (String) args[0];
        rating = Integer.parseInt((String) args[1]);
        sex = ((String) args[2]).equals("1");

        System.out.println("Agent " + getAID() + " started");
    }
}
