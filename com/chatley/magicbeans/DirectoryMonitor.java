package com.chatley.magicbeans;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

class DirectoryMonitor implements Adder {
    
    private PluginManager o_pm;
    private File o_dir;
    private List o_files;

    private boolean o_loop;

    DirectoryMonitor( File p_dir , boolean p_loop ) { 

	o_pm = PluginManager.getInstance();
	o_dir = p_dir; 
	o_files = new ArrayList();
	o_loop = p_loop;
    }

    DirectoryMonitor( String p_dir , boolean p_loop ) { 

	o_pm = PluginManager.getInstance();
	o_dir = new File( p_dir ); 
	o_files = new ArrayList();
	o_loop = p_loop;
    }

    public void run() {

	do {

	    File[] x_files = o_dir.listFiles();

	    if ( x_files != null ) {
		
		for ( int i=0 ; i < x_files.length ; i++ ) {
		    if ( x_files[i].getName().endsWith( ".jar" ) ){
			
			if ( ! o_files.contains( x_files[i] ) ) {
			    
			    System.out.println( "about to add " + x_files[i].getPath() );
			    o_pm.add( new Component( x_files[i] , o_pm.getLastComponent() ) );
			    o_files.add( x_files[i] );
			}
		    }
		}
	    }

	    try {
		Thread.sleep( 5000 );
	    } catch ( InterruptedException p_ie ) {}

	} while ( o_loop );

    }

}
