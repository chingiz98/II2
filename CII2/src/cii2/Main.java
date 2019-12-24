package cii2;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main
{
    static AgentController questions;
    static AgentController tickets;

    static AgentController players;
    //static AgentController tickets;

    public static void main(String[] args)
    {
        Runtime rt = Runtime.instance();
        Profile p  = new ProfileImpl();
        p.setParameter("gui", "true");
        ContainerController cc = rt.createMainContainer(p);
        rt.setCloseVM(true);

        System.out.println("SAS");

        try
        {


            players = cc.createNewAgent("players", "Players.Players", args);
            players.start();
            /*
            questions = cc.createNewAgent("questions"+System.currentTimeMillis(),
                                          "Questions.Questions",
                                          args);
            questions.start();
            tickets = cc.createNewAgent("tickets"+System.currentTimeMillis(),
                                        "Tickets.Tickets",
                                        args);
            tickets.start();

             */

        }
        catch (StaleProxyException e)
        {
            e.printStackTrace();
        }
    }
}