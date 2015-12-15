package alpv.mwp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class MyPool<T> extends UnicastRemoteObject implements Pool<T> {


	private static final long serialVersionUID = 1L;
	ArrayList<T> poolList;

	// Constructor
	protected MyPool() throws RemoteException {
		super();
		poolList = new ArrayList<>();
	}

	@Override
	public void put(T t) throws RemoteException {
		// adds the new T to the list
		synchronized (poolList) {
			poolList.add(t);
		}
	}

	@Override
	public T get() throws RemoteException {
		// tries to get a T from the list (index 0)
		// returns the T or null
		T task = null;
		synchronized (poolList) {
			if (poolList.isEmpty()) {
				task = null;
			} else {
				task = poolList.get(0);
				poolList.remove(0);
			}
		}
		return task;
	}

	@Override
	public int size() throws RemoteException {
		synchronized (poolList) {
			return poolList.size();
		}
	}

}
