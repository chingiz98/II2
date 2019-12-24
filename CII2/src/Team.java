import Players.Player;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class Team extends Agent {
    private final int TEAM_SIZE = 4;
    private final int WAITING_FOR_PLAYERS = 0;
    private final int PRE_OPTIMIZING = 1;
    private final int OPTIMIZING_BEGIN = 2;


    AID LEFT;
    AID RIGHT;
    ArrayList<Player> players = new ArrayList<>();

    int avg = 0;
    int teamsCount = 0;



    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new TeamBehaviour());
        String name = getLocalName();

        System.out.println(name);

    }

    private class TeamBehaviour extends CyclicBehaviour {

        int behaviour = 0;
        MessageTemplate mt;

        public void calcAvg() {
            avg = 0;
            for (Player p : players) {
                avg += p.rating;
            }

            avg /= players.size();
        }

        @Override
        public void action() {

            switch (behaviour){
                case WAITING_FOR_PLAYERS:
                    if(players.size() < TEAM_SIZE){
                        mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                        ACLMessage msg = myAgent.blockingReceive(mt, 100);
                        Player player = null;
                        try {
                            player = (Player) msg.getContentObject();
                            players.add(player);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        System.out.println("HELLO! " + msg.getContent());

                        calcAvg();

                        if(players.size() == TEAM_SIZE)
                            behaviour = PRE_OPTIMIZING;
                    }
                    break;
                case PRE_OPTIMIZING:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    ACLMessage msg = myAgent.blockingReceive(mt, 100);


                    if(msg != null) {
                        if(msg.getOntology() != null && msg.getOntology().equals("teamsCount")){
                            teamsCount = Integer.parseInt(msg.getContent());
                            behaviour = OPTIMIZING_BEGIN;
                        }
                    } else {
                        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                        request.setOntology("getTeamsCount");
                        request.addReceiver(new AID("players", AID.ISLOCALNAME));
                        send(request);
                    }

                    break;

                case OPTIMIZING_BEGIN:

                    break;
            }

        }
    }
}
