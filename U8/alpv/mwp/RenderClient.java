package alpv.mwp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import alpv.mwp.ray.GUI;

public class RenderClient {

	Server server;

	public RenderClient(String host, int port) {
		try {

			Registry reg = LocateRegistry.getRegistry(host, port);

			this.server = (Server) reg.lookup("MyMaster");
			System.out.println("Client starting job.");
			Job<Stripe, FileChunk, File> newJob = new RenderJob(); //When fails swap File for Array of byte[] and build in Client
			
			RemoteFuture<File> rf =  server.doJob(newJob);
			
			//Creates a temp file and updates it when RemoteFuture returns a new Strip
			while(!rf.isFinished()){
			
				if (rf.get()!= null) { //TODO Maybe does not work like this
					System.out.println("Result update, got new stripe");			
		        	//GUI.display(rf.get().getCanonicalPath());
		        	
				}
			}
			System.out.println("Got the final result");
			GUI.display(rf.get().getCanonicalPath());
			
			//TODO Display final result

		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("ERROR: Server not bound");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
