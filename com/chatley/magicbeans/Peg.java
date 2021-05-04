package com.chatley.magicbeans;

import java.util.*;
import java.lang.reflect.Method;

class Peg {

    private String o_name;
    private Component o_comp;
    private boolean o_bound;
    private Map o_intf_impl;

    Peg( String p_name , Component p_comp , List p_intfs ) {

	o_name = p_name; 
	o_comp = p_comp;
	o_bound = false;
	o_intf_impl = new HashMap();

	for ( Iterator i = p_intfs.iterator() ; i.hasNext() ; ) {
	    o_intf_impl.put( i.next() , new Boolean( false ) );
	}
    }

    boolean isBound( Class p_intf ) { 

	if ( o_intf_impl.get( p_intf ) != null )
	    return   ((Boolean)o_intf_impl.get( p_intf )).booleanValue();
	return false;
    }

    void setBound( Class p_intf , boolean p_bound ) {

	o_intf_impl.put( p_intf , new Boolean( p_bound) );  
    }

    String getName() { return o_name; }
    Component getComponent() { return o_comp; }
    List getImplementedInterfaces() { return new ArrayList( o_intf_impl.keySet() ); }

    Set getMethods() {

	Set x_meth = new HashSet();

	for ( Iterator i = o_intf_impl.keySet().iterator() ; i.hasNext() ; ) {

	    Class x_int = (Class)i.next();
  
	    for( Iterator j = Arrays.asList( x_int.getMethods() ).iterator() ; j.hasNext() ; ) {

		Method x_m = (Method)j.next();
		x_meth.add( x_m.getName() );
	    }
	}

	return x_meth;
    }

    public String toString() { return o_name; }
}
