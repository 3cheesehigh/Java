package alpv.mwp;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyMaster extends UnicastRemoteObject implements Server, Master {

	LinkedList<Worker> workerList = new LinkedList<Worker>();
	Queue<queuedJob> jobQueue = new LinkedBlockingQueue<>();
	// ArrayList<Worker> workerList = new ArrayList();

	// default ID from Eclipse
	private static final long serialVersionUID = 1L;
	
	//Constructor
	protected MyMaster(int port) throws RemoteException {
		Registry reg = LocateRegistry.createRegistry(port);
		reg.rebind("MyMaster", this);

		new Thread(new JobHandler()).start();
		System.out.println("Server started");
	}

	// REGISTER
	public void registerWorker(Worker w) throws RemoteException {
		synchronized (workerList) {
			System.out.println("Registering worker ");
			workerList.add(w);
			//TODO Implement looking for a jaaab
		}
	}

	// UNREGISTER
	public void unregisterWorker(Worker w) throws RemoteException {
		synchronized (workerList) {
			System.out.println("Unegistering worker ");
			workerList.remove(w);
		}
	}

	//adds no job to the Queue. Calculates the split of arguments and returns the remoteFuture Object to the Client.
	@Override
	public <Argument, Result, ReturnObject> RemoteFuture<ReturnObject> doJob(Job<Argument, Result, ReturnObject> j)
			throws RemoteException {

		// create two pools
		Pool<Argument> argpool = new MyPool<>();
		Pool<Result> respool = new MyPool<>();
		synchronized (workerList) {
			j.split(argpool, workerList.size());			
		}
		// adds job to queue 
		this.jobQueue.add(new queuedJob(j, respool, argpool));
		
		// Returns the Remote Future Object to the Client
		RemoteFuture<ReturnObject> rf = j.getFuture();
		return rf;

		


	}

	//Thread will look for jobs in queue and starts workers with corresponding pools
	public class JobHandler implements Runnable {
		
		@Override
		public void run() {
			System.out.println("JobHandler: started");
			while(true){				
				try {
				if(!(jobQueue.isEmpty())){
					System.out.println("JobHandler: Getting next Job");
					queuedJob nextJob = jobQueue.poll();
					Job  job = nextJob.getJ();
					Pool respool   = nextJob.getRespool();
					Pool argpool = nextJob.getArgpool();
					
					// Start the workers
					while (workerList.isEmpty()) {
						//wait
						Thread.sleep(0);
					}
					synchronized (workerList) {
						System.out.println("JobHandler: Starting Workers");
						for (Worker w : workerList) {							
								w.start(job.getTask(),argpool , respool);
						}
					}
					System.out.println("JobHandler: Merging");						
					job.merge(respool);							//Remote Future will be updated inside merge defined by the client							
					System.out.println("JobHandler: Merging end");						

				}
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}

	}
	
	
	//Object to save the queued jobs and their split arguments/result pools
	
	public class queuedJob {
		
		private Job j;
		private Pool respool;
		private Pool argpool;


		public queuedJob(Job j, Pool  respool, Pool argpool) {
			super();
			this.j = j;
			this.respool = respool;
			this.argpool = argpool;
		}
		
		public Job getJ() {
			return j;
		}

		public void setJ(Job j) {
			this.j = j;
		}

		public Pool getRespool() {
			return respool;
		}

		public void setRespool(Pool respool) {
			this.respool = respool;
		}

		public Pool getArgpool() {
			return argpool;
		}

		public void setArgpool(Pool argpool) {
			this.argpool = argpool;
		}


	}

}
