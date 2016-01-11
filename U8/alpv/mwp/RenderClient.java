package alpv.mwp;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Comparator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import alpv.mwp.ray.GUI;

public class RenderClient {

	Server server;
	private final static int TOTALIMAGEWIDTH = 800;
	private ArrayList<FileChunk> fileChunkList = new ArrayList<>();

	public RenderClient(String host, int port) {
		try {

			Registry reg = LocateRegistry.getRegistry(host, port);

			this.server = (Server) reg.lookup("MyMaster");
			System.out.println("Client starting job.");
			Job<Stripe, FileChunk, ArrayList<FileChunk>> newJob = new RenderJob(); // When fails swap File for Array of byte[] and build in Client

			RemoteFuture<ArrayList<FileChunk>> rf = server.doJob(newJob);
			PlayModemSound play = new PlayModemSound();
			Thread playThread = new Thread(play);
			playThread.start();

			while (!rf.isFinished()) {
				// Creates a temp file and updates it when RemoteFuture returns
				// a new Strip
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

				if (rf.get() != null) { 
					System.out.println("BEEP BOOP BEEBEE BOOP KRRRRRR");

					fileChunkList = rf.get();
				}
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

					heightSum += Math.abs(fileChunk.getPartEnd() - fileChunk.getPartStart());
				}

				String hdr = "RGB\n" + TOTALIMAGEWIDTH + " " + (heightSum) + " 8 8 8\n";

				BufferedWriter wOut = new BufferedWriter(new OutputStreamWriter(outs));
				wOut.write(hdr, 0, hdr.length());
				wOut.flush();

				for (FileChunk fileChunk : fileChunkList) {
					outs.write(fileChunk.getFilePart());
				}

				outs.close();
				GUI.display(outF.getCanonicalPath());

			}
			System.out.println("Got the final result");
			play.stopping();
			try {
				playThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (

		RemoteException e1)

		{
			e1.printStackTrace();
		} catch (

		NotBoundException e)

		{
			System.out.println("ERROR: Server not bound");
		} catch (

		IOException e)

		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class PlayModemSound implements Runnable {

		private volatile boolean running = true;

		public void run() {
			
				try {
					AudioInputStream stream;
					AudioFormat format;
					DataLine.Info info;
					Clip clip;

					stream = AudioSystem.getAudioInputStream(new BufferedInputStream(ClassLoader.getSystemResourceAsStream("wav/loopsound.wav")));
					format = stream.getFormat();
					info = new DataLine.Info(Clip.class, format);
					clip = (Clip) AudioSystem.getLine(info);
					clip.open(stream);
					clip.loop((Clip.LOOP_CONTINUOUSLY));
					clip.start();
					while(running){
						Thread.sleep(0);
					}
					clip.stop();
					clip.drain();
					clip.close();
				} catch (Exception e) {
					// whatevers
				}

			
		}

		public void stopping() {
			this.running = false;
			
		}
		
	}


}
