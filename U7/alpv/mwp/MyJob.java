package alpv.mwp;

import java.rmi.RemoteException;
import java.util.Arrays;

public class MyJob implements Job<textChunk, Integer, Integer> {

	
	private static final long serialVersionUID = 1L;
	private String text;
	private Task task;
	private final static int ARGUMENTEPERWORKER = 3;
	private String searchedString;
	private int argSize;
	private MyRemoteFuture rf;
	
	//Constructor
	public MyJob(String text,String searchedString, Task task){
		this.text=text;
		this.task = task;
		this.searchedString = searchedString;		
	}
	
	@Override
	public Task<textChunk, Integer> getTask() {
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
	public void split(Pool<textChunk> argPool, int workerCount) {
	
		String[] textList = text.split(" ");
		int number = textList.length / workerCount * ARGUMENTEPERWORKER;
		
		for (int i = 0; i < textList.length; i += number) {
			// Create arguments
			textChunk arg;
			try {
			if (i+ number > textList.length){
				arg = new textChunk(searchedString, Arrays.copyOfRange(textList, i, textList.length));
			}
			else
				arg = new textChunk(searchedString, Arrays.copyOfRange(textList, i, number + i));	
			
				argPool.put(arg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			argSize = argPool.size();
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
