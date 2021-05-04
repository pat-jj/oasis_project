import java.io.IOException;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("---- Start ----");
        //alg.initAndRun();
        //---------------------------------------------------------------------------
        ArrayList<ArrayList<Integer>> data = Retrieve_Data.retrieve_data();
        //---------------------------------------------------------------------------
        String csvFilePath = "/Users/rexjiang/desktop/";
        String[] csvHeaders = {"login", "select", "charge_back", "pay_pw_back",
                "decide_votes_back", "confirm_back", "logout", "NoVoteFlipping",
                "NoPersonalInfoLeak", "NoVotesNumChange", "ChargeAfterLogin",
                "NoConfirmAfterSelectUntilVote", "PayAfterCharge"};

        CsvWriter.writeCSV(data, csvHeaders ,csvFilePath);


        System.out.println("---- End ----");

    }
}
