import ic.doc.ltsa.lts.*;
import javafx.util.Pair;
import java.util.*;

public class Algorithm {
    private ArrayList<String> S = new ArrayList<String>();
    private ArrayList<String> E = new ArrayList<String>();
    private ArrayList<String> SA = new ArrayList<String>();
    private ArrayList<String> sae = new ArrayList<String>();
    private ArrayList<String> se = new ArrayList<String>();
    private Hashtable T = new Hashtable();
    private ArrayList<String> sigma = new ArrayList<String>();
    private ArrayList<List<String>> traceVisited = new ArrayList<List<String>>();

    private LTSFunction LTSfunc = new LTSFunction();

    public Algorithm()
    {
        this.S.add("NULL");
        //this.S.add("output");
        //this.S.add("send");
        this.E.add("NULL");
        //this.E.add("ack");
        System.out.println("Algorithm construct()");
    }

    public void initAndRun(){
        System.out.println("Algorithm init()");

        this.LTSfunc.openFile("source.lts", "SOURCE");
        this.LTSfunc.ModelChecking("ORDER");
        System.out.println(this.LTSfunc.getCurrent());

        ArrayList<String> input = new ArrayList<String>();
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<String> order = new ArrayList<String>();
        ArrayList<String> tau = new ArrayList<String>();
        for (int i = 0; i < this.LTSfunc.getCurrent().machines.size(); ++i){
            Object cs = this.LTSfunc.getCurrent().machines.get(i);
            CompactState cmpstate = (CompactState)cs;
            if (cmpstate.getName().equals("INPUT")){
                for (String s : cmpstate.getAlphabet()){
                    input.add(s);
                }
            }
            else if (cmpstate.getName().equals("OUTPUT")){
                for (String s : cmpstate.getAlphabet()){
                    output.add(s);
                }
            }
            else if (cmpstate.getName().equals("ORDER")){
                for (String s : cmpstate.getAlphabet()){
                    order.add(s);
                }
            }
        }
        System.out.println(input.toString() + "\\" + output.toString() + "\\" + order.toString());
        if (input.size() != 0 && output.size() != 0 && order.size() != 0) {
            tau.add("tau");
            this.sigma.addAll(input);
            this.sigma.addAll(order);
            this.sigma.retainAll(output);
            this.sigma.removeAll(tau);
        }
        System.out.println("Sigma:" + this.sigma.toString());
        this.run();
    }


    private void buildSSE(ArrayList<String> SSE)
    {
        ArrayList<String> SSIGMA = new ArrayList<String>();
        SSIGMA.addAll(this.S);
        for (String s : this.S){
            for (String sig : this.sigma){
                String tmp = s + "|" + sig;
                SSIGMA.add(tmp);
            }
        }
        for (int i = 0; i < this.E.size(); ++i){
            for (int j = 0; j < SSIGMA.size(); ++j){
                String tmp = SSIGMA.get(j) + "^" + E.get(i);
                SSE.add(tmp);
            }
        }
    }
    private void splitSSE(ArrayList<String> sp, String[] stmp, String symbol)
    {
        for (int j = 0; j < stmp.length; ++j) {
            if (!stmp[j].equals("NULL")){
                String[] e = stmp[j].split(symbol);
                if (e.length == 0){
                    sp.add(stmp[j]);
                }
                else{
                    for (String ee : e){
                        sp.add(ee);
                    }
                }
            }
        }
    }

    private String buildFSP(String SSE)
    {
        String[] stmp;
        ArrayList<String> sp = new ArrayList<String>();
        if (SSE.contains("|")){
            stmp = SSE.split("\\|");
            //System.out.println("stmp:" + Arrays.toString(stmp));
            for (int i = 0; i < stmp.length; ++i){
                if (!stmp[i].contains("^") && !stmp[i].equals("NULL")){
                    String[] s = stmp[i].split("#");
                    if (s.length == 0){
                        sp.add(stmp[i]);
                    }
                    else{
                        for (String ss : s){
                            sp.add(ss);
                        }
                    }
                }
            }
            for (int i = 0; i < stmp.length; ++i){
                if (stmp[i].contains("^")){
                    String[] sstmp = stmp[i].split("\\^");
                    //System.out.println("sstmp:" + Arrays.toString(sstmp));
                    splitSSE(sp, sstmp, "&");
                }
            }
        }
        else{
            stmp = SSE.split("\\^");
            //System.out.println("stmp:" + Arrays.toString(stmp));
            splitSSE(sp, stmp, "&");
        }
        System.out.println("sse:" + SSE + "---> sp:" + sp.toString());
        String sentence = "";
        String newFSP = "";
        if (sp.isEmpty()){
            newFSP = "INPUT = (input->send->ack->INPUT).\n";
        }
        else{
            sentence = "A = (";
            for (int i = 0; i < sp.size(); ++i){
                sentence += sp.get(i) + " -> ";
            }
            sentence += "A).";
            newFSP += "INPUT = (input->send->ack->INPUT).\n"+
                      sentence;
        }
//        System.out.println(tmp);
        return newFSP;
    }

