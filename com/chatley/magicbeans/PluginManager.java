package com.chatley.magicbeans;

import java.util.*;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;

public class PluginManager {

    private static boolean GENERATE_DARWIN = true;
    private static String s_host = "127.0.0.1";
    private static PluginManager s_pm = new PluginManager();
    private static Set s_exclusions = new HashSet(Arrays.asList(new String[] { "ListAdder", "Framework", "FSPDefinition",
            ConfigurationManager.get("frameworkjar") }));

    private List o_bindings;
    private List o_components;
    private List o_adders;
    private List o_strategies;

    private ObserverList o_observers;
    private Map o_pegs_instantiated;
    private Map o_fsp;

    private ByteCodeChecker o_byteCodeChecker;

    private static Logger o_log;

    private PluginManager() {

        o_bindings = new ArrayList();
        o_components = new ArrayList();
        o_adders = new ArrayList();
        o_strategies = new ArrayList();
        o_observers = new ObserverList();
        o_pegs_instantiated = new HashMap();
        o_fsp = new HashMap();
        o_log = new SystemOutLogger(); // NullLogger();
    }

    public static PluginManager getInstance() {

        return s_pm;
    }

    public List get(Object p_obj) {

        List x_results = new ArrayList();
        Component x_from = getComponent(p_obj);

        if (x_from == null) {
            return x_results;
        } //return the empty list

        for (Iterator i = o_bindings.iterator(); i.hasNext();) {

            Binding x_bind = (Binding) i.next();
            if (x_bind.getFrom() == x_from) {
                x_results.add(x_bind.getTo());
            }
        }

        return x_results;
    }

    private Component getComponent(Object p_obj) {

        try {

            Component x_comp = (Component) p_obj.getClass().getClassLoader();
            return x_comp;

        } catch (ClassCastException p_cce) {
            o_log.msg("Class " + p_obj.getClass().getName() + " was not loaded through component mechanism.");
            return null;
        }
    }

    public List get(Object p_obj, String p_interface) {

        List x_results = new ArrayList();

        return x_results;
    }

    public void addObserver(Object p_observer) {

        o_log.msg("PM --- adding observer" + p_observer.getClass().getName());

        o_observers.add(p_observer);
    }

    public void addBackDatedObserver(Object p_observer) {

        addObserver(p_observer);

        for (Iterator i = o_bindings.iterator(); i.hasNext();) {

            Binding x_bind = (Binding) i.next();

            if (x_bind.getFrom() == p_observer.getClass().getClassLoader()) {

                MultiMethod.dispatch(p_observer, "pluginAdded", x_bind.getProxy());
            }
        }
    }

    public void bind() {

        Map x_cand_map = new HashMap();

        for (Iterator i = o_components.iterator(); i.hasNext();) {

            Component x_icomp = (Component) i.next();

            for (Iterator j = o_components.iterator(); j.hasNext();) {

                Component x_jcomp = (Component) j.next();

                if (x_icomp == x_jcomp) {
                    continue;
                } // don't bind to same component

                for (Iterator k = x_icomp.getPegs().iterator(); k.hasNext();) {

                    Peg x_peg = (Peg) k.next();

                    for (Iterator l = x_jcomp.getHoles().iterator(); l.hasNext();) {

                        Hole x_hole = (Hole) l.next();

                        /*
                         * this line enforces the constraint that each provision
                         * can only be bound to a single requirement. To allow a
                         * provision to satisfy multiple requirements (as Darwin
                         * allows), comment out this line.
                         */

                        if (ConfigurationManager.get("bindpolicy").equals("1-1")) {

                            if (x_peg.isBound(x_hole.getInterface())) {
                                continue;
                            }
                        }

                        //....

                        if (!x_hole.available()) {
                            continue;
                        }

                        if (sameShape(x_peg, x_hole)) {

                            makeBinding(new Binding(x_jcomp, x_icomp, x_hole, x_peg));

                        }
                    }
                }
            }
        }
    }

    private void makeBinding(Binding p_bind) {

        p_bind.getHole().decr();
        p_bind.getHole().setBound(true);
        p_bind.getPeg().setBound(p_bind.getHole().getInterface(), true);
        p_bind.generateProxy(o_pegs_instantiated);

        o_bindings.add(p_bind);

        o_log.msg("PM -- NEW BINDING : " + p_bind);

        notifyObservers(p_bind);
    }

