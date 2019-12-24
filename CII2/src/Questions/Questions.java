package Questions;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class Questions extends Agent
{
    private ArrayList<Question> data;
    private ArrayList<Question> sendQ   = new ArrayList<>();
    private int                 minComp = 0, maxComp = 0;
    private int lastQFound = 0;
    private int questionCount = 0;


    @Override
    public void setup()
    {
        data = new ArrayList<>();

        this.addQuestion();

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

        //addBehaviour(new RequestsServer());
    }

    public void addQuestion()
    {

        addBehaviour(new OneShotBehaviour()
        {
            @Override
            public void action()
            {

                // УКАЗАТЬ ЧИСЛО ВОПРОСОВ
                int N = 24; //12
                String namesSubj[] = new String[N];
                int compl[] = new int[N];
                String[] questions = new String[N];

                //НАЧАЛО ВСТАВОЧНОГО КОД, ВНИМАНИЕ!!!
                String fileName = "input.txt";
                String content;

                try {
                    content = Files.lines(Paths.get(fileName)).reduce("", String::concat);

                    String[] strs = new String[N];
                    int i = 0;

                    // Разбиение по строкам блеать, сраный говнокод, запись строк в strs
                    for (String retval : content.split("]", N))
                    {
                        strs[i] = retval;
                        i += 1;
                    }

                    // Разбиение по свойствам
                    System.out.println("\n\n Вопросы к билетам");
                    String[][] storage = new String[strs.length][3];

                    for (int count=0;count<strs.length;count++) {
                        i = 0;
                        String cont = strs[count];
                        for (String retval : cont.split(";", 3))
                        {
                            storage[count][i] = retval;
                            i += 1;
                        }
                    }
                    for (int count=0;count<N;count++) {
                        namesSubj[count]=storage[count][0];
                        questions[count]=storage[count][1];
                        compl[count]=Integer.parseInt(storage[count][2]);
                    }
                }
                catch (IOException ex)
                {}
                //КОНЕЦ ВСТАВКИ

                ContainerController cc =  getContainerController();
                for(int count = 1; count < N ; count++)
                    try
                    {
                        cc.createNewAgent("questionAgent"+System.currentTimeMillis(), "Questions.Question",
                                new Object[]{questions[count], namesSubj[count], compl[count], 1}).start();
/*                    cc.createNewAgent("questionAgent" + System.currentTimeMillis(), "Questions.Question",
                                                           new Object[]{nameQuestion, nameSubj,complex, 2}).start();*/
                        doWait(5);
                    }
                    catch (StaleProxyException e)
                    {
                        e.printStackTrace();
                    }
            }
        });

    }

    public Question getQuestion(String nameSubj)
    {
        for (Question q : data)
        {
            if (q.getNameSubj().equalsIgnoreCase(nameSubj))
            {
                return q;
            }
        }
        return null;
    }

    public Question getQuestion(int complex, String subj)
    {
        ListIterator<Question> iter;
        for (iter = data.listIterator(); iter.hasNext(); )
        {
            int t = iter.nextIndex();
            Question q = iter.next();
            if (q.getComplex() == complex && !q.getNameSubj().equals(subj) && !sendQ.contains(q))
            {
                return q;
            }
        }
        return null;
    }

    private class RequestsServer extends CyclicBehaviour
    {
        private int sendQuestionRand = 0; // Real random number

        @Override
        public void action()
        {

        }
    }

}
