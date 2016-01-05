package alpv.mwp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TextChunk extends Remote{

	String getSearchedString() throws RemoteException;

	void setSearchedString(String searchedString)throws RemoteException;

	String[] getStrings() throws RemoteException;

	void setStrings(String[] strings) throws RemoteException;

}