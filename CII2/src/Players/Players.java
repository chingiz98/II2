package Players;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ContainerController;
import org.paukov.combinatorics3.Generator;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Players extends Agent {


    private int                 minComp = 0, maxComp = 0;
    private int lastQFound = 0;
    private int playersCount = 0;

    private int avgRating = 0;
    private int count = 0;

    private ArrayList<Player> players = new ArrayList<>();

    private int teamsCount = 2;

    @Override
    protected void setup() {
        super.setup();


        /*
        System.out.println("LENGTH IS ");
        ArrayList arr = (ArrayList) Generator.combination("red", "black", "white", "green", "blue", "white", "green", "blue")
                .simple(4).stream().collect(Collectors.toList());

        System.out.println(arr.size());
        System.out.println(arr.get(0));
        
         */

        String fileName = "inp.txt";
        String content;
        ContainerController cc =  getContainerController();
        try {
            File file=new File(fileName);    //creates a new file instance
            FileReader fr=new FileReader(file);   //reads the file
            BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream

            String line;

            while((line=br.readLine())!=null)
            {
                String contents[] = line.split(" ");

                avgRating += Integer.parseInt(contents[1]);
                count++;

                /*
                cc.createNewAgent("playerAgent" + playersCount++, "Players.Player",
                        new Object[]{contents[0], contents[1], contents[2]}).start();

                doWait(5);
                */

                players.add(new Player((String) contents[0], Integer.parseInt((String) contents[1]), ((String) contents[2]).equals("1")));


            }

            avgRating /= count;


            cc.createNewAgent("team1", "Team", new Object[]{}).start();
            cc.createNewAgent("team2", "Team", new Object[]{}).start();
            cc.createNewAgent("team3", "Team", new Object[]{}).start();
            cc.createNewAgent("team4", "Team", new Object[]{}).start();

            teamsCount = 4;

            int counter = 0;
            int currTeam = 1;
            for(int i = 0; i < players.size(); i++){
                if(counter == 4){
                    counter = 0;
                    currTeam++;
                }
                counter++;
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setContent("player_msg");
                msg.setContentObject(players.get(i));
                msg.addReceiver(new AID("team" + currTeam, AID.ISLOCALNAME));
                send(msg);
            }



        } catch (Exception e){

        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("questions-agent");
        sd.setName("Questions-storage");
        dfd.addServices(sd);

        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        addBehaviour(new PlayersBehaviour());

    }


    private class PlayersBehaviour extends CyclicBehaviour {

        private final int WAITING_FOR_MESSAGE = 0;
        private final int MAKING_DECISION = 1;
        private final int BROADCAST_CONTINUE = 2;
        private final int BROADCAST_STOP = 3;

        int behaviour = WAITING_FOR_MESSAGE;
        MessageTemplate mt;


        byte flags[] = new byte[teamsCount + 1];
        ArrayList<Integer> ratings = new ArrayList<>();
        int previousAvg = 0;
        int retries = 0;

        int receivedCnt = 0;
        @Override
        public void action() {




            switch (behaviour) {
                case WAITING_FOR_MESSAGE:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    ACLMessage msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null){
                        if(msg.getOntology().equals("getTeamsCount"))
                        {
                            ACLMessage reply = new ACLMessage(ACLMessage.CFP);
                            reply.setContent(String.valueOf(teamsCount));
                            reply.setOntology("teamsCount");
                            reply.addReceiver(msg.getSender());
                            send(reply);
                        }

                        if(msg.getOntology().equals("optResult")){
                            int agentNumber = Integer.parseInt(msg.getSender().getLocalName().replace("team", ""));
                            if(flags[agentNumber] == 0) {
                                flags[agentNumber] = 1;
                                try {
                                    ratings.clear();
                                    ratings.add((int) msg.getContentObject());
                                } catch (UnreadableException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(sum(flags) == teamsCount / 2){
                                behaviour = MAKING_DECISION;
                                flags = new byte[teamsCount + 1];
                            }

                        }
                    }
                    break;

                case MAKING_DECISION:

                    if(retries == 0){
                        previousAvg = avgRating(ratings);
                    }

                    if(retries == 5){
                        behaviour = BROADCAST_STOP;
                        //broadcast("stop");
                        System.out.println("stopping " + retries );
                        retries = 0;
                        //behaviour = WAITING_FOR_MESSAGE;
                        return;
                    }

                    if(avgRating(ratings) < previousAvg){
                        previousAvg = avgRating(ratings);
                        retries = 0;
                        System.out.println("new best " + previousAvg );
                        behaviour = BROADCAST_CONTINUE;
                        //broadcast("continue");
                    } else {
                        retries++;
                        //broadcast("continue");

                        System.out.println("retrying " + retries + " AVG: " + avgRating(ratings) + " prevAVG: " + previousAvg);
                        behaviour = BROADCAST_CONTINUE;
                    }


                    //behaviour = WAITING_FOR_MESSAGE;
                    break;

                case BROADCAST_CONTINUE:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology().equals("received")) {
                        receivedCnt++;
                        if(receivedCnt == teamsCount){
                            behaviour = WAITING_FOR_MESSAGE;
                            receivedCnt = 0;
                            return;
                        }

                    }

                    broadcast("continue");

                    break;

                case BROADCAST_STOP:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt, 100);

                    if(msg != null && msg.getOntology().equals("received")) {
                        receivedCnt++;
                        if(receivedCnt == teamsCount){
                            behaviour = WAITING_FOR_MESSAGE;
                            receivedCnt = 0;
                            return;
                        }

                    }

                    broadcast("stop");

                    break;

            }






        }


        private void broadcast(String msg){

            int currTeam = 1;
            for(int i = 0; i < teamsCount; i++){
                sendMsg(ACLMessage.CFP, new AID("team" + currTeam, AID.ISLOCALNAME), msg);
                currTeam++;
            }
        }

        private int avgRating(ArrayList<Integer> players){
            int rating = 0;
            for (int p : players) {
                rating += p;
            }

            return rating / players.size();
        }

        private int sum(byte[] a){
            int s = 0;
            for (int i = 0; i < a.length; i++){
                s += a[i];
            }

            return s;
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
    }

}
