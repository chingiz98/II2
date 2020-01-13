package lab2;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main
{

    static AgentController players;

    public static void main(String[] args)
    {
        Runtime rt = Runtime.instance();
        Profile p  = new ProfileImpl();
        p.setParameter("gui", "true");
        ContainerController cc = rt.createMainContainer(p);
        rt.setCloseVM(true);


        try
        {

            players = cc.createNewAgent("players", "Players.Players", args);
            players.start();


        }
        catch (StaleProxyException e)
        {
            e.printStackTrace();
        }
    }
}