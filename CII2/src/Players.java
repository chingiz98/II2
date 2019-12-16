import Questions.Question;
import jade.core.Agent;

import java.util.ArrayList;

class Players extends Agent {

    private ArrayList<Question> data;
    private ArrayList<Question> sendQ   = new ArrayList<>();
    private int                 minComp = 0, maxComp = 0;
    private int lastQFound = 0;
    private int questionCount = 0;

    @Override
    protected void setup() {
        super.setup();
    }
}
