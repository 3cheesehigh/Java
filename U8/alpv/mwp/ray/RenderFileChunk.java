package alpv.mwp.ray;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RenderFileChunk extends UnicastRemoteObject implements FileChunk {


	private static final long serialVersionUID = 1L;
	private byte[] filePart;
	private int partStart;
	private int partEnd;

	public RenderFileChunk(byte[] filePart, int partStart,int partEnd) throws RemoteException {
		this.filePart = filePart;
		this.partStart = partStart;
		this.partEnd = partEnd;
	}

	@Override
	public byte[] getFilePart()  {
		return filePart;
	}

	@Override
	public int getPartStart() {
		return partStart;
	}
	public int getPartEnd() {
		return partEnd;
	}





}