    public synchronized void add(Component p_comp) {

        o_components.add(p_comp);
        bind();
        if (ConfigurationManager.get("generatedarwin").equals("true"))
            new DarwinGenerator().generateDarwin();
    }

    public void add2(Component p_comp) {

        //
        //		Map x_cand_map = new HashMap();
        //		List x_pegs = p_comp.getPegs();
        //		Peg x_peg = null;
        //		Component x_comp = null;
        //
        //		o_components.add(p_comp);
        //
        //		for (Iterator i = x_pegs.iterator(); i.hasNext();) {
        //
        //			x_peg = (Peg) i.next();
        //
        //			//System.out.println("found peg : " + x_peg );
        //
        //			for (Iterator j = o_components.iterator(); j.hasNext();) {
        //
        //				x_comp = (Component) j.next();
        //
        //				for (Iterator k = x_comp.getHoles().iterator(); k.hasNext();) {
        //
        //					Hole x_hole = (Hole) k.next();
        //
        //					//all holes filled?
        //					if (!x_hole.available()) {
        //						continue;
        //					}
        //
        //					if (x_hole.getName().equals(
        //							"com.chatley.magicbeans.Notifiable")) {
        //						continue;
        //					}
        //
        //					if (sameShape(x_peg, x_hole)) {
        //
        //						if (x_cand_map.get(x_hole.getName()) != null) {
        //
        //							List x_candidates = (List) x_cand_map.get(x_hole
        //									.getName());
        //							x_candidates.add(new Binding(x_comp, p_comp,
        //									x_hole, x_peg));
        //
        //						} else {
        //
        //							List x_candidates = new ArrayList();
        //							x_candidates.add(new Binding(x_comp, p_comp,
        //									x_hole, x_peg));
        //							x_cand_map.put(x_hole.getName(), x_candidates);
        //						}
        //					}
        //				}
        //			}
        //
        //			Collection x_lists = x_cand_map.values();
        //			for (Iterator h = x_lists.iterator(); h.hasNext();) {
        //
        //				List x_candidates = (List) h.next();
        //
        //				if (x_candidates.size() > 0) {
        //
        //					// employ Strategies here to pick the right binding
        //
        //					Binding x_best = (Binding) x_candidates.get(0);
        //
        //					for (Iterator j = x_candidates.listIterator(1); j.hasNext();) {
        //
        //						Binding x_next = (Binding) j.next();
        //
        //						for (Iterator k = o_strategies.iterator(); k.hasNext();) {
        //
        //							Binding x_pref = ((Strategy) k.next()).prefer(
        //									x_best, x_next);
        //
        //							if (x_pref != null) {
        //								x_best = x_pref;
        //								break; //not sure about this - sets priority to
        //								// first added strategy over last
        //							}
        //						}
        //					}
        //
        //					x_best.getHole().decr();
        //					x_best.getHole().setBound(true);
        //					x_best.generateProxy(o_pegs_instantiated);
        //
        //					o_bindings.add(x_best);
        //
        //					o_log.msg("NEW BINDING : " + x_best);
        //				}
        //			}
        //		}
        //
        //		x_cand_map = new HashMap();
        //		List x_holes = p_comp.getHoles();
        //
        //		for (Iterator i = x_holes.iterator(); i.hasNext();) {
        //
        //			Hole x_hole = (Hole) i.next();
        //
        //			for (Iterator j = o_components.iterator(); j.hasNext();) {
        //
        //				x_comp = (Component) j.next();
        //
        //				for (Iterator k = x_comp.getPegs().iterator(); k.hasNext();) {
        //
        //					x_peg = (Peg) k.next();
        //
        //					//all holes filled?
        //					if (!x_hole.available()) {
        //						continue;
        //					}
        //
        //					if (x_hole.getName().equals(
        //							"com.chatley.magicbeans.Notifiable")) {
        //						continue;
        //					}
        //
        //					if (sameShape(x_peg, x_hole)) {
        //
        //						if (x_cand_map.get(x_hole.getName()) != null) {
        //
        //							// System.out.println("candidate list exists for this hole");
        //							List x_candidates = (List) x_cand_map.get(x_hole
        //									.getName());
        //							x_candidates.add(new Binding(x_comp, p_comp,
        //									x_hole, x_peg));
        //						} else {
        //
        //							// System.out.println("creating new candidate list for this hole");
        //
        //							List x_candidates = new ArrayList();
        //							x_candidates.add(new Binding(x_comp, p_comp, x_hole, x_peg));
        //							x_cand_map.put(x_hole.getName(), x_candidates);
        //						}
        //					}
        //				}
        //			}
        //
        //			Collection x_lists = x_cand_map.values();
        //			
        //			for (Iterator h = x_lists.iterator(); h.hasNext();) {
        //
        //				List x_candidates = (List) h.next();
        //
        //				if (x_candidates.size() > 0) {
        //
        //					// employ Strategies here to pick the right binding
        //
        //					Binding x_best = (Binding) x_candidates.get(0);
        //
        //					for (Iterator j = x_candidates.listIterator(1); j.hasNext();) {
        //
        //						Binding x_next = (Binding) j.next();
        //
        //						for (Iterator k = o_strategies.iterator(); k.hasNext();) {
        //
        //							Binding x_pref = ((Strategy) k.next()).prefer(
        //									x_best, x_next);
        //
        //							if (x_pref != null) {
        //			
        //								x_best = x_pref;
        //								break; //not sure about this - sets priority to
        //								// first added strategy over last
        //							}
        //						}
        //					}
        //
        //					//Binding x_bind = (Binding)x_candidates.get( 0 );
        //					x_best.getHole().decr();
        //
        //					x_best.generateProxy(o_pegs_instantiated);
        //
        //					o_bindings.add(x_best);
        //
        //					o_log.msg("NEW BINDING : " + x_best);
        //				}
        //			}
        //		}
    }

