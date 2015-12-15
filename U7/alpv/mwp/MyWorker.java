package alpv.mwp;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MyWorker extends UnicastRemoteObject implements Worker {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Master master = null;
	private int maxThreads;
	private int countThreads;
	public boolean seperate = true;

	// Constructor
	public MyWorker(String host, int port, int maxThreads) throws RemoteException {
		super();
		this.maxThreads = maxThreads;

		Registry reg = LocateRegistry.getRegistry(host, port);
		try {
			this.master = (Master) reg.lookup("MyMaster");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		System.out.println("Registering worker with " + maxThreads + " max. Threads.");

		master.registerWorker(this);
	}

	@Override
	public <Argument, Result> void start(Task<Argument, Result> t, Pool<Argument> argpool, Pool<Result> respool)
			throws RemoteException {
		System.out.println("Starting worker");
		new Thread(new SepTask<Argument, Result>(t, argpool, respool)).start();

	}

	/*
	 * helping class, to separate the tasks and forward it to the threads
	 * exercise wants to cycle through start as fast as possible, thats why we
	 * need a new class, to start a thread for that
	 */
	class SepTask<Argument, Result> implements Runnable {

		Pool<Argument> argpool;
		Pool<Result> respool;
		Task<Argument, Result> t;
		Argument arg;

		public SepTask(Task<Argument, Result> t, Pool<Argument> argpool, Pool<Result> respool) {
			this.t = t;
			this.argpool = argpool;
			this.respool = respool;
		}

		@Override
		public void run() {
			while (seperate) {
				// get argument
				try {
					System.out.println("Getting argument from argument pool");
					arg = argpool.get();

					// break on termination condition -> argpool is empty
					if (arg == null) {
						System.out.println("Argument pool is empty.");
						seperate = false;
						break;
					} else {
						while (countThreads == maxThreads) {
							// Waiting for free Threads
						}
						// start threads, as long as we have space for them
						if (countThreads < maxThreads) {
							// create new threads
							countThreads++;
							System.out.println("Creating new thread with argument. Number of Threads: " + countThreads);
							new Thread(new ExeTask<Argument, Result>(t, arg, respool)).start();
						}

					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// helping class, to start the tasks in a thread
	class ExeTask<Argument, Result> implements Runnable {

		Task<Argument, Result> t;
		Argument arg;
		Pool<Result> respool;

		public ExeTask(Task<Argument, Result> t, Argument arg, Pool<Result> respool) {
			this.t = t;
			this.arg = arg;
			this.respool = respool;
		}

		@Override
		public void run() {
			try {
				respool.put(t.exec(arg));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			countThreads--;
		}

	}

	public int getMaxThreads() {
		return maxThreads;
	}
}
