package com.chatley.magicbeans;

import java.util.*;

/**
 ** JarClassLoader provides a minimalistic ClassLoader which shows how to
 ** instantiate a class which resides in a .jar file.<br><br>
 **
 ** @author	John D. Mitchell, Non, Inc., Mar  3, 1999
 **
 ** @version 0.5
 **
 **/

public class JarClassLoader extends MultiClassLoader {

    private List                jarResList;
    private JarResources	jarResources;
    
    public JarClassLoader (String jarName) {
	
	// Create the JarResource and suck in the .jar file.
	jarResList = new ArrayList();
	jarResources = new JarResources (jarName);
	jarResList.add( jarResources );
    }

    public void addJar( String jarName ) {
	jarResources = new JarResources( jarName );
	jarResList.add( jarResources  );
    }

    protected byte[] loadClassBytes (String className) {
	
	// Support the MultiClassLoader's class name munging facility.
	className = formatClassName (className);
	
	for ( Iterator i = jarResList.iterator() ; i.hasNext() ; ) {

	    JarResources x_jr = (JarResources)i.next();

	    //a bit hackish

	    try {
		if ( x_jr.getResource( className ) == null ) continue;

		return ( x_jr.getResource (className));
	    } catch ( Exception e ) {}
	}

	return null;
    }

    public Enumeration enumerateResources() {

	return jarResources.enumerateResources();
    }
}	


