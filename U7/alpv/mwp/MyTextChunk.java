package alpv.mwp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MyTextChunk extends UnicastRemoteObject implements TextChunk{

	

	private static final long serialVersionUID = 1L;
	private String searchedString;
	private String[] strings;
	
	
	public MyTextChunk(String searchedString, String[] strings) throws RemoteException{
		this.searchedString = searchedString;
		this.strings = strings;
	}


	/* (non-Javadoc)
	 * @see alpv.mwp.TextChunk#getSearchedString()
	 */
	@Override
	public String getSearchedString() {
		return searchedString;
	}


	/* (non-Javadoc)
	 * @see alpv.mwp.TextChunk#setSearchedString(java.lang.String)
	 */
	@Override
	public void setSearchedString(String searchedString) {
		this.searchedString = searchedString;
	}


	/* (non-Javadoc)
	 * @see alpv.mwp.TextChunk#getStrings()
	 */
	@Override
	public String[] getStrings() {
		return strings;
	}


	/* (non-Javadoc)
	 * @see alpv.mwp.TextChunk#setStrings(java.lang.String[])
	 */
	@Override
	public void setStrings(String[] strings) {
		this.strings = strings;
	}
	
	
}