    private boolean buildTraceAndChecking()
    {
        this.LTSfunc.initGraph();
        this.LTSfunc.findTrace(0, 0);
        Map<Integer, List<List<Integer>>> pathes = this.LTSfunc.getPathes();
        Map<Pair<Integer, Integer>, Integer> events = this.LTSfunc.getEvents();
        System.out.println("pathes: " + pathes.toString());

        String[] alpha = this.LTSfunc.getCurrent().composition.getAlphabet();
        ArrayList<List<String>> allTrace = new ArrayList<List<String>>();
        allTrace = this.LTSfunc.getAllTrace(0, 0);
        System.out.println("allTrace: " + allTrace.toString());

        ArrayList<String> P = new ArrayList<String>();
        P.add("input");
        P.add("output");
        String[] ca = this.LTSfunc.getCurrent().composition.getAlphabet();
        ArrayList<String> curAlpha = new ArrayList<String>();
        for (String s : ca){
            curAlpha.add(s);
        }
        P.retainAll(curAlpha);
        boolean ret = true;
        for (int i = 0; i < allTrace.size(); ++i){
            ArrayList<String> curTrace = new ArrayList<String>();
            curTrace = (ArrayList<String>)allTrace.get(i);
            curTrace.retainAll(P);
            System.out.println("curTrace after project: " + curTrace);
            String strTrace = "";
            String strP = "";
            for (String s : curTrace){
                strTrace += s;
            }
            for (String p : P){
                strP += p;
            }
            if (P.size() >= curTrace.size()){
                int pos = strP.indexOf(strTrace);
                if (pos != 0){
                    ret = false;
                    break;
                }
            }
            else{
                int pos = strTrace.indexOf(strP);
                if (pos != 0){
                    ret = false;
                    break;
                }
            }
        }
        System.out.println("Check result: " + ret);
        return ret;
    }

    private void membershipQuery()
    {
        System.out.println("\n\n================ MEMBERSHIP QUERY ================\n");
        ArrayList<Vector<String>> sigmaKleene = new ArrayList<Vector<String>>();
        ArrayList<String> SSE = new ArrayList<String>();
        this.buildSSE(SSE);
        int i = 1;
        for (String sse : SSE){  //理解为SSE是sigmaKleene的真子集
            if (!this.T.containsKey(sse)){
                System.out.println("\n========== update T ==========\n");
                String newfsp = buildFSP(sse);
                this.LTSfunc.ModelChecking("ASIGMA " + i++, newfsp);
                boolean ret = buildTraceAndChecking();
                this.T.put(sse, ret);
            }
        }

        System.out.println("\n========== generated T ==========\n");
        System.out.println("S :" + this.S);
        System.out.println("E :" + this.E);
        System.out.println("SSE :" + SSE);
        System.out.println("T(membershipQuery):" + this.T.toString());
    }

