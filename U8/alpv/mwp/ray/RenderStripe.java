package alpv.mwp.ray;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RenderStripe extends UnicastRemoteObject implements Stripe {
	

	private static final long serialVersionUID = 1L;

	private int stripeStart;
	private int stripeEnd;
	
	
	
	public RenderStripe(int stripeStart,int stripeEnd) throws RemoteException{
		this.stripeStart = stripeStart;
		this.stripeEnd = stripeEnd;
	}



	@Override
	public int getStripeStart() throws RemoteException {
		return stripeStart;
	}



	@Override
	public int getStripeEnd() throws RemoteException {
		return stripeEnd;
	}
	







}
