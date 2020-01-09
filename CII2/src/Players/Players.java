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
import jade.wrapper.ContainerController;
import org.paukov.combinatorics3.Generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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


        System.out.println("LENGTH IS ");
        ArrayList arr = (ArrayList) Generator.combination("red", "black", "white", "green", "blue", "white", "green", "blue")
                .simple(4).stream().collect(Collectors.toList());

        System.out.println(arr.size());
        System.out.println(arr.get(0));

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




            /*
            for(int i = 0; i < playersCount; i++){
                AID id = new AID("playerAgent" + i, AID.ISLOCALNAME);
                playersAids.add(id);
            }

             */

            /*
            for (int i = 0; i < players.size() / 2; i++) {
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setContent("player_msg");
                msg.setContentObject(players.get(i));
                AID a = new AID("team1", AID.ISLOCALNAME);
                msg.addReceiver(a);
                send(msg);
            }

            for (int i = players.size() / 2; i < players.size(); i++) {
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setContent("player_msg");
                msg.setContentObject(players.get(i));
                msg.addReceiver(new AID("team2", AID.ISLOCALNAME));
                send(msg);
            }

             */

/*

            for (int i = 0; i < playersAids.size() / 2; i++) {
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setContent("player_msg");
                msg.setContentObject(playersAids.get(i));
                AID a = new AID("team1", AID.ISLOCALNAME);
                msg.addReceiver(a);
                send(msg);
            }

            for (int i = playersAids.size() / 2; i < playersAids.size(); i++) {
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setContent("player_msg");
                msg.setContentObject(playersAids.get(i));
                msg.addReceiver(new AID("team2", AID.ISLOCALNAME));
                send(msg);
            }

            */


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

        int behaviour = 0;
        MessageTemplate mt;


        @Override
        public void action() {

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
            }




        }
    }

}
