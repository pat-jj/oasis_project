import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LTS_to_KS {
    public List<List<String>> allActions = new ArrayList<List<String>>();
    public static LTSFunction LTSfunc = new LTSFunction();

    public LTS_to_KS() {
    }

    public void lts_to_ks(){
        System.out.println(Arrays.toString(allActions.toArray()));
        int ltsLength;
        ltsLength = allActions.toArray().length;
        System.out.println("LTS length: " + ltsLength);
        int[] stateWidth = new int[ltsLength];
        for (int i = 0; i < ltsLength; i++){
            stateWidth[i] = allActions.get(i).toArray().length;
        }
        System.out.println(Arrays.toString(stateWidth));

//        KSnode head = new KSnode();
//        KSnode fresh = new KSnode();
//        int id_int = 0;
//        int bit_length = 5;
//        head.action = "START";
//        head.ID = ToBinary(id_int, bit_length);
//        id_int++;
//        head.next1.action = allActions.get(0).get(0);
//        head.next1.ID = ToBinary(id_int, bit_length);
//        head.prev1 = head;
//        head = head.next1;
//        int ltsIndexOfState = 0;
//        while(head != null){
//            // if not fresh state
//            if (head.action != null){
//                if (stateWidth[ltsIndexOfState + 1] == 1){
//                    ++id_int;
//                    head.next1.action = null;
//                    head.next1.ID = ToBinary(id_int, bit_length);
//                    head.prev1 = head;
//                    head = head.next1;
//                    ltsIndexOfState++;
//                }else if (stateWidth[ltsIndexOfState + 1] == 2){
//                    ++id_int;
//                    head.next1.action = null;
//                    head.next1.ID = ToBinary(id_int, bit_length);
//                    head.prev1 = head;
//                    head = head.next1;
//                }
//            }
//
//        }



    }

//    public static class KSnode{
//        String action = null;
//        String ID = "";
//        KSnode prev1, prev2, prev3;
//        KSnode next1, next2, next3;
//    }

    public static String ToBinary(int integer, int bit_length){
        StringBuilder binaryString = new StringBuilder();
        binaryString.append(Integer.toBinaryString(integer));
        for(int n=binaryString.length(); n<bit_length; n++) {
            binaryString.insert(0, "0");
        }
        return binaryString.toString();
    }

    public void RegexMatches(String machineBM){
        allActions = new ArrayList<List<String>>();
        String regex_0 = "=(.*?)\\)";
        //String regex_1 = "=(.*?).";
        String regex_2 = "\\((.*?)\\|";
        String regex_3 = "\\((.*?)->";
        String regex_4 = "\\|(.*?)->";

        String[] lineByline = new String[20];
        final Pattern pattern0 = Pattern.compile(regex_0, Pattern.DOTALL);
        final Matcher matcher0 = pattern0.matcher(machineBM);
        //final Pattern pattern1 = Pattern.compile(regex_1, Pattern.DOTALL);
        //final Matcher matcher1 = pattern1.matcher(machineBM);
        int count = 0;
        while (matcher0.find()) {
            count++;
            lineByline[count] = matcher0.group(1);
        }
        System.out.println(Arrays.toString(lineByline));

        final Pattern pattern2 = Pattern.compile(regex_2);
        final Pattern pattern3 = Pattern.compile(regex_3);
        final Pattern pattern4 = Pattern.compile(regex_4);
        try {
            for (int j = 1; j <= lineByline.length; j++) {
                final Matcher matcher2 = pattern2.matcher(lineByline[j]);
                final Matcher matcher3 = pattern3.matcher(lineByline[j]);
                final Matcher matcher4 = pattern4.matcher(lineByline[j]);
                List<String> actionForState = new ArrayList<String>();
                int cnt = 0;
                if (matcher3.find()) {
                    if(!matcher2.find()) {
                        System.out.println(matcher3.group(1));
                        actionForState.add(cnt, matcher3.group(1));
                    }else if (matcher4.find()){
                        System.out.print(matcher3.group(1)+" ");
                        actionForState.add(cnt, matcher3.group(1));
                        //while(matcher4.find()) {
                            cnt++;
                            System.out.print(matcher4.group(1));
                            actionForState.add(cnt, matcher4.group(1));
                        //}
                        System.out.println();
                    }
                }else{
                    break;
                }
                allActions.add(j-1, actionForState);
            }
        }catch (Exception e1) {
            System.out.println("Cannot extract actions:" + e1);
        }
        System.out.println("Sorted Actions: "+allActions + "\n");

    }

    public static String getMachineBM(){
        String res = LTSfunc.getText();
        //System.out.println("Full text"+res);
        String regex_ = "BM_1(.*?)END(.*?)\\).";
        try {
            final Pattern pattern = Pattern.compile(regex_, Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(res);
            if (matcher.find()) {
                return matcher.group();
            }else{
                System.out.println("No matched content!");
            }
        }catch (Exception e1) {
            System.out.println("Cannot find matching content:" + e1);
        }
        return null;
    }
}
