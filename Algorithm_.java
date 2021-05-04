import ic.doc.ltsa.frontend.HPWindow;
import ic.doc.ltsa.lts.*;
import javafx.util.Pair;
import java.util.*;

public class Algorithm_ {
    int ReqNum = 6;
    private LTS_to_KS LtK = new LTS_to_KS();

    public LTS_to_KS getLtK() {
        return LtK;
    }

    private ArrayList<Integer> Checking_Result = new ArrayList<>();

    public ArrayList<Integer> getChecking_Result() {
        return Checking_Result;
    }

    public Algorithm_()
    {
        System.out.println("Algorithm construct()");
    }

    public void initAndRun(String BM) {
        System.out.println("Algorithm init()");

        String[] flag_v = new String[ReqNum];
        String[] vio_req = new String[ReqNum];
        String[][] TBL_1 = new String[2][ReqNum + 1];
//        HashMap<String, String> result = new HashMap<>();
        String REQ = "", CHECK = "";
        String Check = null;


        for(int i = 0; i < ReqNum; i++) {
            LTSFunction LTSfunc = new LTSFunction();
            if(i == 0){
                Check = "NoVoteFlipping";
                REQ = "\tNoVoteFlipping = Q0,\n" +
                        "\tQ0\t= (mu.select -> Q0\n" +
                        "\t\t  |u.select -> Q1),\n" +
                        "\tQ1\t= (mu.select -> ERROR\n" +
                        "\t\t  |u.select -> Q1).\n";
                CHECK = "||Check = (Env||NoVoteFlipping).";
            }

            if(i == 1){
                Check = "NoPersonalInfoLeak";
                REQ = "    NoPersonalInfoLeak = Q0,\n" +
                        "    Q0\t= (mu.charge -> Q0\n" +
                        "          |u.charge -> Q1),\n" +
                        "    Q1\t= (mu.charge -> ERROR\n" +
                        "          |u.charge -> Q1).\n";
                CHECK = "||Check = (Env||NoPersonalInfoLeak).";
            }
            if(i == 2) {
                Check = "NoVotesNumChange";
                REQ = "\tNoVotesNumChange = Q0,\n" +
                        "\tQ0\t= (mu.decide_votes -> Q0\n" +
                        "\t\t  |u.decide_votes -> Q1),\n" +
                        "\tQ1\t= (mu.decide_votes -> ERROR\n" +
                        "\t\t  |u.decide_votes -> Q1).\n";
                CHECK = "||Check = (Env||NoVotesNumChange).";
            }

            if(i == 3){
                Check = "ChargeAfterLogin";
                REQ = "\tChargeAfterLogin = Q0,\n" +
                        "\tQ0\t= ({mu, u}.charge -> ERROR\n" +
                        "\t\t  |{mu, u}.login -> Q1),\n" +
                        "\tQ1\t= ({mu, u}.login -> ERROR\n" +
                        "\t\t  |{mu, u}.charge -> Q0).\n";
                CHECK = "||Check = (Env||ChargeAfterLogin).";
            }
            if(i == 4){
                Check = "NoConfirmAfterSelectUntilVote";
                REQ = "\tNoConfirmAfterSelectUntilVote = Q0,\n" +
                        "\tQ0\t= ({confirm, decide_votes} -> Q0\n" +
                        "\t\t  |select -> Q1),\n" +
                        "\tQ1\t= (confirm -> ERROR\n" +
                        "\t\t  |decide_votes -> Q0\n" +
                        "\t\t  |select -> Q1).\n";
                CHECK = "||Check = (Env||{u, mu}::NoConfirmAfterSelectUntilVote).";

            }
            if(i == 5){
                Check = "PayAfterCharge";
                REQ = "\tPayAfterCharge = Q0,\n" +
                        "\tQ0\t= ({u, mu}.pay_pw -> ERROR\n" +
                        "\t\t  |{u, mu}.charge -> Q1),\n" +
                        "\tQ1\t= ({u, mu}.charge -> ERROR\n" +
                        "\t\t  |{u, mu}.pay_pw -> Q0).\n";
                CHECK = "||Check = (Env||PayAfterCharge).";

            }

            LTSfunc.openFile("moti_example.lts", "<MOTI>", REQ, CHECK, BM);
            LTSfunc.ModelChecking("Check");
            LtK.LTSfunc = LTSfunc;
//            System.out.println("Machine Behavior Model:\n"+LtK.getMachineBM());
            LtK.RegexMatches(LtK.getMachineBM());
            LtK.lts_to_ks();
//        this.LTSfunc.ModelChecking("ORDER2");
//        this.LTSfunc.assertion.compile(this.LTSfunc.myoutput, "assert NoPersonalInfoLeak = [](u.input_info -> [](!mu.input_info))");
            CompositeState cs1 = LTSfunc.getCurrent();
//        cs1.checkLTL(this.LTSfunc.myoutput, cs1);
//            System.out.println("Hello, CompactStates " + cs1);
            LTSfunc.newMachine(cs1);
            for (int id = 0; id < LTSfunc.sm.length; id++) {
                LTSfunc.printTransitions(id);
            }
            if(LTSfunc.violate_flag == 1) {
                flag_v[i] = "         Y         ";
                vio_req[i] = LTSfunc.violated_property + "   ";
                Checking_Result.add(1);
            }else{
                flag_v[i] = "         N         ";
                vio_req[i] = Check + "   ";
                Checking_Result.add(0);
            }

        }
        System.out.println("---------------------------------------------\n" +
                           "--------------------Result-------------------\n");
        TBL_1[1][0] = "Violated? (Y/N)"; TBL_1[0][0] = "Violated Req.  ";
        for(int i = 0; i < ReqNum; i++){
            TBL_1[1][i+1] = flag_v[i];
            TBL_1[0][i+1] = vio_req[i];
        }
        for(int i = 0; i < 2; i++){
            for(int j = 0; j < ReqNum + 1; j++){
                System.out.print(TBL_1[i][j]);
            }
            System.out.println();
        }
        System.out.println("---------------------------------------------");
    }
}
