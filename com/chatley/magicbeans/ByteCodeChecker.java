package com.chatley.magicbeans;

import java.io.*;
import java.util.*;
import de.fub.bytecode.classfile.*;
import de.fub.bytecode.*;

public class ByteCodeChecker {

    boolean   code = true;
    boolean constants = false;
    boolean verbose = false;
    boolean classdep = false;
    boolean nocontents = false;
    boolean recurse = false;
    Hashtable listedClasses = new Hashtable();
    Vector    exclude_name = new Vector();
    Set o_fields = new HashSet();

    String o_name;
    
    public ByteCodeChecker() {}


    public ByteCodeChecker(boolean code, boolean constants, boolean verbose, boolean classdep,
		     boolean nocontents, boolean recurse, Vector exclude_name)
    {
	this.code = code;
	this.constants = constants;
	this.verbose = verbose;
	this.classdep = classdep;
	this.nocontents = nocontents;
	this.recurse = recurse;
	this.listedClasses = new Hashtable();
	this.exclude_name = exclude_name;
	o_fields = new HashSet();
    }

    /** Print the given class on screen
     */
    public void list( String name) {
	try {
	    JavaClass java_class;

	    o_name = name;

	    if((listedClasses.get(name) != null) || name.startsWith("["))
		return;

	    for(int idx = 0; idx < exclude_name.size(); idx++)
		if(name.startsWith((String) exclude_name.elementAt(idx)))
		    return;

	    if((java_class = Repository.lookupClass(name)) == null)
		java_class = new ClassParser(name).parse(); // May throw IOException

	    Field[] x_fields = java_class.getFields();

	    for ( int i=0 ; i < x_fields.length ; i++ ) {

		o_fields.add( x_fields[i].getName() );
	    }

	    printCode(java_class.getMethods(), verbose);

	} catch(IOException e) {
	    System.out.println("Error loading class " + name + " (" + e.getMessage() + ")");
	}
	catch(Exception e) {
	    System.out.println("Error processing class " + name + " (" + e.getMessage() + ")");
	}
    }


    public String getByteCode( Component p_comp , String name) {

	try {
	    JavaClass java_class;

	    o_name = name;

	    if((listedClasses.get(name) != null) || name.startsWith("["))
		return "";

	    for(int idx = 0; idx < exclude_name.size(); idx++)
		if(name.startsWith((String) exclude_name.elementAt(idx)))
		    return "";

	    if((java_class = Repository.lookupClass(name)) == null) {

		String x_name = name.replaceAll( "\\." , "/" ) + ".class";

		InputStream x_is = p_comp.getResourceAsStream(x_name);
		//System.out.println( "InputStream for " + x_name + " is null? " + ( x_is == null ) );
		java_class = new ClassParser( x_is, name ).parse(); // May throw IOException
	    }

	    Field[] x_fields = java_class.getFields();

	    for ( int i=0 ; i < x_fields.length ; i++ ) {

		o_fields.add( x_fields[i].getName() );
	    }

	    return printCode(java_class.getMethods(), verbose);

	} catch(IOException e) {
	    System.out.println("Error loading class " + name + " (" + e.getMessage() + ")");
	}
	catch(Exception e) {
	    System.out.println("Error processing class " + name + " (" + e.getMessage() + ")");
	    e.printStackTrace();
	}

	return "";
    }


    /**
     * Dump the list of classes this class is dependent on
     */
    public static void printClassDependencies(ConstantPool pool) {
	String[] names = getClassDependencies(pool);
	System.out.println("Dependencies:");
	for(int idx = 0; idx < names.length; idx++)
	    System.out.println("\t" + names[idx]);
    }

    public static String[] getClassDependencies(ConstantPool pool) {
	String[] tempArray = new String[pool.getLength()];
	int size = 0;
	StringBuffer buf = new StringBuffer();

	for(int idx = 0; idx < pool.getLength(); idx++) {
	    Constant c = pool.getConstant(idx);
	    if(c != null && c.getTag() == Constants.CONSTANT_Class) {
		ConstantUtf8 c1 = (ConstantUtf8) pool.getConstant(((ConstantClass)c).getNameIndex());
		buf.setLength(0);
		buf.append(new String(c1.getBytes()));
		for(int n = 0; n < buf.length(); n++) {
		    if(buf.charAt(n) == '/')
			buf.setCharAt(n, '.');
		}

		tempArray[size++] = buf.toString();
	    }
	}

	String[] dependencies = new String[size];
	System.arraycopy(tempArray, 0, dependencies, 0, size);
	return dependencies;
    }

    /**
     * Dump the disassembled code of all methods in the class.
     */
    public String printCode(Method[] methods, boolean verbose) {

	// this code finds all the pluginAdded methods and adds a requires field for each 

	StringBuffer x_tmp = new StringBuffer();

	for(int i=0; i < methods.length; i++) {

	    if ( ! methods[i].getName().equals( "pluginAdded" ) ) continue;

	    String x_type = new de.fub.bytecode.generic.MethodGen( methods[i], o_name , null ).getArgumentType( 0 ).toString();

	    Code code = methods[i].getCode();
	    if(code != null) {
		String x_code = code.toString(verbose);
      		x_tmp = x_tmp.append( x_code );
	    }
	}

	return x_tmp.toString();
    }

    public boolean inByteCode( Component p_comp , String p_class , String p_field ) {

	String x_code = getByteCode( p_comp , p_class );

	return ( x_code.indexOf( p_field ) > -1 );	
    }

    public static void main( String[] args ) {

	new ByteCodeChecker().list( "FilterImp" );
    }
}




//		for ( Iterator j = o_fields.iterator() ; j.hasNext() ; ) {

//		    String x_field = (String)j.next();
		    
// 		    try {

// 			if ( ! Class.forName( x_type ).isAssignableFrom( Class.forName( o_name ) ) ) continue;

// 			if ( x_code.indexOf( x_field ) > -1 ) { System.out.println( " require : " + x_field + ":" + x_type +";" ); }

// 		    } catch ( ClassNotFoundException p_cnfe ) {
// 		    }
// 		}
