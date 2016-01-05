package alpv.mwp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MyRemoteFuture extends UnicastRemoteObject implements RemoteFuture<Integer> {


	private static final long serialVersionUID = 1L;
	
	private boolean isFinished;
	private int result;
	
	public MyRemoteFuture() throws RemoteException{
		isFinished =false;
	}
	
	
	public boolean isFinished() {
		return isFinished;
	}
	

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}


	public void setResult(int result) {
		this.result = result;
	}
	

	@Override
	public Integer get() throws RemoteException {
		return result;
	}



	


	
}

