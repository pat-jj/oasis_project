package com.chatley.magicbeans;

import java.util.*;

class ObserverList extends ArrayList {

    public synchronized Iterator iterator() {

	return super.iterator();
    }

    public synchronized boolean add( Object o ) {
	return super.add( o );
    }

    public List clonelist() {
	return (List)clone();
    }
}
