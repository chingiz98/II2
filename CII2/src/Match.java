import Players.Player;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public class Match extends Agent{

    ArrayList<Player> team1 = new ArrayList<>();
    ArrayList<Player> team2 = new ArrayList<>();

    int teamsCount = 0;
    int TEAM_SIZE = 4;


    @Override
    protected void setup() {
        super.setup();


        Object[] args = getArguments();
        team2.addAll((ArrayList) args[0]);
        team1.addAll((ArrayList) args[1]);
        teamsCount = (int) args[2];
        int a = 5;

        /*

        String result = "";

        result+= "-----------------\n";
        result+=getLocalName() + " FINISHED\n";
        for (Player p : team1) {
            result+= p.name + " " + p.rating + " " + p.getSex() + "\n";
        }

        result+="AVG: " + avgRating(team1) + "\n";




        for (Player p : team2) {
            result+= p.name + " " + p.rating + " " + p.getSex() + "\n";
        }

        result+="AVG: " + avgRating(team2) + "\n";
        result+= "-----------------\n";
        System.out.println(result);
        */

        addBehaviour(new MatchBehaviour());

    }

    private class MatchBehaviour extends Behaviour {

        private final int TRYING_TO_OPTIMIZE_LOCALLY = 0;

        int behaviour = 0;

        int EPSILON = 30;

        boolean done = false;

        @Override
        public void action() {

            switch (behaviour) {
                case TRYING_TO_OPTIMIZE_LOCALLY:

                    ArrayList<Player> t1 = new ArrayList<>();
                    ArrayList<Player> t2 = new ArrayList<>();

                    ArrayList<Player> result1 = new ArrayList<>();
                    ArrayList<Player> result2 = new ArrayList<>();


                    int delta1 = difCount(team1);
                    int delta2 = difCount(team2);
                    int avgRating1 = avgRating(team1);
                    int avgRating2 = avgRating(team2);



                    for(int i = 0; i < Math.pow(2, team1.size()); i++){
                        boolean arr[] = toBinary(i, team1.size());
                        t1.clear();
                        t2.clear();
                        for(int j = 0; j < arr.length; j++){
                            if(arr[j]) {
                                t1.add(team1.get(j));
                                t2.add(team2.get(j));
                            } else {
                                t1.add(team2.get(j));
                                t2.add(team1.get(j));
                            }
                        }


                        //int EPS1 = EPSILON;
                        //int EPS2 = EPSILON;


                        if(difCount(t1) < delta1 && difCount(t2) < delta2
                                && Math.abs(avgRating(t1) - avgRating1) < EPSILON
                                && Math.abs(avgRating(t2) - avgRating2) < EPSILON){
                            //EPS1 = Math.abs(avgRating(t1) - avgRating1);
                            //EPS2 = Math.abs(avgRating(t2) - avgRating2);
                            delta1 = difCount(t1);
                            delta2 = difCount(t2);

                            result1.clear();
                            result1.addAll(t1);

                            result2.clear();
                            result2.addAll(t2);

                            System.out.println("TRANSITION");

                        }


                    }

                    if(result1.size() > 0 && result2.size() > 0){
                        team1.clear();
                        team1.addAll(result1);

                        team2.clear();
                        team2.addAll(result2);
                    }

                    done = true;
                    break;
            }



        }

        @Override
        public boolean done() {

            int agentNumber = Integer.parseInt(getLocalName().replace("Match", ""));

            String result = "";

            if(done){
                result+= "-----------------\n";
                result+=getLocalName() + " FINISHED\n";
                result += "team" + ((agentNumber * 2) - 1) + "\n";
                for (Player p : team1) {
                    result+= p.name + " " + p.rating + " " + p.getSex() + "\n";
                }

                result+="AVG: " + avgRating(team1) + "\n\n";



                result += "team" + ((agentNumber * 2)) + "\n";
                for (Player p : team2) {
                    result+= p.name + " " + p.rating + " " + p.getSex() + "\n";
                }

                result+="AVG: " + avgRating(team2) + "\n";
                result+= "-----------------\n";
                System.out.println(result);

            }


            return done;
        }

        private boolean[] toBinary(int number, int base) {
            final boolean[] ret = new boolean[base];
            for (int i = 0; i < base; i++) {
                ret[base - 1 - i] = (1 << i & number) != 0;
            }
            return ret;
        }



        private int difCount(ArrayList<Player> players) {
            int boysCount = 0;
            int girlsCount = 0;
            for (Player p : players) {
                if(p.sex)
                    boysCount++;
                else
                    girlsCount++;
            }

            return Math.abs(boysCount - girlsCount);
        }
    }

    private int avgRating(ArrayList<Player> players){
        int rating = 0;
        for (Player p : players) {
            rating += p.rating;
        }

        return rating / TEAM_SIZE;
    }
}
