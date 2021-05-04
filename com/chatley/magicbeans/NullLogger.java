package com.chatley.magicbeans;

class NullLogger implements Logger {

    public void msg( String p_msg ) {}

    public void error( String p_msg ) {

	System.err.println( p_msg );
    }
}
