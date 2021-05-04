import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import ic.doc.ltsa.common.iface.LTSInput;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.EventManager;
import ic.doc.ltsa.lts.*;
import ic.doc.ltsa.lts.ltl.AssertDefinition;
import javafx.util.Pair;


public class LTSFunction {
    private String text;
    private CompositeState current = null;
    public CompactState[] sm;
    private EventManager eman = new EventManager();
    private Map<String, String[]> m_alphabet = new HashMap<String, String[]>();
    private myLTSIO myinput;
    public myLTSIO myoutput;
    private String transition_result;
    private Hashtable parsetable = null;
    public String violated_property;
    public AssertDefinition assertion; // assert
    public int violate_flag = 0;
    public Object fileResource;
    StringBuilder str_;

    Map<Integer, List<Integer>> graph = new HashMap<Integer, List<Integer>>();
    Map<Pair<Integer, Integer>, Integer> events = new HashMap<Pair<Integer, Integer>,Integer>();
    Map<Integer, List<List<Integer>>> pathes = new HashMap<Integer, List<List<Integer>>>();

    public boolean openFile(String filename, String key, String req, String check, String BM)
    {
        if (filename == null) {
            System.out.println("filename is null");
            return false;
        }
        Object resource = null;
        resource = this.getClass().getResourceAsStream(filename);
        fileResource = resource;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)resource));
            try {
                str_ = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    str_.append(line + '\n');
                }
                str_.append(BM);
                str_.append("||Env = (Room||U||MU||{u, mu}::BM_1).").append("\n");
                str_.append(req);
                str_.append(check);
                System.out.println("filename:" + filename + "\n" + str_);
                this.text = str_.toString();

            } catch (Exception e1) {
                System.out.println("error reading file:" + e1);
            }
        } catch (Exception e2) {
            System.out.println("error creating inputstream:" + e2);
        }
        return true;
    }

    private boolean doParse(String FSP)
    {
        boolean parsed = false;
        this.myinput = new myLTSIO();
        this.myoutput = new myLTSIO();
        this.myinput.fPos = -1;
        this.myinput.fSrc = FSP;
        LTSCompiler ltscmpr = new LTSCompiler(this.myinput, this.myoutput, "");

        Hashtable cs = new Hashtable();
        Hashtable ps = new Hashtable();
        try{
            ltscmpr.parse(cs, ps);
            this.parsetable = cs;
            parsed = true;
            //this.myoutput.print();
        }catch(Exception var7){
            System.out.println(var7);
        }

        return parsed;
    }

    private boolean doCompile(String FSP, String name)
    {
        boolean compiled = false;
        this.myinput = new myLTSIO();
        this.myoutput = new myLTSIO();
        this.assertion = new AssertDefinition();
        this.myinput.fSrc = FSP + '\n';
        this.myinput.fPos = -1;
        CompositeState compstate = null;
        LTSCompiler myltscompiler = new LTSCompiler(this.myinput, this.myoutput, "");
        try{
            this.myinput.fPos = -1;
            System.out.println("Compiling: " + name);
            compstate = (CompositeState)myltscompiler.compile(name);
            this.myinput.fPos = -1;
            this.current = (CompositeState)compstate;

            compiled = true;
            //this.myoutput.print();
        } catch (Exception var1){
            System.out.println(var1);
        }
        return compiled;
    }

    private boolean doCompose(CompositeState cs)
    {
        this.myoutput = new myLTSIO();
        boolean ret = false;
        try {
            cs.compose(this.myoutput);
            if (this.myoutput.result.startsWith("null")){
                this.myoutput.result = this.myoutput.result.substring(4);
            }
            System.out.println("222");
            this.myoutput.print();
            System.out.println("333");
//            System.out.println(this.myoutput.result);
//            System.out.println("444");
            ret = true;
        }catch(Exception var1){
            System.out.println(var1);
            return ret;
        }
        return ret;
    }

    private void reachable()
    {
        this.myoutput = new myLTSIO();
        boolean ret = false;
        if (this.current != null && this.current.machines.size() > 0){
            Analyser analy = new Analyser(this.current, this.myoutput, (EventManager)null);
            SuperTrace trace = new SuperTrace(analy, this.myoutput);
            this.current.setErrorTrace(trace.getErrorTrace());
            this.violated_property = trace.mach.getViolatedProperty();
        }
        this.myoutput.print();
//        System.out.println("Violated Property hahahaha: " + violated_property);

    }

    public void newMachine(CompositeState cs)
    {
        int i = cs != null && cs.composition != null ? 1 : 0;
        if (cs != null && cs.machines != null && cs.machines.size() > 0){
            this.sm = new CompactState[cs.machines.size() + i];
            Enumeration en = cs.machines.elements();

            for (int j = 0; en.hasMoreElements(); ++j){
                this.sm[j] = (CompactState)en.nextElement();
            }
            int Nmath = this.sm.length;
            if (i == 1){
                this.sm[Nmath - 1] = cs.composition;
            }
            else {
                Nmath = 0;
            }
        }
    }

    public void printTransitions(int id)
    {
        violate_flag = 0;
        this.myoutput = new myLTSIO();
        if (id < this.sm.length){
            PrintTransitions pt = new PrintTransitions(this.sm[id]);
            pt.print(this.myoutput, 400);
//            this.myoutput.print();
            System.out.println(myoutput.result);
            if(id == this.sm.length - 1) {
                this.transition_result = myoutput.result;
                if(!this.transition_result.contains("ERROR")){
                    System.out.println("--------Good--------");
                }else{
                    System.out.println("------Violated------");
                    System.out.println("Violated Property: " + violated_property);
                    violate_flag = 1;
                }

            }
        }
    }

    public void ModelChecking(String name){
        String fsp = this.text;
        ModelChecking(name, fsp);
    }

    public void ModelChecking(String name, String fsp)
    {
        this.text = fsp;
        System.out.println("111"+name + '\n' + fsp + '\n');
        if (!this.doCompile(fsp, name)){
            System.out.println("Compile Error");
        }
        if (!this.doCompose(this.current)){
            System.out.println("Compose Error");
        }
        this.reachable();
    }


    public void initGraph(){
        this.graph.clear();
        this.events.clear();
        this.pathes.clear();
        CompositeState cs = this.getCurrent();
        EventState[] states = (EventState[])cs.composition.getStates();
        for(int i = 0; i < states.length; i++){
            EventState es = states[i];
            graph.put(i, new ArrayList<Integer>());

            while(es != null){
                graph.get(i).add(es.getNext());
                events.put(new Pair<Integer, Integer>(i, es.getNext()), es.getEvent());
                es = (EventState)es.getList();
            }
        }
    }
    public void findTrace(int start, int end){
        List<Integer> subPath = new ArrayList<Integer>();
        subPath.add(start);
        FLP(this.graph, this.pathes, subPath, start, end);
    }

    private void FLP(Map<Integer, List<Integer>> graph, Map<Integer, List<List<Integer>>> path,
                     List<Integer> prePath, int start, int end)
    {
        List<Integer> next = graph.get(start);
        for (Integer i : next){
            if (prePath.contains(i) && i != end){
                continue;
            }
            prePath.add(i);
            if (i == end || graph.get(i).isEmpty()){
                if (!path.containsKey(prePath.size() - 1)){
                    path.put(prePath.size() - 1, new ArrayList<List<Integer>>());
                }
                path.get(prePath.size() - 1).add(new ArrayList<Integer>(prePath));
            }
            else{
                FLP(graph, path, prePath, i, end);
            }
            prePath.remove(prePath.size() - 1);
        }
    }

    public ArrayList<List<String>> getAllTrace(int start, int end){
        ArrayList<List<String>> allTrace = new ArrayList<List<String>>();
        String[] alpha = this.getCurrent().composition.getAlphabet();
        for (List<List<Integer>> values : pathes.values()){
            for (int i = 0; i < values.size(); ++i){
                int len = values.get(i).size();
                ArrayList<String> trace = new ArrayList<String>();
                if (len == 1){
                    int event = events.get(new Pair<Integer, Integer>(start, end));
                    trace.add(alpha[event]);
                }
                else {
                    for(int j = 0; j < len; ++j){
                        if ((j + 1) < len){
                            int s = values.get(i).get(j);
                            int e = values.get(i).get(j + 1);
                            int event = events.get(new Pair<Integer, Integer>(s, e));
                            trace.add(alpha[event]);
                        }
                    }
                }
                allTrace.add(trace);
            }
        }
        return allTrace;
    }

    public CompositeState getCurrent(){
        return this.current;
    }

    public String getText(){
        return this.text;
    }

    public Map<Integer, List<Integer>> getGraph(){
        return this.graph;
    }
    public Map<Pair<Integer, Integer>, Integer> getEvents(){
        return this.events;
    }
    public Map<Integer, List<List<Integer>>> getPathes(){
        return this.pathes;
    }

    static class myLTSIO implements LTSOutput, LTSInput {
        String fSrc = "A = (test -> END). B = (test2 -> END). ||C = (A || B).";
        private int fPos;
        public String result = "";

        public myLTSIO() {
        }

        public void setFPos(int value) {
            this.fPos = value;
        }

        public char backChar() {
            --this.fPos;
            if (this.fPos < 0) {
                this.fPos = 0;
                return '\u0000';
            } else {
                return this.fSrc.charAt(this.fPos);
            }
        }

        public char nextChar() {
            ++this.fPos;
            if (this.fPos < this.fSrc.length()) {
                return this.fSrc.charAt(this.fPos);
            } else {
                --this.fPos;
                return '\u0000';
            }
        }

        public void out(String str)
        {
            this.result = this.result + str;
        }

        public void clearOutput() {
        }

        public void outln(String str) {
            this.result = this.result + str + "\n";
        }

        public int getMarker() {
            return this.fPos;
        }


        public void print() {
            System.out.print(this.result);
        }
    }
}