    private boolean isClosed(ArrayList<String> S, ArrayList<String> E, ArrayList<String> Sigma)
    {
        this.SA.clear();
        this.sae.clear();
        this.se.clear();
        int count_S = 0;
        boolean ret = true;
        ArrayList<String> errorSA = new ArrayList<String>();
        for (String ls : S){
            boolean found = false;
            int count_Sigma = 0;
            for (String a : Sigma){
                int count_e = 0;
                for (String e : E){
                    String ll = ls + "|" + a + "^" + e;
                    this.sae.add(ll);
                    for (String rs : S){
                        String rr = rs + "^" + e;
                        if (!this.se.contains(rr)){
                            this.se.add(rr);
                        }
                        if (this.T.get(ll) == this.T.get(rr)){
                            count_e++;
                        }
                    }
                }
                if (count_e == this.E.size()){
                    count_Sigma++;
                }
                else{
                    System.out.println("Cause of Failure");
                    if (ls.equals("NULL")){
                        errorSA.add(a);
                    }
                    else{
                        errorSA.add(ls + "#" + a);
                    }
                }
            }
            if (count_Sigma == Sigma.size()){
                found = true;
            }
            if (!found){
                ret = false;
            }
        }
        System.out.println("sae :" + this.sae.toString());
        System.out.println("se :" + this.se.toString());
        System.out.println("ALL SA: " + errorSA.toString());
        for(String s : errorSA){
            if (!this.S.contains(s)){
                this.S.add(s);
                System.out.println("add SSSSS: " + s);
                break;
            }
        }
        return ret;
//        for (String s : S){
//            for (String e : E){
//                String rr = s + "^" + e;
//                this.se.add(rr);
//            }
//        }
//        System.out.println("sae :" + this.sae.toString());
//        System.out.println("se :" + this.se.toString());
//        ArrayList<String> allSA = new ArrayList<String>();
//        int count_true = 0;
//        for (String l : this.sae){
//            boolean found = false;
//            int num_e = 0;
//            for (String r : this.se){
//                String[] le = l.split("\\^");
//                String[] re = r.split("\\^");
//
//                String lee = le[le.length - 1];
//                String ree = re[re.length - 1];
//                if (this.T.get(l) == this.T.get(r) && lee.equals(ree)){
//                    num_e++;
//                    if (num_e == this.E.size()) {
//                        count_true++;
//                        found = true;
//                        break;
//                    }
//                }
//            }
//            if (num_e != this.E.size()){
//                System.out.println("Cause of Failure: " + l);
//            }
//            if (!found){
//                String []ll = l.split("\\^");
//                String []sa = ll[0].split("\\|");
//                ArrayList<String> SA = new ArrayList<String>();
//                for (int i = 0; i < sa.length; ++i){
//                    if (!sa[i].equals("NULL")){
//                        SA.add(sa[i]);
//                    }
//                }
//                String s = "";
//                for (int j = 0; j < SA.size(); ++j){
//                    s += SA.get(j);
//                    if (j != SA.size() - 1){
//                        s += "#";
//                    }
//                }
//                if (!allSA.contains(s) && !s.equals("")){
//                    allSA.add(s);
//                }
//            }
//        }
//        System.out.println("ALL SASA: " + allSA.toString());
//        for(String s : allSA){
//            if (!this.S.contains(s)){
//                this.S.add(s);
//                System.out.println("add SSSSS: " + s);
//                break;
//            }
//        }
//        if (count_true == this.sae.size()){
//            return true;
//        }
//        else{
//            return false;
//        }
    }



    public String constructDFA()
    {
        System.out.println("\n\n================ CONSTRUCT DFA ================\n");
        ArrayList<String> F = new ArrayList<String>(); //终止状态

        Hashtable sTrue = new Hashtable();
        int num_sTrue = 0;
        for (String s : this.S){
            if (this.T.get(s + "^NULL").equals(true)){
                F.add(s);
                sTrue.put(s, num_sTrue);
                num_sTrue++;
            }
        }
        System.out.println("==== F: " + F.toString());

        String sentence = "";
        for (int i = 0; i < F.size(); ++i){  //遍历起始状态
            ArrayList<String> path = new ArrayList<String>();
            for (String a : this.sigma){
                for (int j = 0; j < F.size(); ++j){  //遍历结束状态
                    boolean ret = true;
                    for (String e : this.E){
                        String str_sae = F.get(i) + "|" + a + "^" + e;
                        String str_se = F.get(j) + "^" + e;
                        if (this.T.get(str_sae) != this.T.get(str_se)){
                            ret = false;
                            break;
                        }
                    }
                    if (ret){
                        String end = sTrue.get(F.get(j)).toString();
                        path.add(a + "->C" + end);
                    }
                }
            }
            if (!path.isEmpty()){
                String start = sTrue.get(F.get(i)).toString();
                sentence += "C" + start + " = ( ";
                for (int j = 0; j < path.size(); ++j){
                    sentence += path.get(j);
                    if (j != path.size() - 1){
                        sentence += " | ";
                    }
                    else{
                        sentence += " ),\n";
                    }
                }
            }
            if (i == F.size() - 1){
                int pos = sentence.lastIndexOf(",");
                StringBuilder tmp = new StringBuilder(sentence);
                tmp.setCharAt(pos, '.');
                sentence = tmp.toString();
            }
        }
        System.out.println("sentence: \n" + sentence);
        return sentence;
    }

