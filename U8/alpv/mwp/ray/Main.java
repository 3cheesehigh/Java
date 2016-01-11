package alpv.mwp.ray;

import java.rmi.RemoteException;

public class Main {

	private static final String USAGE = "usage: java -jar UB%%X_NAMEN master PORT%n"
			+ "			(to start a master-server) %n"
			+ "or:		java -jar UB%%X_NAMEN worker SERVERIDRESS SERVERPORT MAXTHREADS%n"
			+ "			(to start a worker) %n" + "or 		java -jar UB%%X_NAMEN client ERVERIDRESS SERVERPORT%n"
			+ "			(to start a client) %n";

	/*
	 * Starts a worker/server/client according to the given arguments.
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		try {
			int i = 0;

			if (args[i].equals("master")) {

				new MyMaster(Integer.parseInt(args[i + 1]));
				
			}
			else if(args[i].equals("worker")) {
				new MyWorker(args[i+1],Integer.parseInt(args[i + 2]),Integer.parseInt(args[i + 3]));
				
			}
			else if(args[i].equals("client")) {
				new RenderClient(args[i+1],Integer.parseInt(args[i + 2]));
				
			}
		} catch (IllegalArgumentException|ArrayIndexOutOfBoundsException e) {
			System.err.println(USAGE);
		} catch (RemoteException e){
			e.printStackTrace();
		}
		
	}
}
