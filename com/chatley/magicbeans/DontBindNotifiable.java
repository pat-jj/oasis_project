package com.chatley.magicbeans;

public class DontBindNotifiable implements Strategy {

    public Binding prefer( Binding p_a , Binding p_b ) {

	System.out.println("using strategy");

	if (   p_a.getHole().getName().equals( "com.chatley.magicbeans.Notifiable" ) && 
	       ! p_b.getHole().getName().equals( "com.chatley.magicbeans.Notifiable" ) ) {
	    return p_b;
	}

	if (   p_b.getHole().getName().equals( "com.chatley.magicbeans.Notifiable" ) && 
	       ! p_a.getHole().getName().equals( "com.chatley.magicbeans.Notifiable" ) ) {
	    return p_a;
	}

	return null;
    }
}
