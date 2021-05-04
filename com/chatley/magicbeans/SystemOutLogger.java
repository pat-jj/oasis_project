package com.chatley.magicbeans;

class SystemOutLogger implements Logger {

    public void msg( String p_msg ) { System.out.println( p_msg ); }

    public void error( String p_msg ) {

        System.err.println( p_msg );
    }
}
