package com.chatley.magicbeans;

public class Pair {

    public Object a;
    public Object b;

    public Pair(){}
    public Pair( Object p_a , Object p_b ) { a = p_a; b = p_b; } 

    public boolean equals( Object o ) {

	if ( o instanceof Pair ) {
	    Pair p = (Pair)o;
	    return ( p.a == a ) && ( p.b == b );
	}

	return false;
    }
}
