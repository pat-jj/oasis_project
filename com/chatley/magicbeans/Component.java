package com.chatley.magicbeans;

import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Component extends URLClassLoader {

	static final int BIG = 999;

	private static JarClassLoader s_jcl;

	private String o_name;

	private List o_interfaces;

	private List o_conc_classes;

	public Component(String p_jar, Component p_comp) {

		super(new URL[] {}, p_comp);
		addURL(getJarURL(p_jar));

		if (s_jcl == null) {
			s_jcl = new JarClassLoader(p_jar);
		} else {
			s_jcl.addJar(p_jar);
		}

		o_name = p_jar;
		System.out.println("file -- " + o_name);
		o_interfaces = new ArrayList();
		o_conc_classes = new ArrayList();
		interrogate();
	}

	public Component(File p_jar, Component p_comp) {

		super(new URL[] {}, p_comp);
		addURL(getJarURL(p_jar));

		if (s_jcl == null) {
			s_jcl = new JarClassLoader(p_jar.getPath());
		} else {
			s_jcl.addJar(p_jar.getPath());
		}

		o_name = p_jar.getPath();
		System.out.println("file -- " + o_name);
		o_interfaces = new ArrayList();
		o_conc_classes = new ArrayList();
		interrogate();
	}

	public Component(String p_jar, ClassLoader p_cl) {

		super(new URL[] {}, p_cl);
		addURL(getJarURL(p_jar));

		if (s_jcl == null) {
			s_jcl = new JarClassLoader(p_jar);
		} else {
			s_jcl.addJar(p_jar);
		}

		o_name = p_jar;
		System.out.println("file -- " + o_name);
		o_interfaces = new ArrayList();
		o_conc_classes = new ArrayList();
		interrogate();
	}

	public String getName() {
		return o_name;
	}

	public List getHoles() {
		return o_interfaces;
	}

	public List getPegs() {

		return o_conc_classes;
	}

	private URL getJarURL(String p_jar) {

		File x_file = new File(p_jar);
		return getJarURL(x_file);
	}

	private URL getJarURL(File p_file) {

		try {
			return p_file.toURL();
		} catch (java.net.MalformedURLException p_mue) {
			return null;
		}
	}

	void interrogate() {

		Enumeration x_enum = s_jcl.enumerateResources();

		while (x_enum.hasMoreElements()) {

			String x_res = x_enum.nextElement().toString();

			if (x_res.indexOf('$') != -1)
				continue; // no inner classes

			if (x_res.endsWith(".class")) {

				String x_class = x_res.substring(0, x_res.lastIndexOf("."));
				String x_binary_label = x_class.replace('/', '.');

				Class x_cl = null;

				try {

					x_cl = loadClass(x_binary_label, true); // was
															// s_jcl.loadClass

					// skip over interfaces

					if (x_cl.isInterface()) {
						continue;
					}

					if (x_cl.getModifiers() != Modifier.ABSTRACT) {

						//check for provisions

						List x_interfaces = implementedInterfaces(x_cl);

						if (x_interfaces.size() > 0) {

							//System.out.println( "Peg found : " + getName() +
							// " - " + x_cl.getName() + " - " + x_cl.getName()
							// );
							o_conc_classes.add(new Peg(x_cl.getName(), this,
									x_interfaces));
						}
					}

					// check for requirements

					// need to add (or move to) here, the byte-code reading code
					// add a hole for each field of appropriate types featured
					// (assigned to?)
					// in pluginAdded. Add name (as well as type) to holes?

					List x_fields = null;

					//System.out.println("********** try to get declared fields
					// for " + x_cl.getName() );

					//try {
					x_fields = Arrays.asList(x_cl.getDeclaredFields());
					//} catch ( Exception e ) {

					// System.err.println( "error trying to read declared fields
					// of " + x_cl.getName() );
					//e.printStackTrace();
					//	}

					List x_add_meths = getMethods("pluginAdded", x_cl);

					for (Iterator i = x_fields.iterator(); i.hasNext();) {

						boolean x_matched = false;
						Field x_field = (Field) i.next();

						for (Iterator j = x_add_meths.iterator(); j.hasNext();) {

							Method x_meth = (Method) j.next();
							Class[] x_params = x_meth.getParameterTypes();

							if (x_params.length > 1) {
								continue;
							}

							if (x_field.getType().isAssignableFrom(x_params[0])) {

								int x_cap = 1;

								//check the bytecode for the presence of
								// x_field.getName()
								if (new ByteCodeChecker().inByteCode(this, x_cl
										.getName(), x_field.getName())) {

									//boolean x_isArray = Array.isArray(
									// x_field.getType() );
									//  if ( x_isArray ) {
									//	if ( x_field.getValue() != null ) {
									//    x_cap = Array.getLength( x_field.get( )
									// ); hmm need an object ref here
									//	}

									//System.out.println("adding new hole " +
									// x_field.getName() + ":" +
									// x_params[0].getName() );
									o_interfaces.add(new Hole(x_params[0]
											.getName(), x_cl, this, x_field
											.getName(), x_cap));
								}

							} else {

								Class x_collection = Class
										.forName("java.util.Collection");

								if (x_collection.isAssignableFrom(x_field
										.getType())) {

									int x_cap = BIG;

									if (new ByteCodeChecker().inByteCode(this,
											x_cl.getName(), x_field.getName())) {

										o_interfaces.add(new Hole(x_params[0]
												.getName(), x_cl, this, x_field
												.getName(), x_cap));
									}
								}
								//    } catch ( ClassNotFoundException p_cnfe ) {

								// 				System.err.println(" Could not load
								// java.util.Collection" );
								// 			    }
							}

						}

						//			    Hole x_hole = new Hole( x_params[0].getName() , x_cl
						// , this );

						//	System.out.println( "Hole found : " + getName() + " -
						// " + x_cl.getName() +
						//" - " + "pluginAdded( " + x_params[0].getName() + ")"
						// );

						//  o_interfaces.add( x_hole );

						//	}
					}

					//dont' know how to do cardinalities using this new stuff
					// right now

					//	try {

					//  Field x_field = x_cl.getField( "cardinality" );
					//  x_hole.setCardinality( x_field.getInt( null ) );

					// 	} catch ( NoSuchFieldException p_nsfe ) {
					//  } catch ( SecurityException p_se ) {
					//  }

					//	System.out.println("added hole : " + x_hole.getName() );
				}

				catch (NoClassDefFoundError p_ncdf) {

					System.err.println("Couldn't load : " + x_binary_label);
					// p_ncdf.printStackTrace();
					continue;

				} catch (Exception e) {

					// e.printStackTrace();
					continue;
				}

			}
		}
	}

	private List getMethods(String p_name, Class p_class) {

		Method[] x_meths = p_class.getDeclaredMethods();
		List x_matches = new ArrayList();

		for (int i = 0; i < x_meths.length; i++) {

			if (x_meths[i].getName().equals(p_name)) {
				x_matches.add(x_meths[i]);
			}
		}

		return x_matches;
	}

	private List implementedInterfaces(Class p_cl) {

		return implementedInterfaces(p_cl, new ArrayList());
	}

	private List implementedInterfaces(Class p_cl, List p_int) {

		if (p_cl == null) {
			return p_int;
		} else {
			p_int.addAll(Arrays.asList(p_cl.getInterfaces()));
			return implementedInterfaces(p_cl.getSuperclass(), p_int);
		}
	}

	public String toString() {
		return getName();
	}

	public boolean equals(Object o) {
		return toString().equals(o.toString()) && o instanceof Component;
	}
}