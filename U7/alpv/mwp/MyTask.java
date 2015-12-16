package alpv.mwp;

import java.rmi.RemoteException;

public class MyTask implements Task<TextChunk, Integer> {

	private static final long serialVersionUID = 1L;
		
    @Override
	public Integer exec(TextChunk a) {
		String searchedString = null;
		String[] strings = null;
		try {
			searchedString = a.getSearchedString();
			strings = a.getStrings();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		int l=0;
		for (int i = 0; i < strings.length; i++) {
			if (searchedString.equals(strings[i])){
				l++;
			}			
		}
		return new Integer(l);
	}
	

}
