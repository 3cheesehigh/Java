package alpv.mwp;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;

import alpv.mwp.ray.RendererFrontend;

public class RenderTask implements Task<Stripe,FileChunk> {


	private static final long serialVersionUID = 1L;

	@Override
	public FileChunk exec(Stripe renderStripe) {
        ByteArrayOutputStream part = new ByteArrayOutputStream();
        
        final RendererFrontend rendfe = new RendererFrontend(part);

        try {
			rendfe.setWindowStrip(renderStripe.getStripeStart(),renderStripe.getStripeEnd());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        rendfe.render();
        
        
        FileChunk result = null;
		try {
			result = new RenderFileChunk(part.toByteArray(),renderStripe.getStripeStart(),renderStripe.getStripeEnd());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
