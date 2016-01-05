package alpv.mwp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MyFileChunk extends UnicastRemoteObject implements FileChunk {


	private static final long serialVersionUID = 1L;
	byte[] filePart;
	int partStart;

	public MyFileChunk(byte[] filePart, int partStart) throws RemoteException {
		this.filePart = filePart;
		this.partStart = partStart;
	}

	@Override
	public byte[] getFilePart()  {
		return filePart;
	}

	@Override
	public int getPartStart() {
		return partStart;
	}

}
