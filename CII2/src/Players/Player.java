package Players;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import java.io.Serializable;

public class Player implements Serializable {
    public String name;
    public int rating;
    public boolean sex;


    public Player(String name, int rating, boolean sex){
        this.name = name;
        this.rating = rating;
        this.sex = sex;
    }

/*
    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        name = (String) args[0];
        rating = Integer.parseInt((String) args[1]);
        sex = ((String) args[2]).equals("1");

        System.out.println("Agent " + getAID() + " started");
    }
    */

}
