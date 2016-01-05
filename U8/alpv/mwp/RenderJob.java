package alpv.mwp;

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

public class RenderJob implements Job<Stripe, FileChunk, File> {

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
	public RemoteFuture<File> getFuture() {
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
					Stripe stripe = new MyStripe(i, i + stripeHeight); // First
																		// stripe
					argPool.put(stripe);
				} else {
					Stripe stripe = new MyStripe(i + 1, i + stripeHeight); // Every
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
		// TODO Auto-generated method stub
		/*
		 * try{ while (!(rf.isFinished())) { ByteArrayOutputStream result = new
		 * ByteArrayOutputStream(); if (argSize > 0 && resPool.size()> 0) {
		 * 
		 * resultStripes.add(resPool.get());
		 * 
		 * // TODO: könnte klappen muss aber nicht; stripes //
		 * absteigend-sortieren resultStripes.sort(new Comparator<Stripe>() {
		 * 
		 * @Override public int compare(Stripe o1, Stripe o2) { try {
		 * if(o1.getAbsoluteHeight()< o2.getAbsoluteHeight()){ return -1; }else
		 * if(o1.getAbsoluteHeight()> o2.getAbsoluteHeight()){ return 1; } }
		 * catch (RemoteException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } return 0; } });
		 * 
		 * int heightSum = 0; for (Stripe stripe : resultStripes) { heightSum +=
		 * stripe.getImageHeigth(); }
		 * 
		 * String hdr = "RGB\n" + resultStripes.get(0).getImageWidth()+" "+
		 * (heightSum) + " 8 8 8\n"; rf.setHdr(hdr);
		 * 
		 * 
		 * for (Stripe stripe : resultStripes){
		 * stripe.getByteArrayOutputStream().writeTo(result);; }
		 * rf.setBytes(result.toByteArray());
		 * 
		 * argSize--; } else if (0 == argSize) // Updates final result status
		 * and sets RemoteFuture rf.setFinished(true); }
		 */
		try {
			while (!(rf.isFinished())) {
				File outF = null;
				// create temporary file
				try {
					outF = File.createTempFile("alpiv", ".pix");
				} catch (IOException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Cannot create a temporary file.");
				}
				// create outputstream to temporary file
				OutputStream outs = null;
				try {
					outs = new FileOutputStream(outF);
				} catch (IOException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Cannot open the output file " + outF);
				}
				// create a new image and write it to file
				if (argSize > 0 && resPool.size() > 0) {

					fileChunkList.add(resPool.get());

					// TODO: könnte klappen muss aber nicht; stripes
					// absteigend-sortieren
					fileChunkList.sort(new Comparator<FileChunk>() {
						@Override
						public int compare(FileChunk o1, FileChunk o2) {
							try {
								if (o1.getPartStart() > o2.getPartStart()) {
									return -1;
								} else if (o1.getPartStart() < o2.getPartStart()) {
									return 1;
								}
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return 0;
						}
					});

					// Adds together the height of the partial result for the
					// GUI
					int heightSum = 0;
					for (FileChunk fileChunk : fileChunkList) {
						heightSum += stripeHeight;
					}

					String hdr = "RGB\n" + TOTALIMAGEWIDTH + " " + (heightSum) + " 8 8 8\n";

					BufferedWriter wOut = new BufferedWriter(new OutputStreamWriter(outs));
					wOut.write(hdr, 0, hdr.length());
					wOut.flush();

					for (FileChunk fileChunk : fileChunkList) {
						outs.write(fileChunk.getFilePart());
					}

					rf.setFile(outF);
					argSize--;
				} else if (0 == argSize) // Updates final result status and sets
											// RemoteFuture
					rf.setFinished(true);
				outs.close();
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
