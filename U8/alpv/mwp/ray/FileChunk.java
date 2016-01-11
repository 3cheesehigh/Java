package alpv.mwp.ray;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileChunk extends Remote {

	byte[] getFilePart() throws RemoteException;
	
	int getPartStart() throws RemoteException;
	
	int getPartEnd() throws RemoteException;
}
