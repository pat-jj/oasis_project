package com.chatley.magicbeans;

import java.lang.reflect.*;

class PluginInvocationHandler implements InvocationHandler {

    private Object o_imp;
   
    public PluginInvocationHandler( Object p_imp ) { o_imp = p_imp; }

    public synchronized Object invoke(Object p_proxy, Method p_method, Object[] p_args) throws Throwable {

	if ( o_imp != null ) {

	    if ( p_method.getName().equals( "equals" ) ) { return new Boolean( p_args[0] == p_proxy ); }

	    try {
		return p_method.invoke( o_imp , p_args );
	    } catch ( Exception p_e ) { 
		System.err.println("error invoking method : " + p_e ); return null;
	    }

	} else {
	    throw new RuntimeException("plugin removed");
	}
    }

    public synchronized void removeImp() { o_imp = null; }

    public synchronized void upgrade( Object p_upg ) { o_imp = p_upg; }
}
