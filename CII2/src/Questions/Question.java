package Questions;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.Serializable;

public class Question extends Agent implements Serializable
{
    String nameQuestion;
    String nameSubj;
    int    complex;
    int    inAllTicketsCount;
    CyclicBehaviour behaviour;

    @Override
    public void setup()
    {
        Object[] args = getArguments();
        nameQuestion = (String) args[0];
        nameSubj = (String) args[1];
        complex = (int) args[2];
        inAllTicketsCount = (int) args[3];

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("question");
        sd.setName("OneQuestion");
        dfd.addServices(sd);

        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        System.out.println(this);

        behaviour = new RequestsServer(this);
        addBehaviour(behaviour);
    }

    public void deregister()
    {
        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException e)
        {
            e.printStackTrace();
        }
        removeBehaviour(behaviour);
    }

    public String getNameQuestion()
    {
        return nameQuestion;
    }

    public String getNameSubj()
    {
        return nameSubj;
    }

    public int getComplex()
    {
        return complex;
    }

    @Override
    public String toString()
    {
        return " [ Тематика ]=" + nameSubj + " [ Bonpoc ]=" + nameQuestion +  " [ Сложность ]=" + complex;
        //return " [ Тематика ]=" + nameQuestion + " [ Вопрос ]=" + nameSubj + " [ Сложность ]=" + complex;
    }

    public boolean equals(Question q)
    {
        return (nameQuestion.equals(q.getNameQuestion()) &&
                nameSubj.equals(q.getNameSubj()) &&
                complex == q.getComplex());
    }

    private class RequestsServer extends CyclicBehaviour
    {
        Question q;

        public RequestsServer(Question q)
        {
            this.q = q;
        }

        @Override
        public void action()
        {
            MessageTemplate mt  = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage      msg = myAgent.receive(mt);
            ACLMessage      reply;
            if(msg != null)
            {
                reply = msg.createReply();
                String[] message = msg.getContent().split(";");
                if(message[0].equalsIgnoreCase("get question") && inAllTicketsCount != 0 && !message[1].equals(nameSubj))
                {
                    try
                    {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContentObject(q);
                        inAllTicketsCount--;
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else
            {
                block();
            }
        }
    }
}
