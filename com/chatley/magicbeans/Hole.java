package com.chatley.magicbeans;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;

class Hole {

    private Component o_comp;
    private int o_card;
    private String o_interface;
    private List o_methods;
    private boolean o_bound;
    private Class o_intf;
    private Class o_class;
    private String o_name;

    Hole() { 

	o_card = -1; 
	o_bound = false;
    }

    Hole( String p_intf , Class p_class , Component p_comp ) { 

	o_bound = false;
	o_card = -1; 
	o_interface = p_intf;
	o_comp = p_comp;
	o_methods = new ArrayList();
	o_class = p_class;

	try {
	    o_intf = Class.forName( p_intf, false, p_comp );
	
	    Method[] x_meths = o_intf.getDeclaredMethods();
	    
	    for ( int i = 0 ; i < x_meths.length ; i++ ) {
		
		o_methods.add( x_meths[i].getName() );
	    }
	} catch ( ClassNotFoundException p_cnfe ) {}
    }

    Hole( String p_intf , Class p_class , Component p_comp , String p_name , int p_cap ) {

	this( p_intf, p_class, p_comp );
	o_name = p_name;
	o_card = p_cap;
    }

    void setCardinality( int p_card ) { o_card = p_card;  }
    int  getCardinality() { return o_card; }

    void decr() { o_card--; }
    void incr() { o_card++; setBound( ! available() ); }

    boolean isBound() { return o_bound; }
    void setBound( boolean p_bound ) { o_bound = p_bound; }

    boolean available() { return o_card != 0; }
    String getName() { return o_interface; }
    Component getComponent() { return o_comp; }
    Class getInterface() { return o_intf ; }
    Class getDeclaringClass() { return o_class; }
    List getMethods() { return o_methods; }

    public String toString() { return o_interface + " : " + o_card; }
}
