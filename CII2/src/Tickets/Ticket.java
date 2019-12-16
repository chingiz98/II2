package Tickets;

import Questions.Question;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;

public class Ticket extends Agent
{
    boolean DEBUG = false;
    final int delta = 1;
    protected Question questions[];

    private AID myManager;
    private int neededComp;
    private int totalComplex = -1;

    @Override
    public void setup()
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ticket");
        sd.setName("OneTicket");
        dfd.addServices(sd);

        //--- Clear
        try {
            FileOutputStream writer = new FileOutputStream("output.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //---

        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        questions = new Question[2];
        myManager = (AID) getArguments()[0];
        addBehaviour(new RequestPerformer());
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
    }

    public Question getFirstQuestion()
    {
        return questions[0];
    }

    public Question getSecondQuestion()
    {
        return questions[1];
    }

    public Question getQuestionNot(int n)
    {
        return (n == 0) ? questions[1] : questions[0];
    }

    public int getTotalComplex() // СЛ
{
    return (totalComplex == -1) ? totalComplex = questions[0].getComplex() + questions[1].getComplex() : totalComplex;

}

    public int getSendIndex()
    {
        if (getTotalComplex() > neededComp)
        {
            return (questions[0].getComplex() >= questions[1].getComplex()) ? 0 : 1;
        }
        else
        {

            return (questions[0].getComplex() <= questions[1].getComplex()) ? 0 : 1;
        }
    }

    public boolean isReady()
    {
        return (Math.abs(getTotalComplex() - neededComp) <= delta);
    }

    public int needSwap(Question sw)
    {
        int d  = Math.abs(getTotalComplex() - neededComp);
        int d0 = Math.abs(sw.getComplex() + questions[0].getComplex() - neededComp);
        int d1 = Math.abs(sw.getComplex() + questions[1].getComplex() - neededComp);
        if (d0 <= d1 && d0 <= d && !sw.getNameSubj().equals(getFirstQuestion()) )

        {

            return 1;
        }
        else if (d1 < d0 && d1 <= d && !sw.getNameSubj().equals(getSecondQuestion()))
        {
            return 0;
        }
        return -1;
    }

    public boolean needSwapWithout(int without, Question sw) //Сложность
    {
        int d = Math.abs(getTotalComplex() - neededComp);

        int d2 = 0;
        if (without == 0 && !sw.getNameSubj().equals(getFirstQuestion()))
        {
            d2 = Math.abs(sw.getComplex() + questions[1].getComplex() - neededComp);

        }
        else if(!sw.getNameSubj().equals(getSecondQuestion()))
        {
            d2 = Math.abs(sw.getComplex() + questions[0].getComplex() - neededComp);

        }
        if (d2 < d)
        {

            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "[1 Bonpoc]{" + questions[0].toString() + "} [2 Bonpoc]{" + questions[1].toString() + "}";
    }

    public boolean equals(Ticket t)
    {
        System.out.println("equals t = "+t);
        return ((questions[0].equals(t.getFirstQuestion()) || questions[0].equals(t.getSecondQuestion())) &&
                (questions[1].equals(t.getFirstQuestion()) || questions[1].equals(t.getSecondQuestion())));
    }

    private class RequestPerformer extends Behaviour
    {
        ArrayList<DFAgentDescription> questionsAgent, tickets;
        private int step = 0, receiver = 0, questionsCount = 0, request, propose, skipCount = 0;
        private boolean done = false;
        private MessageTemplate mt;
        Question proposeQuestion;

        ArrayList<DFAgentDescription> searchByType(String type)
        {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd       = new ServiceDescription();
            sd.setType(type);
            template.addServices(sd);
            ArrayList<DFAgentDescription> desc = new ArrayList<>();
            try
            {
                desc.addAll(Arrays.asList(DFService.search(myAgent, template)));
            }
            catch (FIPAException fe)
            {
                fe.printStackTrace();
            }
            for (DFAgentDescription a : desc)
            {
                if (a.getName().equals(myAgent.getAID()))
                {
                    desc.remove(a);
                    break;
                }
            }
            return desc;
        }


        @Override
        public void action()
        {
            switch (step)
            {
                case 0:
                    questionsAgent = searchByType("question");
                    if (questionsAgent.size() != 0)
                    {
                        step = 1;
                    }
                    else
                    {
                        System.err.println("Не найдено вопросов");
                        done = true;
                        deregister();
                        return;
                    }
                    break;
                case 1:
                    if (questionsCount == 2)
                    {

                        System.out.println("\n"+myAgent.getLocalName() + " start " +  " Сложность в билете: " + getTotalComplex()+"\n"+ getFirstQuestion() + "\n " + getSecondQuestion() );
                        System.out.println("--------------------------------------------------------------------");
                        if (isReady())
                        {
                            deregister();
                            done = true;
                        }
                        step = 3;
                        return;
                    }
                    if (questionsAgent.size() == receiver)
                    {
                        deregister();
                        done = true;
                        return;
                    }
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(questionsAgent.get(receiver++).getName());
                    if (questionsCount == 1)
                    {
                        cfp.setContent("get question;" + getFirstQuestion().getNameSubj());
                    }
                    else
                    {
                        cfp.setContent("get question;_");
                    }
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    cfp.setConversationId("tickets-creation");
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("tickets-creation"),
                                             MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 2;
                    break;
                case 2:
                    ACLMessage msg = myAgent.blockingReceive(mt);

                    if (msg.getPerformative() == ACLMessage.PROPOSE)
                    {
                        try
                        {
                            questions[questionsCount++] = (Question) msg.getContentObject();
                        }
                        catch (UnreadableException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    step = 1;
                    break;
                case 3:
                    cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(myManager);
                    cfp.setContent(String.valueOf(getTotalComplex()));
                    myAgent.send(cfp);
                    step = 4;
                    break;
                case 4:
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    msg = myAgent.blockingReceive(mt);
                    neededComp = Integer.parseInt(msg.getContent());

                    step = 5;
                    receiver = 0;
                    break;
                case 5:
                    if (isReady())
                    {
                        deregister();
                        done = true;
                        return;
                    }
                    skipCount = 0;
                    if (getTotalComplex() > neededComp)
                    {
                        step = 6;
                    }
                    else
                    {
                        step = 7;
                    }

                    break;
                case 6:
                    tickets = searchByType("ticket");

                    if (receiver >= tickets.size())
                    {
                        deregister();
                        done = true;
                        return;
                    }
                    ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                    req.addReceiver(tickets.get(receiver).getName());
                    try
                    {
                        request = getSendIndex();
                        req.setContentObject(questions[request]);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    myAgent.send(req);
                    step = 8;
                    break;
                case 7:
                    tickets = searchByType("ticket");
                    if(tickets.size() == 0)
                    {
                        deregister();
                        done = true;
                        return;
                    }

                    mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                    msg = myAgent.blockingReceive(mt, 1000);
                    if(msg != null)
                    {
                        Question q = null;
                        try
                        {
                            q = (Question) msg.getContentObject();
                        }
                        catch (UnreadableException e)
                        {
                            e.printStackTrace();
                        }
                        ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
                        propose = needSwap(q);
                        proposeQuestion = q;
                        if (propose > -1)
                        {
                            reply.setContent("confirm");
                            try
                            {
                                reply.setContentObject(questions[propose]);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            step = 9;
                        }
                        else
                        {
                            reply.setContent("disconfirm");
                        }
                        reply.addReceiver(msg.getSender());
                        myAgent.send(reply);

                    }
                    else
                    {
                        skipCount++;
                        if(skipCount == 10)
                        {
                            deregister();
                            done = true;
                            return;
                        }
                    }
                    break;
                case 8:

                    mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                    msg = myAgent.blockingReceive(mt, 500);
                    if(msg != null)
                    {
                        if (!msg.getContent().equalsIgnoreCase("disconfirm"))
                        {
                            Question q = null;
                            try
                            {
                                q = (Question) msg.getContentObject();

                            }
                            catch (UnreadableException e)
                            {
                                e.printStackTrace();
                            }
                            ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
                            if (needSwapWithout(request, q))
                            {
                                System.out.println("\r\n"+"Обмен " + myAgent.getLocalName() + " " + questions[request] + " - " + q + "  " + msg.getSender().getLocalName()+"\r\n");
                                questions[request] = q;
                                reply.setContent("swap");
                            }
                            else
                            {
                                reply.setContent("not swap");
                            }
                            reply.addReceiver(msg.getSender());
                            myAgent.send(reply);
                        }
                        if (isReady())
                        {
                            deregister();
                            done = true;
                            return;
                        }
                        receiver++;
                        step = 5;
                    }
                    else
                    {
                        step = 6;
                        skipCount++;
                        if(skipCount == 10)
                        {
                            skipCount = 0;
                            receiver++;
                        }
                    }
                    break;
                case 9:

                    mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                    msg = myAgent.blockingReceive(mt);

                    if (msg.getContent().equalsIgnoreCase("swap"))
                    {
                        questions[propose] = proposeQuestion;
                        step = 5;
                    }
                    else
                    {
                        step = 7;
                    }
                    break;
            }
        }



        String newLine = System.lineSeparator();

        @Override
        public boolean done()
        {

            if (done && Ticket.this.questions[0] != null && Ticket.this.questions[1] != null) // ! Добавить "разные темы"!
            {
                System.out.println(newLine + myAgent.getName() + " :" +  " Сложность в билете = " + ((int)questions[0].getComplex() + (int)questions[1].getComplex())+ newLine + Ticket.this.questions[0] + newLine  + Ticket.this.questions[1]);//+getTotalComplex());


                try(FileWriter writer = new FileWriter("output.txt", true))
                {
                    // запись всей строки
                    String text = myAgent.getName() + " :" + "Сложность="+ ((int)questions[0].getComplex() + (int)questions[1].getComplex())+ newLine +Ticket.this.questions[0] + newLine + Ticket.this.questions[1] + newLine;
                    writer.write(text);
                    // запись по символам
                    writer.append('\n');

                    writer.flush();
                }
                catch(IOException ex){

                    System.out.println(ex.getMessage());
                }
            }
            else if (done)
            {
                System.out.println("Агенту " + myAgent.getLocalName() + " не осталось вопросов" );
            }
            return done;
        }
    }
}