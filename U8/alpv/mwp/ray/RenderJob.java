package alpv.mwp.ray;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;

public class RenderJob implements Job<Stripe, FileChunk, ArrayList<FileChunk>> {

	private Task<Stripe, FileChunk> task;
	private RenderRemoteFuture rf;
	private final static int TOTALIMAGEHEIGHT = 800;
	private final static int ARGUMENTEPERWORKER = 10;
	private final static int TOTALIMAGEWIDTH = 800;
	private int stripeHeight;
	private int argSize;
	private ArrayList<FileChunk> fileChunkList = new ArrayList();

	public RenderJob() {
		this.task = new RenderTask();
	}

	@Override
	public Task<Stripe, FileChunk> getTask() {
		return task;
	}

	@Override
	public RemoteFuture<ArrayList<FileChunk>> getFuture() {
		try {
			this.rf = new RenderRemoteFuture();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return rf;
	}

	@Override
	public void split(Pool<Stripe> argPool, int workerCount) {

		// Divide imagine in equal parts in depending on the number of workers
		if (workerCount > 0) {
			stripeHeight = TOTALIMAGEHEIGHT / (workerCount * ARGUMENTEPERWORKER);
		} else {
			stripeHeight = TOTALIMAGEHEIGHT / (1 * ARGUMENTEPERWORKER);
		}

		System.out.println("Image height: " + TOTALIMAGEHEIGHT);
		System.out.println("Stripe height per argument: ~" + stripeHeight);

		// Fill the pool
		for (int i = 0; i < TOTALIMAGEHEIGHT; i += stripeHeight) {
			try {
				if (i == 0) {
					Stripe stripe = new RenderStripe(i, i + stripeHeight); // First
																		// stripe
					argPool.put(stripe);
				} else {
					Stripe stripe = new RenderStripe(i + 1, i + stripeHeight); // Every
																			// stripe
					argPool.put(stripe); // else

				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				argSize = argPool.size();
				System.out.println("Number of arguments: " + argSize);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@Override
	public void merge(Pool<FileChunk> resPool) {
		try {
			while (!(rf.isFinished())) {
				// create a new image and write it to file
				if (argSize > 0 && resPool.size() > 0) {
					fileChunkList.add(resPool.get());
					rf.setList(fileChunkList);
					argSize--;
				} else if (0 == argSize)
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					rf.setFinished(true);
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
