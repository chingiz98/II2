import Players.Player;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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
    private final int OPTIMIZING_BEGIN_STEP_1 = 2;
    private final int PLAYERS_RECEIVER_STEP_1 = 3;
    private final int PLAYERS_SENDER_STEP_1 = 4;
    private final int WAITING_FOR_OPTIMIZED_USER_ARRAY_STEP_1 = 5;
    private final int TRYING_TO_OPTIMIZE_STEP_1 = 6;
    private final int PLAYERS_SENDER_STEP_2 = 7;
    private final int PLAYERS_RECEIVER_STEP_2 = 8;
    private final int TRYING_TO_OPTIMIZE_STEP_2 = 9;
    private final int WAITING_FOR_OPTIMIZED_USER_ARRAY_STEP_2 = 10;

    private final int WAITING_FOR_APPROVAL = 11;


    AID LEFT;
    AID RIGHT;
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Player> receivedPlayers = new ArrayList<>();

    ArrayList<Player> result1 = new ArrayList<>();
    ArrayList<Player> result2 = new ArrayList<>();


    int avg = 0;
    int teamsCount = 0;

    int anotherMatchRating = 0;

    int temp_count = 0;
    int temp_count2 = 0;

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
                            System.out.println("-----------------");
                            System.out.println(getLocalName() + " STARTED");
                            for (Player p : players) {
                                System.out.println(p.name + " " + p.rating);
                            }

                            System.out.println("AVG: " + avgRating(players));
                            System.out.println("-----------------");
                            teamsCount = Integer.parseInt(msg.getContent());
                            behaviour = OPTIMIZING_BEGIN_STEP_1;
                        }
                    } else {
                        sendMsg(ACLMessage.REQUEST, new AID("players", AID.ISLOCALNAME), "getTeamsCount");
                    }

                    break;

                case OPTIMIZING_BEGIN_STEP_1:
                    String name = getLocalName();
                    int agentNumber = Integer.parseInt(name.replace("team", ""));
                    if(agentNumber % 2 == 1){
                        behaviour = PLAYERS_SENDER_STEP_1;
                    } else {
                        behaviour = PLAYERS_RECEIVER_STEP_1;
                    }
                    break;

                case PLAYERS_SENDER_STEP_1:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology() != null && msg.getOntology().equals("received")){
                        behaviour = WAITING_FOR_OPTIMIZED_USER_ARRAY_STEP_1;
                    }

                    agentNumber = Integer.parseInt(getLocalName().replace("team", ""));
                    if(agentNumber != teamsCount){
                        sendMsg(ACLMessage.CFP, new AID("team" + (agentNumber + 1), AID.ISLOCALNAME), "playersParcel", players);
                    }
                    break;

                case PLAYERS_RECEIVER_STEP_1:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null) {
                        if(msg.getOntology() != null && msg.getOntology().equals("playersParcel")){
                            try {
                                receivedPlayers.clear();
                                receivedPlayers.addAll((ArrayList<Player>) msg.getContentObject());
                                //System.out.println("!PLAYERS RECEIVED! " + getLocalName());
                                sendMsg(ACLMessage.CFP, msg.getSender(), "received");
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                            behaviour = TRYING_TO_OPTIMIZE_STEP_1;
                        }
                    }
                    break;


                case WAITING_FOR_APPROVAL:
                    agentNumber = Integer.parseInt(getLocalName().replace("team", ""));
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology() != null) {
                        //System.out.println("Ont  " + msg.getOntology());
                        if(msg.getOntology().equals("stop")){
                            //System.out.println("STOP RECEIVED AGENT " + agentNumber);
                            sendMsg(ACLMessage.CFP, msg.getSender(), "received");
                            done = true;
                            return;
                        }
                        if(msg.getOntology().equals("continue")){
                            //System.out.println("CONTINUE RECEIVED AGENT " + agentNumber);
                            sendMsg(ACLMessage.CFP, msg.getSender(), "received");
                            if(agentNumber % 2 == 0){
                                behaviour = PLAYERS_SENDER_STEP_2;
                            } else {
                                behaviour = PLAYERS_RECEIVER_STEP_2;
                            }
                        }


                    }


                    if(agentNumber % 2 == 0){
                        int difference = Math.abs(avgRating(result1) - avgRating(result2));
                        //System.out.println("DIFF " + avgRating(result1) + " " + avgRating(result2) + " num " + agentNumber);
                        sendMsg(ACLMessage.REQUEST, new AID("players", AID.ISLOCALNAME), "optResult", difference);
                    }

                    break;

                case TRYING_TO_OPTIMIZE_STEP_1:

                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology() != null && msg.getOntology().equals("received")){

                        behaviour = WAITING_FOR_APPROVAL;

                        /*
                        temp_count++;
                        if(temp_count == 3){
                            done = true;
                            return;
                        }

                         */

                        //behaviour = PLAYERS_SENDER_STEP_2;
                        //done = true;
                    }

                    int rating = 0;
                    int currentTeamRating = avgRating(players);
                    int receivedTeamRating = avgRating(receivedPlayers);
                    rating = (currentTeamRating + receivedTeamRating) / 2;

                    int deltaCurrentTeamRating = Math.abs(rating - currentTeamRating);
                    int deltaReceivedTeamRating = Math.abs(rating - receivedTeamRating);

                    ArrayList<Player> t1 = new ArrayList<>();
                    ArrayList<Player> t2 = new ArrayList<>();

                    boolean conditionFlag = false;

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

                        int t1rating = avgRating(t1);


                        int t2rating = avgRating(t2);

                        int delta1 = Math.abs(rating - t1rating);
                        int delta2 = Math.abs(rating - t2rating);


                        if(delta1 < deltaCurrentTeamRating && delta2 < deltaReceivedTeamRating){
                            result1.clear();
                            result1.addAll(t1);
                            deltaCurrentTeamRating = delta1;

                            result2.clear();
                            result2.addAll(t2);
                            deltaReceivedTeamRating = delta2;
                            conditionFlag = true;
                        }

                    }

                    if(!conditionFlag){
                        result2.clear();
                        result2.addAll(receivedPlayers);
                    }

                    agentNumber = Integer.parseInt(getLocalName().replace("team", ""));
                    sendMsg(ACLMessage.CFP, new AID("team" + (agentNumber - 1), AID.ISLOCALNAME), "optimizedPlayers", result2);
                    if(conditionFlag){
                        players.clear();
                        players.addAll(result1);
                    }


                    anotherMatchRating = (avgRating(result1) + avgRating(result2)) / 2;

                    break;

                case WAITING_FOR_OPTIMIZED_USER_ARRAY_STEP_1:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null) {
                        if(msg.getOntology() != null && msg.getOntology().equals("optimizedPlayers")){
                            try {
                                players.clear();
                                players.addAll((ArrayList<Player>) msg.getContentObject());
                                //System.out.println("!!PLAYERS RECEIVED!! " + getLocalName());
                                sendMsg(ACLMessage.CFP, msg.getSender(), "received");

                                behaviour = WAITING_FOR_APPROVAL;

                                /*
                                temp_count2++;
                                if(temp_count2 == 3){
                                    done = true;
                                    return;
                                }

                                 */
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                            //behaviour = PLAYERS_RECEIVER_STEP_2;
                        }
                    }
                    break;

                case PLAYERS_SENDER_STEP_2:

                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology() != null && msg.getOntology().equals("received")){

                        behaviour = WAITING_FOR_OPTIMIZED_USER_ARRAY_STEP_2;

                    }

                    agentNumber = Integer.parseInt(getLocalName().replace("team", ""));
                    int receiverNum = agentNumber + 1;
                    if(agentNumber == teamsCount){
                        receiverNum = 1;
                    }
                    //System.out.println("SEND " + agentNumber + " " + receiverNum);
                    sendMsg(ACLMessage.CFP, new AID("team" + receiverNum, AID.ISLOCALNAME), "playersParcel2", new Object[] {anotherMatchRating, players});
                    break;
                case PLAYERS_RECEIVER_STEP_2:

                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null) {
                        if(msg.getOntology() != null && msg.getOntology().equals("playersParcel2")){
                            try {
                                receivedPlayers.clear();
                                receivedPlayers.addAll((ArrayList<Player>) ((Object[]) msg.getContentObject())[1]);
                                anotherMatchRating = (int) ((Object[]) msg.getContentObject())[0];
                                //System.out.println("!!!PLAYERS RECEIVED!!! " + getLocalName());
                                sendMsg(ACLMessage.CFP, msg.getSender(), "received");

                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                            behaviour = TRYING_TO_OPTIMIZE_STEP_2;
                        }
                    }
                    break;

                case TRYING_TO_OPTIMIZE_STEP_2:

                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology() != null && msg.getOntology().equals("received")){



                        behaviour = PLAYERS_SENDER_STEP_1;
                        //TODO PLAYERS_SENDER

                        return;
                    }


                    currentTeamRating = avgRating(players);
                    receivedTeamRating = avgRating(receivedPlayers);
                    rating = (currentTeamRating + receivedTeamRating) / 2;

                    deltaCurrentTeamRating = Math.abs(rating - currentTeamRating);
                    deltaReceivedTeamRating = Math.abs(anotherMatchRating - receivedTeamRating);

                    t1 = new ArrayList<>();
                    t2 = new ArrayList<>();

                    conditionFlag = false;

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

                        int t1rating = avgRating(t1);
                        int t2rating = avgRating(t2);
                        int delta1 = Math.abs(rating - t1rating);
                        int delta2 = Math.abs(anotherMatchRating - t2rating);


                        if(delta1 < deltaCurrentTeamRating && delta2 < deltaReceivedTeamRating){
                            result1.clear();
                            result1.addAll(t1);
                            deltaCurrentTeamRating = delta1;

                            result2.clear();
                            result2.addAll(t2);
                            deltaReceivedTeamRating = delta2;
                            conditionFlag = true;
                        }

                    }


                    agentNumber = Integer.parseInt(getLocalName().replace("team", ""));
                    receiverNum = agentNumber - 1;
                    if(agentNumber == 1){
                        receiverNum = 4;
                    }

                    if(!conditionFlag){
                        result2.clear();
                        result2.addAll(receivedPlayers);
                    }


                    //System.out.println("SENT " + getLocalName() + " " + result2.size());
                    sendMsg(ACLMessage.CFP, new AID("team" + receiverNum, AID.ISLOCALNAME), "optimizedPlayers", result2);
                    if(conditionFlag){
                        players.clear();
                        players.addAll(result1);
                    }

                    //System.out.println("ADDED " + getLocalName() + " " + result1.size());
                    int a = 5;
                    //done = true;
                    break;

                case WAITING_FOR_OPTIMIZED_USER_ARRAY_STEP_2:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null) {
                        if(msg.getOntology() != null && msg.getOntology().equals("optimizedPlayers")){
                            try {
                                players.clear();
                                players.addAll ((ArrayList<Player>) msg.getContentObject());
                                //System.out.println("!!!!PLAYERS RECEIVED!!!! " + getLocalName() + " SIZE " + players.size());
                                sendMsg(ACLMessage.CFP, msg.getSender(), "received");
                                //TODO PLAYERS_RECEIVER
                                behaviour = PLAYERS_RECEIVER_STEP_1;
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }

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

                System.out.println("AVG: " + avgRating(players));
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
