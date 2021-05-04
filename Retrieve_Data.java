import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Retrieve_Data {
    public static ArrayList<ArrayList<Integer>> retrieve_data(){
        ArrayList<ArrayList<Integer>> data = new ArrayList<>();
        String[] actions = {"login", "select", "charge_back", "pay_pw_back",
                "decide_votes_back", "confirm_back", "logout"};

        SequenceGenerator sg = new SequenceGenerator();
        ArrayList<ArrayList<String>> sequences = new ArrayList<ArrayList<String>>();
        sequences = sg.permute(actions);

        System.out.println("sequences: "+sequences);

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
        HashMap<String, Integer> map_ActionToInt = new HashMap<>();

        //        for (List key : map_ActionToInt.keySet()){
//            System.out.println(key + " " +map_ActionToInt.get(key));
//        }

        for (int i = 0; i < Actions.size(); i++) {
            map_ActionToInt.put(Actions.get(i).get(0), i + 1);
        }
//        sequences.size();
        for (int i = 0; i < sequences.size(); i++) {
            Algorithm_ alg = new Algorithm_();
            alg.initAndRun(sg.constructedLTS(sequences.get(i)));
            ArrayList<Integer> list = new ArrayList<>();
            for (int j = 0; j < alg.getLtK().allActions.size(); j++) {
                list.add(map_ActionToInt.get(alg.getLtK().allActions.get(j).get(0))); // add the sequence of actions in int
            }
            list.addAll(alg.getChecking_Result());
            data.add(list);
            System.out.println("Completion for " + i);
            //System.out.println("Hello, Data!" + data);
        }
//        System.out.println(sequences.get(35));
//        System.out.println(sg.constructedLTS(sequences.get(35)));

        return data;

    }
}
