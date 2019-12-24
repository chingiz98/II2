package Players;


import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ContainerController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Players extends Agent {


    private int                 minComp = 0, maxComp = 0;
    private int lastQFound = 0;
    private int questionCount = 0;

    private int playersCount = 0;

    private int avgRating = 0;
    private int count = 0;
    @Override
    protected void setup() {
        super.setup();



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
                cc.createNewAgent("playerAgent" + playersCount++, "Players.Player",
                        new Object[]{contents[0], contents[1], contents[2]}).start();

                doWait(5);

            }

            avgRating /= count;

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



    }
}
