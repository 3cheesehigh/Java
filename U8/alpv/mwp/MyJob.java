package alpv.mwp;

import java.rmi.RemoteException;
import java.util.Arrays;

public class MyJob implements Job<TextChunk, Integer,Integer > {

	
	private static final long serialVersionUID = 1L;
	private String text;
	private Task<TextChunk, Integer> task;
	private final static int ARGUMENTEPERWORKER = 10;
	private String searchedString;
	private int argSize;
	private MyRemoteFuture rf;
	
	//Constructor
	public MyJob(String text,String searchedString, Task<TextChunk, Integer> task){
		this.text=text;
		this.task = task;
		this.searchedString = searchedString;		
	}
	
	@Override
	public Task<TextChunk, Integer> getTask() {
		return task;
	}

	@Override
	public RemoteFuture<Integer> getFuture() {
		try {
			this.rf =  new MyRemoteFuture();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return rf;
	}

	@Override
	public void split(Pool<TextChunk> argPool, int workerCount) {
	
		String[] textList = text.split(" ");
		
		int number;
		if (workerCount > 0) {
			number = textList.length / (workerCount * ARGUMENTEPERWORKER);
		}
		else{
			number = textList.length / (1 * ARGUMENTEPERWORKER);
		}
		System.out.println("Text Length: " + textList.length);
		System.out.println("Number of Strings in one argument: ~" + number);
		
		for (int i = 0; i < textList.length; i += number) {
			// Create arguments
			TextChunk arg;
			try {
			if (i+ number > textList.length){
				arg = new MyTextChunk(searchedString, Arrays.copyOfRange(textList, i, textList.length));
				argPool.put(arg);
			}
			else{
				arg = new MyTextChunk(searchedString, Arrays.copyOfRange(textList, i, number + i));	
				argPool.put(arg);
				}
			
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			
			argSize = argPool.size();
			System.out.println("Number of arguments: "+argSize);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	@Override
	public void merge(Pool<Integer> resPool) {
		try {
			//isFinished is needed for the Client to differ the final result and a partial result
			while(!(rf.isFinished())){				
				if( argSize > 0){             //Updates partly finished result					
					for (int i = 0 ; i < resPool.size();i++){
						Integer next = resPool.get();
						if (!next.equals(null)){
							rf.setResult(rf.get() + next);
							
							argSize--;
						}			
					}
				}
				else if (0 == argSize)		  //Updates final result status		
					rf.setFinished(true);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
