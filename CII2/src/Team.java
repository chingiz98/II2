import Players.Player;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Team extends Agent {
    private final int TEAM_SIZE = 4;
    private final int WAITING_FOR_PLAYERS = 0;
    private final int PRE_OPTIMIZING = 1;
    private final int OPTIMIZING_BEGIN = 2;
    private final int PLAYERS_RECEIVER = 3;
    private final int PLAYERS_SENDER = 4;
    private final int WAITING_FOR_OPTIMIZED_USER_ARRAY = 5;
    private final int TRYING_TO_OPTIMIZE = 6;


    AID LEFT;
    AID RIGHT;
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Player> receivedPlayers = new ArrayList<>();

    ArrayList<Player> result1 = new ArrayList<>();
    ArrayList<Player> result2 = new ArrayList<>();


    int avg = 0;
    int teamsCount = 0;



    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new TeamBehaviour());
        String name = getLocalName();

        System.out.println(name);

    }

    private class TeamBehaviour extends Behaviour {

        boolean done = false;
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
                        sendMsg(ACLMessage.REQUEST, new AID("players", AID.ISLOCALNAME), "getTeamsCount");
                    }

                    break;

                case OPTIMIZING_BEGIN:
                    String name = getLocalName();
                    int agentNumber = Integer.parseInt(name.replace("team", ""));
                    if(agentNumber % 2 == 1){
                        behaviour = PLAYERS_SENDER;
                    } else {
                        behaviour = PLAYERS_RECEIVER;
                    }
                    break;

                case PLAYERS_SENDER:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology() != null && msg.getOntology().equals("received")){
                        behaviour = WAITING_FOR_OPTIMIZED_USER_ARRAY;
                    }

                    agentNumber = Integer.parseInt(getLocalName().replace("team", ""));
                    if(agentNumber != teamsCount){
                        sendMsg(ACLMessage.CFP, new AID("team" + (agentNumber + 1), AID.ISLOCALNAME), "playersParcel", players);
                    }
                    break;

                case PLAYERS_RECEIVER:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null) {
                        if(msg.getOntology() != null && msg.getOntology().equals("playersParcel")){
                            try {
                                receivedPlayers = (ArrayList<Player>) msg.getContentObject();
                                System.out.println("PLAYERS RECEIVED " + getLocalName());
                                sendMsg(ACLMessage.CFP, msg.getSender(), "received");
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                            behaviour = TRYING_TO_OPTIMIZE;
                        }
                    }
                    break;

                case TRYING_TO_OPTIMIZE:

                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology() != null && msg.getOntology().equals("received")){

                        behaviour = WAITING_FOR_OPTIMIZED_USER_ARRAY;
                        done = true;
                    }

                    int rating = 0;
                    int currentTeamRating = avgRating(players);
                    int receivedTeamRating = avgRating(receivedPlayers);
                    rating = (currentTeamRating + receivedTeamRating) / 2;

                    int deltaCurrentTeamRating = Math.abs(rating - currentTeamRating);
                    int deltaReceivedTeamRating = Math.abs(rating - receivedTeamRating);

                    ArrayList<Player> t1 = new ArrayList<>();
                    ArrayList<Player> t2 = new ArrayList<>();

                    for(int i = 0; i < Math.pow(2, TEAM_SIZE); i++){
                        boolean arr[] = toBinary(i, TEAM_SIZE);
                        t1.clear();
                        t2.clear();
                        for(int j = 0; j < arr.length; j++){
                            if(arr[j]) {
                                t1.add(players.get(j));
                                t2.add(receivedPlayers.get(j));
                            } else {
                                t1.add(receivedPlayers.get(j));
                                t2.add(players.get(j));
                            }
                        }

                        int t1rating = 0;
                        for (Player p : t1) {
                            t1rating += p.rating;
                        }

                        int t2rating = 0;
                        for (Player p : t2) {
                            t2rating += p.rating;
                        }

                        t1rating /= TEAM_SIZE;
                        t2rating /= TEAM_SIZE;

                        int delta1 = Math.abs(rating - t1rating);
                        int delta2 = Math.abs(rating - t2rating);

                        if(delta1 < deltaCurrentTeamRating){
                            result1.clear();
                            result1.addAll(t1);
                            //players.clear();
                            //players.addAll(t1);
                            currentTeamRating = avgRating(t1);
                            deltaCurrentTeamRating = delta1;
                        }

                        if(delta2 < deltaReceivedTeamRating){
                            result2.clear();
                            result2.addAll(t2);
                            //receivedPlayers.clear();
                            //receivedPlayers.addAll(t2);
                            receivedTeamRating = avgRating(t2);
                            deltaReceivedTeamRating = delta2;
                        }

                    }
                    agentNumber = Integer.parseInt(getLocalName().replace("team", ""));
                    sendMsg(ACLMessage.CFP, new AID("team" + (agentNumber - 1), AID.ISLOCALNAME), "optimizedPlayers", result2);
                    players = result1;

                    break;

                case WAITING_FOR_OPTIMIZED_USER_ARRAY:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null) {
                        if(msg.getOntology() != null && msg.getOntology().equals("optimizedPlayers")){
                            try {
                                players = (ArrayList<Player>) msg.getContentObject();
                                System.out.println("PLAYERS RECEIVED " + getLocalName());
                                sendMsg(ACLMessage.CFP, msg.getSender(), "received");
                                done = true;
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                            behaviour = TRYING_TO_OPTIMIZE;
                        }
                    }
                    break;
            }



        }

        @Override
        public boolean done() {
            if(done){
                System.out.println("-----------------");
                System.out.println(getLocalName() + " FINISHED");
                for (Player p : players) {
                    System.out.println(p.name + " " + p.rating);
                }
                System.out.println("-----------------");


            }

            return done;
        }


        private int avgRating(ArrayList<Player> players){
            int rating = 0;
            for (Player p : players) {
                rating += p.rating;
            }

            return rating / TEAM_SIZE;
        }

        private void sendMsg(int msgType, AID r, String ontology, Serializable obj){
            ACLMessage msg = new ACLMessage(msgType);
            msg.setOntology(ontology);
            if(obj != null) {
                try {
                    msg.setContentObject(obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            msg.addReceiver(r);
            send(msg);
        }

        private void sendMsg(int msgType, AID r, String ontology){
            sendMsg(msgType, r, ontology, null);
        }

        private boolean[] toBinary(int number, int base) {
            final boolean[] ret = new boolean[base];
            for (int i = 0; i < base; i++) {
                ret[base - 1 - i] = (1 << i & number) != 0;
            }
            return ret;
        }


    }
}
