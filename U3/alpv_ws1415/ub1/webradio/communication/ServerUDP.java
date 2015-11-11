package alpv_ws1415.ub1.webradio.communication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.protobuf.ByteString;

import alpv_ws1415.ub1.webradio.communication.ServerTCP.ClientHandler;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.AudioDataMessage;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.ClientChatMessage;

public class ServerUDP implements Server {

	private DatagramSocket serverSocket;
	private int port;
	private Map<InetAddress, Integer> clients = Collections.synchronizedMap(new HashMap<InetAddress,Integer>()) ;
	private ArrayList<ClientChatMessage> allMessages = new ArrayList<>();

	private ExecutorService pool;
	final int poolSize = 8;

	private volatile boolean serverRunning;
	private volatile boolean streamPlayerRunning = true;
	private volatile boolean changeTitle = false;
	private boolean musicIsPlaying = false;
	
	private String path = ServerTCP.class.getResource("/Res/Wav/music.wav").getPath(); 


	public ServerUDP(int port) {
		this.port = port;
		this.pool = Executors.newFixedThreadPool(poolSize);
	}

	@Override
	public void run() {
		try {
			serverRunning = true;
			System.out.println("Server Running");
			// wartet auf ne testnachricht vom client und gibt dann die adresse
			// aus.
			serverSocket = new DatagramSocket(port);
			
			
			pool.execute(new ClientListener());
			pool.execute(new StreamPlayer());
			
			
			while (serverRunning) {
				byte[] buffer = new byte[4096];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				serverSocket.receive(packet);
				System.out.println("Client found\nIP: " + packet.getAddress() + "\nPort: " + packet.getPort() + "\nData: "
						+ new String(packet.getData()));
				clients.put(packet.getAddress(), packet.getPort()); //TODO könnte sein das man hier sync braucht
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void playSong(String path) throws MalformedURLException, UnsupportedAudioFileException, IOException {
		// TODO Auto-generated method stub

	}
	
	private class ClientListener implements Runnable{
		
		public ClientListener() {
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	private class StreamPlayer implements Runnable {
		int nRead = 0;

		public void terminate() {
			streamPlayerRunning = false;
			musicIsPlaying = false;
		}

		@Override
		public void run() {
			while (streamPlayerRunning) {
				changeTitle = false;
				byte[] buffer = new byte[4096];
				File file = new File(path);
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
					while ((nRead = fis.read(buffer, 0, 4096)) >= 0) {
						if (changeTitle) {
							fis.close();
						} else {
							DatagramSocket serverOut = new DatagramSocket();
							synchronized (clients) {
								for (Map.Entry<InetAddress, Integer> client : clients.entrySet() ) {
									try {
										//TODO Kann sein das wir hier nen Iterator brauchen
										ByteArrayOutputStream serverOutput = new ByteArrayOutputStream(4096);
										//Sendet Daten an Client
										AudioDataMessage audioData = AudioDataMessage.newBuilder().setDatapack(ByteString.copyFrom(buffer)).build();
										Data dataPack = Data.newBuilder().setAudioData(audioData).build();
										dataPack.writeDelimitedTo(serverOutput);
										byte[] serializedProto = serverOutput.toByteArray();
										DatagramPacket serverPackage = new DatagramPacket(serializedProto,
												serializedProto.length,
												client.getKey(),
												client.getValue());
										serverOut.send(serverPackage);
									} catch (Exception e) {
										System.out.println("Unit lost");
									}
						
							}
						}
					}

				} catch (FileNotFoundException e) {
					System.out.printf("Couldn't find  %s .\n", path);
				} catch (IOException e) {
					System.out.printf("IO Exception found!\n");
				}
				try {
					fis.close();
				} catch (IOException e) {
					System.out.printf("Error on closing file!\n");
				}

			}
		}

	}



}