    public void remove(Component p_comp) {

        o_log.msg("about to remove component " + p_comp);

        List x_concerned = new ArrayList();

        //remove bindings to/from p_comp

        for (Iterator i = o_bindings.iterator(); i.hasNext();) {

            Binding x_bind = (Binding) i.next();

            // System.out.println("checking " + p_comp + " against " +
            // x_bind.getTo() );
            if (x_bind.getTo().equals(p_comp)) {
                x_concerned.add(x_bind);
                //System.out.println("binding " + x_bind + " added to concerned
                // list" );
                i.remove();
                //System.out.println("removed " + x_bind );
            }

            if (x_bind.getFrom().equals(p_comp)) {

                x_bind.getPeg().setBound(x_bind.getHole().getInterface(), false);
                x_bind.getHole().incr();
                i.remove();
                //System.out.println("removed " + x_bind );
            }
        }

        //notify the observer that their pegs have been removed

        List x_observers = o_observers.clonelist();

        for (Iterator i = x_observers.iterator(); i.hasNext();) {

            Object x_not = i.next();

            for (Iterator j = x_concerned.iterator(); j.hasNext();) {

                Binding x_binding = (Binding) j.next();

                if (!(x_binding.getFrom() == p_comp)
                        && (x_binding.getFrom().equals(x_not.getClass().getClassLoader()) || (x_binding.getFrom().getName().equals("framework.jar")))) {

                    // System.out.println("calling notifiable " + x_not );
                    // Object x_peg = o_pegs_instantiated.get(
                    // x_binding.getPeg() );

                    //NB if this call doesn't return things will hang -
                    // consider timeouts?
                    o_log.msg("notifying " + x_not);
                    MultiMethod.dispatch(x_not, "pluginRemoved", x_binding.getProxy());
                    // x_not.pluginRemoved( x_binding.getProxy() );
                    x_binding.disconnect();
                }
            }
        }

        o_components.remove(p_comp);

        bind();

        o_log.msg("removed " + p_comp);
    }

    public void replace(Component p_old, Component p_new) {

    }

    private void notifyObservers(Binding p_bind) {

        //special case

        if (p_bind.getFrom().getName().endsWith(ConfigurationManager.get("frameworkjar"))) {

            MultiMethod.dispatch(this, "pluginAdded", p_bind.getProxy());

        } else {

            List x_observers = o_observers.clonelist();

            for (Iterator i = x_observers.iterator(); i.hasNext();) {

                Object x_not = i.next();

                if (x_not.getClass().getClassLoader() == p_bind.getFrom()) {

                    MultiMethod.dispatch(x_not, "pluginAdded", p_bind.getProxy());
                }
            }
        }
    }

