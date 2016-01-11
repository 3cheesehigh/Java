package alpv.mwp.ray;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class MyWorker extends UnicastRemoteObject implements Worker {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Master master = null;
	private int maxThreads;
	private int countThreads;
	

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
		new Thread(new Console(this)).start();
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
			boolean seperate = true;
			while (seperate) {
				// get argument
				try {
					System.out.println("Getting argument from argument pool, poolsize: " + argpool.size());
					arg = argpool.get();

					// break on termination condition -> argpool is empty
					if (arg == null) {
						System.out.println("Argument pool is empty.");
						seperate = false;
						break;
					} else {
						while (countThreads == maxThreads) {
							// Waiting for free Threads
							Thread.sleep(0);
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
					e.printStackTrace();
				} catch (InterruptedException e) {
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
	// INPUT from console
	//=======================================================
	class Console implements Runnable {
		Worker w;
		
		public Console(Worker w){
		this.w = w;	
		}
		
		public void run() {

			boolean running = true;
			Scanner scanner = new Scanner(System.in);
			while (running) {
				System.out.println(">>");
				String input = scanner.nextLine();

				
				//UNREGISTER
				if (input.startsWith("unregister")) {
					System.out.println("<< Unregistering...");
					try {
						master.unregisterWorker(w);
					} catch (RemoteException e) {
						System.out.println("<< ERROR: While unregistering !");
						e.printStackTrace();
					}
				}
				
				//REGISTER
				if (input.startsWith("register")) {
					System.out.println("<< Registering...");
					try {
						master.registerWorker(w);
					} catch (RemoteException e) {
						System.out.println("<< ERROR: While unregistering !");
						e.printStackTrace();
					}
				}
				
				//END
				if (input.startsWith("end")) {
					System.out.println("Have a nice Day!");
					try {
						master.unregisterWorker(w);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.exit(0);
				}

			}
		}
	}

}
