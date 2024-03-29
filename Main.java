import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        System.out.println("---- Start ----");
        //alg.initAndRun();

        //---------------------------------------------------------------------------
        String[] actions = {"login", "select", "charge_back", "pay_pw_back",
                            "decide_votes_back", "confirm_back", "logout"};

        String original_BM = "BM_1=(login->P1),\n" +
                "P1=(select->P2),\n" +
                "P2=(charge->P3|back->P1),\n" +
                "P3=(pay_pw->P4|back->P2),\n" +
                "P4=(decide_votes->P5|back->P3),\n" +
                "P5=(confirm->P6|back->P4),\n" +
                "P6=(logout->END).\n";
        
        LTS_to_KS ltk = new LTS_to_KS();
        List<List<String>> Actions = new ArrayList<List<String>>();
        ltk.RegexMatches(original_BM);
        Actions = ltk.allActions;
        HashMap<List<String>, Integer> map_ActionToInt = new HashMap<>();
        for (int i = 0; i < Actions.size(); i++) {
            map_ActionToInt.put(Actions.get(i), i + 1);
        }

        SequenceGenerator sg = new SequenceGenerator();
        ArrayList<ArrayList<String>> sequences = new ArrayList<ArrayList<String>>();
        sequences = sg.permute(actions);
        for (int i = 0; i < sequences.size(); i++) {
            Algorithm_ alg = new Algorithm_();
            alg.initAndRun(sg.constructedLTS(sequences.get(i)));
            System.out.println("Completion for " + i);
        }
//        System.out.println(sequences.get(35));
//        System.out.println(sg.constructedLTS(sequences.get(35)));

        //---------------------------------------------------------------------------



        System.out.println("---- End ----");

    }
}
