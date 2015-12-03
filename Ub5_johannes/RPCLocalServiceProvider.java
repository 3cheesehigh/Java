package rpc.server;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;

/**
 * <p>
 * Eine Implementierung des RPCServiceProviders, die eine statische Methode
 * einer Klasse <b>lokal</b> ausfuehrt und deren Ergebis zurueck liefert.
 * </p>
 * 
 * <p>
 * Zur Implementierung wird hier das Java-Reflections-Framework verwendet, im
 * speziellen die Methoden
 * 
 * <ul>
 * <li>{@link Class#forName(String)}</li>
 * <li>{@link Class#getMethod(String, Class...)}</li>
 * <li>{@link Method#invoke(Object, Object...)}</li>
 * </ul>
 * </p>
 */
public class RPCLocalServiceProvider extends RPCServiceProvider {
	private boolean callPrimitives;

	public RPCLocalServiceProvider() {
		this(true);
	}

	/**
	 * Bei diesem Konstruktor kann gewählt werden, ob man möchte, dass beim
	 * Suchen nach den Methoden die Primitivklassen benutzt werden sollen,
	 * anstatt die der Wrapperklassen.
	 * 
	 * @param callPrimitivesIfBoxed
	 */
	public RPCLocalServiceProvider(boolean callPrimitivesIfBoxed) {
		this.callPrimitives = callPrimitivesIfBoxed;
	}

	/**
	 * <p>
	 * Bei der Implemenierung dieser Methode ist darauf zu achten, das
	 * {@link Class#getMethod(String, Class...)} eine Liste der Type-Parameter
	 * erwartet, nicht eine Liste der Parameter. Den Typen eines Parameters kann
	 * man ueber {@link Object#getClass()} herausfinden.
	 * </p>
	 * <p>
	 * Weiterhin ist der Hinweis auf dem Uebungsblatt ueber primitive Datentypen
	 * zu beachten. Siehe dazu auch
	 * {@link RPCSecrets#warpToPrimitiveClass(Class)}
	 * </p>
	 * 
	 * @param classname
	 *            Name der Klasse, in der die aufzurufende statische Methode
	 *            sich befindet
	 * @param methodname
	 *            Name der aufzurufenden statischen Methode
	 * @param params
	 *            Liste aller Parameter mit denen die statische Methode
	 *            aufzurufen ist.
	 * @return Rueckgabewert des Methodeaufrufs
	 * @throws RPCException
	 *             wird geworfen, falls waernd des Aufrufes ein Versagen
	 *             auftritt ( die Methode kann nicht gefunden werden, die Klasse
	 *             kann nicht gefunden werden, die Methode wirft eine Exception)
	 */
	@SuppressWarnings("unchecked")
	public <R> R callexplicit(String classname, String methodname, Serializable[] params) throws RPCException {
		System.out.println("----------------------------------------------------------------------------------");
		Class<?>[] paramTypes = new Class[params.length];
		Class<?> clazz;
		R result = null;
		try {
			clazz = Class.forName(classname);
			for (int i = 0; i < params.length; i++) {
				if (callPrimitives) {
					Class<?> c = params[i].getClass();
					paramTypes[i] = RPCSecrets.warpToPrimitiveClass(c);
				}
			}
			Method method = (Method) clazz.getMethod(methodname, paramTypes);
			result = (R) method.invoke(null, (Object[]) params);
		} catch (ClassNotFoundException e) {
//			System.out.println("Class " + classname + " not found");
			return (R) ("Class " + classname + " not found");
		} catch (NoSuchMethodException e) {
//			System.out.println("Method " + methodname + "(" + toString(paramTypes) + ") in Class " + classname + " not found");
			return (R) ("Method " + methodname + "(" + toString(paramTypes) + ") in Class " + classname + " not found");
		} catch (SecurityException e) {
//			System.out.println("Security: " + e);
			return (R) ("Security: " + e);
		} catch (IllegalAccessException e) {
//			System.out.println("Illegal Access: " + e);
			return (R) ("Illegal Access: " + e);
		} catch (IllegalArgumentException e) {
//			System.out.println("Illegal Argument: " + e);
			return (R) ("Illegal Argument: " + e);
		} catch (InvocationTargetException e) {
//			System.out.println("Illegal Invokation Target: " + e);
			return (R) ("Illegal Invokation Target: " + e);
		}
		return result;
	}

	private static String toString(Class<?>[] params) {
		String ret = "";
		for (Class<?> c : params) {
			ret += c.getName().substring(c.getName().lastIndexOf('.') + 1) + ",";
		}
		return ret.substring(0, ret.length() - 1);
	}

}
