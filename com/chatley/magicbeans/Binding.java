package com.chatley.magicbeans;

import java.lang.reflect.*;
import java.util.Map;

public class Binding {

    private Component o_to;
    private Component o_from;
    private Hole o_hole;
    private Peg o_peg;

    private PluginInvocationHandler o_ih;
    private Object o_proxy;

    Binding( Component p_from , Component p_to , Hole p_hole , Peg p_peg ) {

	o_from = p_from;
	o_to = p_to;
	o_hole = p_hole;
	o_peg = p_peg;
    }

    public Component getTo() { return o_to; }
    public Component getFrom() { return o_from; }
    public Peg getPeg() { return o_peg; }
    public Hole getHole() { return o_hole; }

    public String toString() { return "From " + o_hole + " in " + o_from + " to " + o_peg + " in " + o_to; }

    public Object getProxy() { return o_proxy; } 

    public void disconnect() { o_ih.removeImp();  }

    public void generateProxy( Map p_pegs_instantiated ) {
	
	Object x_new = null;
	Class x_proxy_intf = null;

	try {
	    
	    x_proxy_intf = Class.forName( o_hole.getName() , true , o_from );
	    
	    if ( p_pegs_instantiated.get( o_peg ) == null ) {
		
		Class x_peg_class = Class.forName( o_peg.getName() , true , o_to );
		
		x_new = x_peg_class.newInstance();
		
		p_pegs_instantiated.put( o_peg , x_new );
		
	    } else {
		
		x_new = p_pegs_instantiated.get( o_peg );
	    }
	    
	    o_ih = new PluginInvocationHandler( x_new );
	    o_proxy = Proxy.newProxyInstance( o_to , new Class[] { x_proxy_intf }, o_ih );
	    
	} catch ( Exception p_e ) { System.err.println( p_e ); }
    }
}
