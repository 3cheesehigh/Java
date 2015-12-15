package alpv.mwp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class textChunk extends UnicastRemoteObject{

	

	private static final long serialVersionUID = 1L;
	String searchedString;
	String[] strings;
	
	
	public textChunk(String searchedString, String[] strings) throws RemoteException{
		this.searchedString = searchedString;
		this.strings = strings;
	}
}
