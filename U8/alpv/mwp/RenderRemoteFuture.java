package alpv.mwp;

import java.io.File;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RenderRemoteFuture extends UnicastRemoteObject implements RemoteFuture<File> {

	private static final long serialVersionUID = 1L;

	private boolean isFinished;
	private File result;

	protected RenderRemoteFuture() throws RemoteException {
		isFinished = false;
	}

	@Override
	public File get() throws RemoteException {
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public boolean isFinished() throws RemoteException {
		// TODO Auto-generated method stub
		return isFinished;
	}

	public void setFile(File result) throws RemoteException {
		this.result = result;
	}

	public void setFinished(boolean b) throws RemoteException{
		this.isFinished = b;
	}

}