    private boolean sameShape(Peg p_peg, Hole p_hole) {

        //	System.out.println("checking : " + p_peg + " against " + p_hole );

        try {
            Class x_p = Class.forName(p_peg.getName(), false, p_peg.getComponent());
            Class x_h = Class.forName(p_hole.getName(), false, p_hole.getComponent());

            if (x_h.isAssignableFrom(x_p)) {
                o_log.msg(x_h + " : " + x_p + x_h.isAssignableFrom(x_p));
            }
            return (x_h.isAssignableFrom(x_p));

        } catch (ClassNotFoundException p_cnfe) {
            return false;
        }
    }

    private void register(Component p_comp) {

        o_log.msg("adding component " + p_comp.getName());
        o_components.add(p_comp);
    }

    public Component getLastComponent() {

        if (o_components.size() > 0) {

            return (Component) o_components.get(o_components.size() - 1);
        }

        return null;
    }

    public DarwinGenerator getDarwinGenerator(boolean p_net) {

        return new DarwinGenerator(p_net);
    }

    class DarwinGenerator {

        private PrintStream o_darwinstream;

        private Socket o_socket;

        private PrintStream o_darwinoutput = null;

        private ByteArrayOutputStream o_baos;

        private Map o_ports;

        private Map o_binding_constants;

        private OutputStream os = null;

        private Set o_interfaces;

        private Map o_requires;

        DarwinGenerator() {

            this(true);
        }

        DarwinGenerator(boolean p_network) {

            if (o_byteCodeChecker == null) {
                o_byteCodeChecker = new ByteCodeChecker();
            }

            o_ports = new HashMap();
            o_requires = new HashMap();
            o_interfaces = new HashSet();
            o_binding_constants = new HashMap();
            o_baos = new ByteArrayOutputStream();
            o_darwinstream = new PrintStream(o_baos);

            if (p_network) {

                try {

                    InetAddress x_server = InetAddress.getByName(ConfigurationManager.get("ltsaIP"));
                    o_socket = new Socket(x_server, 5555);
                    os = o_socket.getOutputStream();
                    o_darwinoutput = new PrintStream(os);

                } catch (Exception e) {
                    o_log.error(e.getMessage());
                }
            }
        }

