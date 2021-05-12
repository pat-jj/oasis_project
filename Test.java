import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        String trial = "BM_1=(login->P1),\n" +
                "P1=(charge->P2|back->BM_1),\n" +
                "P2=(select->P3),\n" +
                "P3=(confirm->P4),\n" +
                "P4=(pay_pw->P5|back->P3),\n" +
                "P5=(decide_votes->P6|back->P4),\n" +
                "P6=(logout->END).\n";

        Algorithm_ alg = new Algorithm_();
        alg.initAndRun(trial);
        ArrayList<Integer> list = new ArrayList<>();
        list.addAll(alg.getChecking_Result());
        System.out.println(list);

    }
}


