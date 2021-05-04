package com.chatley.magicbeans;

import java.util.Properties;
import java.io.*;

class ConfigurationManager {

    static Properties o_config = new Properties();

    static void configure( String p_file ) {

	o_config.setProperty( "ltsaIP" , "127.0.0.1" );
	o_config.setProperty( "generatedarwin" , "false" );
	o_config.setProperty( "bindpolicy" , "1-1" );
	o_config.setProperty( "pollpluginsdir" , "true" );

	try {

	    FileInputStream x_in = new FileInputStream( p_file );
	    o_config.load( x_in );
	} catch ( IOException p_ioe ) {
	    System.err.println( p_ioe );
	}
    }

    static String get( String p_key ) {

	return o_config.getProperty( p_key );
    }
}