        public void generateDarwin() {

            if (os == null) {
                return;
            }

            Set x_bound_comps = new HashSet();
            Map x_interfaces = new HashMap();

            for (Iterator i = o_bindings.iterator(); i.hasNext();) {

                Binding x_bind = (Binding) i.next();

                // 		String x_bind_const =
                // lastPart(x_bind.getFrom().getName()).toUpperCase() + "_" +
                // lastPart(x_bind.getHole().getName()).toUpperCase();

                // 		if ( o_binding_constants.get( x_bind_const ) == null ) {

                // 		    o_binding_constants.put( x_bind_const , new Integer( 1 ) );

                // 		} else {

                // 		    int c = ((Integer)o_binding_constants.get( x_bind_const
                // )).intValue();
                // 		    o_binding_constants.put( x_bind_const , new Integer( c + 1 )
                // );
                // 		}

                if (exclude(lastPart(x_bind.getTo().getName())) || exclude(lastPart(x_bind.getFrom().getName()))) {
                    continue;
                }

                x_bound_comps.add(x_bind.getTo());
                x_bound_comps.add(x_bind.getFrom());

                x_interfaces.put(x_bind.getHole().getName(), x_bind.getHole());
                o_interfaces.add(x_bind.getHole().getName());
            }

            for (Iterator i = x_interfaces.keySet().iterator(); i.hasNext();) {

                String x_holename = (String) i.next();
                Hole x_hole = (Hole) x_interfaces.get(x_holename);

                o_darwinstream.print("interface " + lastPart(x_hole.getName()) + " { ");

                for (Iterator j = x_hole.getMethods().iterator(); j.hasNext();) {

                    o_darwinstream.print(j.next() + "; ");
                }

                o_darwinstream.println("}\n");
            }

            for (Iterator i = x_bound_comps.iterator(); i.hasNext();) {

                Component x_comp = (Component) i.next();

                if (exclude(x_comp.getName())) {
                    continue;
                }

                String x_name = toProcessName(x_comp.getName());

                o_darwinstream.println("component " + x_name + " {");
                printRequires(x_comp);
                printProvides(x_comp);
                printBehaviour(x_comp);
                o_darwinstream.println("}\n");
            }

            o_darwinstream.println("component System {");

            char c = 'a';

            for (Iterator i = x_bound_comps.iterator(); i.hasNext();) {

                Component x_comp = (Component) i.next();
                String x_name = toProcessName(x_comp.getName());

                o_ports.put(x_comp, new Character(c));
                o_darwinstream.println("\t inst " + c++ + ":" + x_name + ";");
            }

            printBindings();

            o_darwinstream.println("}");

            //          if ( ! o_binding_constants.isEmpty() ) {

            // 		o_darwinoutput.println("/%");

            // 		for ( Iterator i = o_binding_constants.keySet().iterator() ;
            // i.hasNext() ; ) {

            // 		    String x_key = (String)i.next();
            // 		    o_darwinoutput.println( "const " + x_key + " = " +
            // o_binding_constants.get( x_key ) );
            // 		}

            // 		o_darwinoutput.println("%/");
            // 	    }

            o_darwinoutput.println("\n" + o_baos.toString() + "\n");
            o_darwinoutput.flush();
            o_darwinoutput.close();

            try {

                os.flush();
                os.close();
                o_socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String analyseComponent(Component p_comp) {

            o_baos = new ByteArrayOutputStream();
            o_darwinstream = new PrintStream(o_baos);
            String x_name = toProcessName(p_comp.getName());

            checkInterfaces(p_comp);

            o_darwinstream.println("component " + x_name + " {");
            printRequires(p_comp);
            printProvides(p_comp);
            printBehaviour(p_comp);
            o_darwinstream.println("}\n");

            return "\n" + o_baos.toString() + "\n";
        }

        private void checkInterfaces(Component p_comp) {

            for (Iterator i = p_comp.getHoles().iterator(); i.hasNext();) {

                Hole x_h = (Hole) i.next();
                o_interfaces.add(x_h.getName());
            }

            for (Iterator i = p_comp.getPegs().iterator(); i.hasNext();) {

                Peg x_p = (Peg) i.next();
                o_interfaces.add(x_p.getName());
            }
        }

        String getBehaviour(Component p_comp) {

            try {

                Class x_fspd = Class.forName("com.chatley.magicbeans.FSPDefinition");

                for (Iterator i = p_comp.getPegs().iterator(); i.hasNext();) {

                    Peg x_p = (Peg) i.next();
                    for (Iterator j = x_p.getImplementedInterfaces().iterator(); j.hasNext();) {

                        Class x_intf = (Class) j.next();

                        //System.out.println("checking : " + x_cl.getName() );
                        if (x_fspd.isAssignableFrom(x_intf)) {

                            Class x_cl = Class.forName(x_p.getName(), true, p_comp);

                            FSPDefinition x_fsp = (FSPDefinition) x_cl.newInstance();
                            return x_fsp.getFSP();
                        }

                    }
                }

            } catch (Exception p_e) {
                o_log.error(p_e.getMessage());
            }

            return "";
        }

        Set getAllActions(Component p_comp) {

            Set x_acts = new HashSet();

            for (Iterator i = p_comp.getPegs().iterator(); i.hasNext();) {

                Peg x_peg = (Peg) i.next();
                x_acts.addAll(x_peg.getMethods());
            }

            for (Iterator i = o_requires.keySet().iterator(); i.hasNext();) {

                String x_port = (String) i.next();

                for (Iterator j = ((Hole) o_requires.get(x_port)).getMethods().iterator(); j.hasNext();) {

                    x_acts.add(x_port + "." + (String) j.next());
                }
            }

            return x_acts;
        }

        private boolean exclude(String p_name) {

            if (p_name == null) {
                return true;
            }

            boolean x_ret = false;

            for (Iterator i = s_exclusions.iterator(); i.hasNext();) {

                String x_name = lastPart(toProcessName(p_name));

                x_ret |= x_name.equalsIgnoreCase((String) i.next());
            }

            return x_ret; //s_exclusions.contains( lastPart( toProcessName(
            // p_name ) ) );
        }

        String getProcessName(Component p_comp) {

            return toProcessName(p_comp.getName());
        }

        private String toProcessName(String p_name) {

            String x_name;
            if (p_name.indexOf("\\") > -1) {
                x_name = p_name.substring(p_name.lastIndexOf("\\") + 1, p_name.lastIndexOf("."));
            } else if (p_name.indexOf(".") > -1) {
                x_name = p_name.substring(0, p_name.lastIndexOf("."));
            } else {
                x_name = p_name;
            }

            return x_name.substring(0, 1).toUpperCase() + x_name.substring(1, x_name.length());
        }

        private void printBindings() {

            for (Iterator i = o_bindings.iterator(); i.hasNext();) {

                Binding x_bind = (Binding) i.next();

                if (exclude(lastPart(x_bind.getTo().getName())) || exclude(lastPart(x_bind.getFrom().getName()))) {
                    continue;
                }

                o_darwinstream.println("\t bind " + o_ports.get(x_bind.getTo()) + "." + lastPart(x_bind.getHole().getName()) + " -- "
                        + o_ports.get(x_bind.getFrom()) + "." + o_ports.get(x_bind.getFrom().getName() + x_bind.getHole().getName()) + ";");
            }
        }

        void printRequires(Component p_comp) {

            char c = 'a';

            for (Iterator i = p_comp.getHoles().iterator(); i.hasNext();) {

                Hole x_hole = (Hole) i.next();

                Class x_class = x_hole.getDeclaringClass();
                Field[] x_fields = x_class.getDeclaredFields();

                for (int j = 0; j < x_fields.length; j++) {

                    Class x_field_type = x_fields[j].getType();

                    if (x_field_type.isAssignableFrom(x_hole.getInterface())) {

                        if (!o_byteCodeChecker.inByteCode(p_comp, x_class.getName(), x_fields[j].getName()))
                            continue;

                        o_ports.put(p_comp.getName() + x_hole.getName(), x_fields[j].getName());
                        o_requires.put(x_fields[j].getName(), x_hole);
                        o_darwinstream.println("\t require " + x_fields[j].getName() + ":" + lastPart(x_hole.getName()) + ";");
                    }
                }

                // 	String x_port = x_hole.getName();
                //  		if ( o_interfaces.contains( x_port ) ) {
                //  		    o_ports.put( p_comp.getName() + x_port , new Character( c )
                // );
                //  		    o_darwinstream.println( "\t require " + c++ + ":" +
                // lastPart(x_port) + ";" );
                //  		}
            }
        }

        void printProvides(Component p_comp) {

            for (Iterator i = p_comp.getPegs().iterator(); i.hasNext();) {

                Peg x_peg = (Peg) i.next();

                for (Iterator j = x_peg.getImplementedInterfaces().iterator(); j.hasNext();) {

                    String x_port = ((Class) j.next()).getName();
                    if (o_interfaces.contains(x_port)) {
                        if (o_ports.get(p_comp.getName() + x_port) == null) {
                            o_ports.put(p_comp.getName() + x_port, new Character('c'));
                        }
                        o_darwinstream.println("\t provide " + lastPart(x_port) + ";");
                    }
                }
            }
        }

        private String lastPart(String p_name) {

            String x_name = p_name.trim();

            if (x_name.endsWith(".jar")) {
                x_name = x_name.substring(0, x_name.length() - 4);
            }

            if (x_name.indexOf(".") > -1) {
                x_name = x_name.substring(x_name.lastIndexOf(".") + 1, x_name.length());
            }

            if (x_name.indexOf("\\") > -1) {
                x_name = x_name.substring(x_name.lastIndexOf("\\") + 1, x_name.length());
            }

            return x_name;
        }

        private void printBehaviour(Component p_comp) {

            List x_fsp = (List) o_fsp.get(p_comp);

            if (x_fsp != null) {

                o_darwinstream.println("\t/%");

                int count = 1;
                for (Iterator i = x_fsp.iterator(); i.hasNext();) {

                    o_log.msg("printBehaviour : " + count++);
                    String x_beh = (String) i.next();

                    if (x_beh.indexOf(".") > -1) {

                        o_darwinstream.print("\t" + x_beh.substring(0, x_beh.indexOf(".")));
                        printAlphabetExtension(p_comp);
                        o_darwinstream.print(".");
                        
                    } else {

                        o_darwinstream.println("\t" + x_beh.trim()); //x_match.replaceAll(
                    }
                }

                o_darwinstream.println("\t%/");

            } else {

                Set x_actions = new HashSet();

                String x_name = toProcessName(p_comp.getName());

                o_darwinstream.println("\t/%");
                o_darwinstream.print("\t" + x_name + " = ( ");

                for (Iterator i = p_comp.getPegs().iterator(); i.hasNext();) {

                    Peg x_peg = (Peg) i.next();
                    x_actions.addAll(x_peg.getMethods());
                }

                Iterator i = x_actions.iterator();

                if (x_actions.size() > 1) {

                    o_darwinstream.print("{ ");

                    if (i.hasNext()) {
                        o_darwinstream.print(i.next());
                    }

                    while (i.hasNext()) {

                        String x_action = (String) i.next();
                        if (x_action.equals("set"))
                            continue;

                        o_darwinstream.print(", " + x_action);
                    }

                    o_darwinstream.println(" } -> " + x_name + ").\n\t%/");

                } else if (x_actions.size() == 1) {

                    o_darwinstream.println(i.next() + " -> " + x_name + " ).\n\t%/");
                }
            }
        }

        private void printAlphabetExtension(Component p_comp) {

            List x_pegs = p_comp.getPegs();

            for (Iterator i = x_pegs.iterator(); i.hasNext();) {

                Peg x_peg = (Peg)i.next();
                List x_intfs = x_peg.getImplementedInterfaces();

                for (Iterator j = x_intfs.iterator(); j.hasNext();) {

                    Class x_cl = (Class) j.next();
                    String x_set = lastPart( x_cl.getName() );
                    
                    if ( j.hasNext() ) { o_darwinstream.print( " + " + x_set ); }
                }
            }
        }
    }

    public void pluginAdded(Strategy p_str) {

        o_strategies.add(p_str);
        o_log.msg("********** added Strategy ***********");
    }

    public void pluginAdded(Adder p_add) {

        o_adders.add(p_add);

        o_log.msg("********** added Adder **************");
        new Thread((Adder) p_add).start();
    }

    public void pluginAdded(Logger p_log) {

        o_log = p_log;
        o_log.msg("new logger added");
    }

    public void pluginAdded(FSPDefinition p_fsp) {

        if (o_fsp.get(p_fsp.getClass().getClassLoader()) == null) {
            o_fsp.put(p_fsp.getClass().getClassLoader(), new ArrayList());
        }

        List x_list = (List) o_fsp.get(p_fsp.getClass().getClassLoader());
        x_list.add(p_fsp.getFSP());

        o_log.msg("PM - adding FSP def : " + p_fsp.getFSP());
    }

    public void pluginRemoved(Strategy p_strat) {

        if (o_strategies.remove(p_strat)) {
            o_log.msg("********** remove Strategy ***********");
        } else {
            o_log.msg("can't remove " + p_strat);
        }
    }

    public static void main(String p_args[]) {

        try {

            //try this in static initialiser s_pm = new PluginManager();

            ConfigurationManager.configure(p_args[0]);

            Component x_framework = new Component(ConfigurationManager.get("frameworkjar"), s_pm.getClass().getClassLoader());
            s_pm.addObserver(s_pm);

            Component x_start = new Component(ConfigurationManager.get("targetjar"), x_framework);

            s_pm.register(x_framework);
            s_pm.register(x_start);

            s_pm.bind();

            //install and start the adders -- just a test one for now

            Adder x_adder = new DirectoryMonitor("plugins", ConfigurationManager.get("pollpluginsdir").equals("true"));
            new Thread(x_adder).start();

            //start the application

            try {

                o_log.msg("about to load mainclass");

                Class x_mainclass = Class.forName(ConfigurationManager.get("targetclass"), true, x_start);
                o_log.msg("done");

                Method x_main = x_mainclass.getMethod("main", new Class[] { p_args.getClass() });

                x_main.invoke(null, new Object[] { new String[] {} });

            } catch (ClassNotFoundException p_cnfe) {
                o_log.error("Class not found in application");
            } catch (NoSuchMethodException p_nsme) {
                o_log.error("Class does not have a main method");
            } catch (IllegalAccessException p_iae) {
                o_log.error(p_iae.getMessage());
            } catch (InvocationTargetException p_ite) {
                o_log.error("Inv err" + p_ite + p_ite.getCause());
                p_ite.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

