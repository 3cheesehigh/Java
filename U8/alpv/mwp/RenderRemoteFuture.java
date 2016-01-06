package alpv.mwp;

import java.io.File;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RenderRemoteFuture extends UnicastRemoteObject implements RemoteFuture<ArrayList<FileChunk>> {

	private static final long serialVersionUID = 1L;

	private boolean isFinished;
	private ArrayList<FileChunk> result;

	protected RenderRemoteFuture() throws RemoteException {
		isFinished = false;
	}

	@Override
	public ArrayList<FileChunk> get() throws RemoteException {
		return result;
	}

	@Override
	public boolean isFinished() throws RemoteException {
		return isFinished;
	}

	public void setList(ArrayList<FileChunk> fileChunkList) throws RemoteException {
		this.result = fileChunkList;
	}

	public void setFinished(boolean b) throws RemoteException{
		this.isFinished = b;
	}

}
