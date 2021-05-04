import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SequenceGenerator {
    public ArrayList<ArrayList<String>> permute(String[] num) {
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        result.add(new ArrayList<String>());

        for (int i = 0; i < num.length; i++) {
            ArrayList<ArrayList<String>> current = new ArrayList<ArrayList<String>>();

            for (ArrayList<String> l : result) {
                for (int j = 0; j < l.size()+1; j++) {
                    l.add(j, num[i]);
                    ArrayList<String> temp = new ArrayList<String>(l);
                    current.add(temp);
                    l.remove(j);
                }
            }
            result = new ArrayList<ArrayList<String>>(current);
        }

        return result;
    }

    public String constructedLTS (ArrayList<String> actions){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            result.append(actonTranslated(actions.get(i), i, actions.size())).append("\n");
        }
        return result.toString();
    }

    public static String actonTranslated (String act, int i, int len){
        String[] subs = new String[2];
        String result = "";
        if (act.equals("login") || act.equals("select") || act.equals("logout")) {
            subs[0] = act;
        }
        if (act.equals("charge_back")){
            subs[0] = "charge";
            subs[1] = "back";
        }
        if (act.equals("pay_pw_back")){
            subs[0] = "pay_pw";
            subs[1] = "back";
        }
        if (act.equals("decide_votes_back")){
            subs[0] = "decide_votes";
            subs[1] = "back";
        }
        if (act.equals("confirm_back")){
            subs[0] = "confirm";
            subs[1] = "back";
        }

        if (i == 0){
            result = "BM_1=(" + subs[0] + "->P1),";
        }else if (i == len - 1){
            if (subs[1] ==  null){
                result = "P" + i + "=(" + subs[0] + "->END).";
            }else{
                result = "P" + i + "=(" + subs[0] + "->END|" + subs[1] + "->P" + (i-1) + ").";
            }
        }else if (i == 1){
                result = "P" + i + "=(" + subs[0] + "->P" + (i + 1) + "),";
        }else{
            if (subs[1] == null){
                result = "P" + i + "=(" + subs[0] + "->P" + (i + 1) + "),";
            }else {
                result = "P" + i + "=(" + subs[0] + "->P" + (i + 1) + "|" + subs[1] + "->P" + (i - 1) + "),";
            }
        }

        return result;
    }

//    public static void main(String[] args) {
//        int[] arr = {1,2,3,4,5,6,7};
//        String[] actions = {"login", "select", "charge_back", "pay_pw_back",
//                            "decide_votes_back", "confirm_back", "logout"};
//
//        System.out.println(permute(actions));
//        System.out.println(permute(actions).size());
//        constructLTS(permute(actions).get(0));
//
//
//    }
}