    private void findAdde(ArrayList<String> E, int traceId)
    {
        System.out.println("\n========== findAdd e 2 E ==========\n");
        this.LTSfunc.initGraph();
        this.LTSfunc.findTrace(0, -1);
        ArrayList<List<String>> allErrorTrace = new ArrayList<List<String>>();
        allErrorTrace = this.LTSfunc.getAllTrace(0, -1);
        System.out.println("AllErrorTrace: " + allErrorTrace);

        for (int n = allErrorTrace.size() - 1; n >= 0; --n){
            ArrayList<String> curErrorTrace = new ArrayList<String>();
            curErrorTrace = (ArrayList<String>)allErrorTrace.get(n);
            System.out.println("\n==========  curErrorTrace: " + curErrorTrace + "\n");

            ArrayList<String> projection = new ArrayList<String>();
            HashSet<String> eset = new HashSet<String>();
            projection.addAll(curErrorTrace);
            projection.retainAll(this.sigma);
            System.out.println("projection: " + projection.toString());
            boolean diffSE = false;
            for (int i = 0; i < projection.size(); ++i){
                String s = "";
                for (int j = 0; j <= i; ++j){
                    s += projection.get(j);
                    if (j < i){
                        s += "#";
                    }
                }
                String e = "";
                for (int j = i + 1; j < projection.size(); ++j){
                    e += projection.get(j);
                    if (j != projection.size() - 1){
                        e += "&";
                    }
                }
                if (e.equals("")){
                    continue;
                }
                String ll = s + "^" + e;
                boolean lbool = false;
                if (!this.T.containsKey(ll)){
                    System.out.println("\n====== ModelChecking SE:  " + ll);
                    String fsp = buildFSP(ll);
                    this.LTSfunc.ModelChecking("SE", fsp);
                    lbool = buildTraceAndChecking();
                }
                else{
                    lbool = (boolean)this.T.get(ll);
                }
                for (int j = 0; j < this.S.size(); ++j){
                    String rr = this.S.get(j) + "^" + e;
                    boolean rbool = false;
                    if (!this.T.containsKey(rr)){
                        System.out.println("\n====== ModelChecking S'E:  " + rr);
                        String fsp = buildFSP(rr);
                        this.LTSfunc.ModelChecking("S'E", fsp);
                        rbool = buildTraceAndChecking();
                    }
                    else{
                        rbool = (boolean)this.T.get(rr);
                    }
                    if (lbool != rbool){
                        eset.add(e);
                        diffSE = true;
                        break;
                    }
                }
                if (diffSE){
                    break;
                }
            }
            ArrayList<String> addE = new ArrayList<String>(eset);
            System.out.println("====== found EEEEEE: " + addE.toString());
            boolean foundE = false;
            for (int i = 0; i < addE.size(); ++i){
                if (!addE.get(i).contains("NULL")){
                    if (!E.contains(addE.get(i))){
                        E.add(addE.get(i));
                        foundE = true;
                        System.out.println("add EEEEE: " + addE.get(i));
                    }
                }
                else{
                    String[] rr = addE.get(i).split("\\^");
                    String e = "";
                    for (int j = 0; j < rr.length; ++j){
                        if (!rr[j].equals("NULL")){
                            e += rr[j];
                        }
                        if (j != rr.length - 1){
                            e += "&";
                        }
                    }
                    if (!E.contains(e)){
                        E.add(e);
                        foundE = true;
                        System.out.println("add EEEEE: " + e);
                    }
                }
            }
            if (foundE){
                break;
            }
        }
    }



    public String run()
    {
        while (true){
            membershipQuery();
            while (!isClosed(this.S, this.E, this.sigma)) {
                //function add sa to S in function inClosed
                System.out.println("\n===== Table T is not Closed =====");
                membershipQuery();

            }
            System.out.println("\n\n===== Table T is Closed =====");
            String sentence = constructDFA();
            String step1 = "property ORDER = (input->output->ORDER).\n" +
                         "INPUT = (input->send->ack->INPUT).\n" +
                         sentence;
            this.LTSfunc.ModelChecking("STEP1", step1);

            if (this.LTSfunc.getCurrent().getErrorTrace() == null){
                String step2 = "property " + sentence;
                step2 += "OUTPUT = (send->output->ack->OUTPUT).\n";
                this.LTSfunc.ModelChecking("STEP2", step2);

                if (this.LTSfunc.getCurrent().getErrorTrace() == null){
                    return step2;
                }
                else {
                    findAdde(this.E, 1);
                }
            }
            else {
                findAdde(this.E, 1);
            }
            System.out.println("================ LOOP ================");
        }
    }

}
