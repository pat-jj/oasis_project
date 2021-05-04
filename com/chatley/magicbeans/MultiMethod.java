package com.chatley.magicbeans;

import java.lang.reflect.*;

public class MultiMethod {

    public static void dispatch( Object p_recv , String p_meth , Object p_param ) {
	
	try {

	    Method m = locate( p_recv.getClass() , p_meth , p_param.getClass().getInterfaces() );
	    
	    if ( m != null ) m.invoke( p_recv , new Object[] { p_param } );

	} catch ( Exception e ) { System.err.println( e ); }
    }

    private static Method locate( Class p_search , String p_meth , Class[] p_ints ) { 

 	for ( int i = 0 ; i < p_ints.length ; i++ ) {
	    
	    //	    System.out.println("interface : " + p_ints[i].getName() );
	    try { 
		Method m = p_search.getDeclaredMethod( p_meth , new Class[] { p_ints[i] } );	    
		return m;
	    } catch ( NoSuchMethodException nsme ) { 
		
		Method m = locate( p_search , p_meth , p_ints[i].getInterfaces() );
		if ( m != null ) return m;
	    }
	}

	return null;
    }
}
