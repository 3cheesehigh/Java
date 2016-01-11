package alpv.mwp.ray;


import java.io.ByteArrayOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Comparator;

public interface Stripe extends Remote{
	
    int getStripeStart() throws RemoteException;
    
    int getStripeEnd() throws RemoteException;
 
}
