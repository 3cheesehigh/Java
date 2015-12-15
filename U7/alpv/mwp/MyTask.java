package alpv.mwp;

public class MyTask implements Task<textChunk, Integer> {

	private static final long serialVersionUID = 1L;
	
    
    @Override
	public Integer exec(textChunk a) {
		
		String searchedString = a.searchedString;
		String[] strings = a.strings;
		
		int l=0;
		for (int i = 0; i < strings.length; i++) {
			if (searchedString.equals(strings[i])){
				l++;
			}
				
		}
		
		return l;
	}
	




}
