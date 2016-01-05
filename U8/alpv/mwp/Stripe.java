package alpv.mwp;


import java.io.ByteArrayOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Comparator;

import alpv.mwp.ray.RendererFrontend;

public interface Stripe extends Remote{
	
    int getStripeStart() throws RemoteException;
    
    int getStripeEnd() throws RemoteException;
 
}
