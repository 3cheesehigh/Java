package alpv_ws1415.ub1.webradio.communication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.protobuf.ByteString;

import alpv_ws1415.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1415.ub1.webradio.ui.ServerGUI;
import alpv_ws1415.ub1.webradio.protobuf.myProto.*;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.AudioDataMessage;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.AudioFormatMessage;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.ClientChatMessage;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.HelloMessage;

public class ServerTCP implements Server {
	/*
	 * 
	 * inner Classes which implements Runnable:
	 *
	 * ClientHandler - Handles incoming Clients and sets them up to listen to
	 * music
	 * 
	 * KeyListener - Listens on Console input to exit,change and start playing
	 * music
	 * 
	 * StreamPlayer - stream music to all Clients added to the clients
	 * collection
	 *
	 */

	// Strings
	private String hello = "Hello World";
	private String ataken = "That name has already been taken. Please choose another";
	private String path = ServerTCP.class.getResource("/Res/Wav/music.wav").getPath(); 
	private String USAGE = "USAGE: %n%nType 'exit' to shutdown server,%n" + ", 'playSong' to start musicstream%n"
			+ ", 'currentSong' for current playing song%n" + ", 'queueSong' to queue a Song%n"
			+ "or 'setSongPath' to change song path";
	private ArrayList<String> playList = new ArrayList<>();

	// Member variables
	private int port;
	private ServerSocket serverSocket;
	private AudioPlayer audioPlayer;
	private AudioInputStream ais;
	private volatile Collection<ClientHandler> clients = new ArrayList<ClientHandler>();
	
	
	private Thread streamPlayerThread = null;
	private StreamPlayer streamPlayer = null;
	private ExecutorService pool;
	final int poolSize = 8;
	private volatile boolean serverRunning = true;
	private volatile boolean streamPlayerRunning = true;
	private volatile boolean changeTitle = false;
	private boolean musicIsPlaying = false;
	// for UI
	private ServerGUI serverGUI;

	// constructor
	public ServerTCP(int port) {
		this.port = port;
		this.pool = Executors.newFixedThreadPool(poolSize);
	}

	// server run
	public void run() {
		// New Server socket
		try {
			startKeyListener();
			
			startMessenger();
			
			System.out.printf("Starting Server\n");
			serverSocket = new ServerSocket(port);
			// Waiting for new client and be blocked while waiting
			while (serverRunning) {
				System.out.printf("Waiting for new Clients to Connect to Server\n");
				Socket client = serverSocket.accept();
				System.out.println("New Client, passing to Handler");
				//Username wird überpfüft und client eingebunden
				integrateClient(client);
				
			
			}
		} catch (Exception e) {
			close();
		} finally {
			pool.shutdown();
			try {
				pool.awaitTermination(4L, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				System.out.println("Interrupt!");
				e.printStackTrace();
			}
		}

	}

	private void integrateClient(Socket client) throws IOException{
		String clientName;
		boolean taken = false;
		InputStream serverIn;
		serverIn = client.getInputStream();
		clientName = ClientChatMessage.parseDelimitedFrom(serverIn).getUser();
		System.out.println("User: "+clientName +" is online");
		synchronized (clients) {
			for (Iterator<ClientHandler> iter = clients.iterator(); iter.hasNext();) {
				ClientHandler listclient = (ClientHandler) iter.next();
				if (clientName.equals(listclient.clientName)){
					taken = true;
					startMessage(client, ataken);
				}
			}
		}
		if(!taken){
			pool.execute(new ClientHandler(client, clientName));
		}


	}
	private void startKeyListener() {
		try {
			Thread listener = new Thread(new KeyListener());
			listener.start();
		} catch (Exception e) {
			System.out.println("KeyListener is Pudding");
		}
	}
	private void startMessenger(){
		Thread messenger = new Thread(new SeverMessenger());
		messenger.start();
	}

	// close Server socket
	@Override
	public void close() {
		serverRunning = false;

	}

	public void closeOnAllClients() throws IOException {
		for (ClientHandler ch : clients) {
			ch.clientSoc.close();
		}
	}

	/*
	 * ---------------------------------------------------------------------
	 * ---- --------------------------------------------------
	 *
	 * sets audioplayer and stuff
	 *
	 * ---------------------------------------------------------------------
	 * ---- --------------------------------------------------
	 */
	public void setSongPath(String string) {
		path = string;
	}

	public String getSongPath() {
		return path;
	}

	public void playSong(String path) throws MalformedURLException, UnsupportedAudioFileException, IOException {
		if (!playList.isEmpty()) {
			setSongPath(playList.remove(0));
		}
		if (musicIsPlaying) {
			changeTitle = true;
		} else {
			musicIsPlaying = true;
			ais = AudioPlayer.getAudioInputStream(path);
			setAudioPlayer(new AudioPlayer(ais.getFormat()));

			streamPlayer = new StreamPlayer();
			pool.execute(streamPlayer);
		}

	}

	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}

	public void setAudioPlayer(AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	// Send some text
	private void startMessage(Socket client, String message) throws IOException {
		// Send Hello World to client
				OutputStream output = client.getOutputStream();
				HelloMessage hellobuild = HelloMessage.newBuilder().setHelloMsg(message).build();
		        hellobuild.writeDelimitedTo(output);
	}

	/*
	 * -------------------------------------------------------------------------
	 * -------------------------------------------------- Handles Clients
	 *
	 * -------------------------------------------------------------------------
	 * --------------------------------------------------
	 */
	class ClientHandler implements Runnable {

		private Socket clientSoc = null;
		private String clientName;
		public ClientHandler() {
		}

		public ClientHandler(Socket socket, String name) {
			clientSoc = socket;
			clientName = name;
		}

		@Override
		// ClientHandler run
		public void run() {
			try {
				// be polite and say hello first
				startMessage(clientSoc, hello);
				// Get a new OutputStream
				OutputStream osw = clientSoc.getOutputStream();
				// Send the client the audioFormat as a String;
				AudioFormat aF = AudioPlayer.getAudioInputStream(path).getFormat();
				// build the Protobuf Message
				AudioFormatMessage format = AudioFormatMessage.newBuilder()
						.setSampleRate(aF.getSampleRate())
						.setSampleSizeInBits(aF.getSampleSizeInBits())
						.setChannels(aF.getChannels())
						.setProperty(true)
						.setBigEndian(aF.isBigEndian())
						.build();
				// Send Client the format
		        format.writeDelimitedTo(osw);

				// Add this instance to collection
				synchronized (clients) {
					clients.add(this);
				}
			} catch (

			IOException e)

			{
				System.out.println("Error: Io exception");
				e.printStackTrace();
			} catch (

			UnsupportedAudioFileException e)

			{
				System.out.println("Error: Audio File is not supported");
				e.printStackTrace();
			}

		}

		public void terminate() throws IOException {
			clientSoc.close();
			synchronized (clients) {
				clients.remove(this);
			}
		}

	}

	/*
	 * ---------------------------------------------------------------------
	 * ---- --------------------------------------------------
	 *
	 * KeyListener waiting for input
	 *
	 * ---------------------------------------------------------------------
	 * ---- --------------------------------------------------
	 */
	class KeyListener implements Runnable {
		@Override
		public void run() {
			System.out.printf("%n%n%n" + USAGE + "%n%n%n");
			
			while (serverRunning) {
				try {
					Thread.sleep(300L);
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					System.out.printf(">>");
					String line = in.readLine();
					if (line.equals("exit")) {
						System.out.println("exit");
						streamPlayer.terminate();
						streamPlayerThread.join();
						in.close();
						close();
						serverRunning = false;
						
						System.exit(0);
					}
					if (line.equals("playSong")) {
						playSong(path);
					}
					if (line.equals("currentSong")) {
						System.out.println(path);
					}
					if (line.equals("setSongPath")) {
						System.out.printf("Set song path to ?%n%nNew path: ");
						setSongPath(in.readLine());
						playSong(path);
					}
					if (line.equals("queueSong")) {
						System.out.printf("Path to song to be queued ?%n%nPath : ");
						playList.add(in.readLine());
						System.out.printf("Current Playlist: " + playList + "%n%n");

					}
					if (line.equals("man")) {
						System.out.println(USAGE);
					}
				} catch (Exception e) {
					System.exit(0);
				}

			}

		}

		private void setSongPath(String line) {
			path = line;
		}

	}

	/*
	 * -------------------------------------------------------------------------
	 * -------------------------------------------------- Inner Class Streams
	 * Music to all clients in the collection
	 *
	 * -------------------------------------------------------------------------
	 * --------------------------------------------------
	 */
	class StreamPlayer implements Runnable {
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
							synchronized (clients) {
								for (Iterator<ClientHandler> iter = clients.iterator(); iter.hasNext();) {
									try {
										ClientHandler client = (ClientHandler) iter.next();
										OutputStream outToClient = client.clientSoc.getOutputStream();
										//Sendet Daten an Client
										AudioDataMessage audioData = AudioDataMessage.newBuilder().setDatapack(ByteString.copyFrom(buffer)).build();
										Data dataPack = Data.newBuilder().setAudioData(audioData).build();
										dataPack.writeDelimitedTo(outToClient);
									} catch (Exception e) {
										System.out.println("Unit lost");
										iter.remove();
									}
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
	class SeverMessenger implements Runnable {

		@Override
		public void run() {
			while(true){
			synchronized (clients) {
				ArrayList<ClientChatMessage> allMessages = new ArrayList<>();
				
				for (Iterator<ClientHandler> iter = clients.iterator(); iter.hasNext();){
					ClientHandler client = (ClientHandler) iter.next();
					InputStream inToClient;
					try {
						inToClient = client.clientSoc.getInputStream();
						//Schaut ob Messages von den Clienten gesendet wurden.
						if (inToClient.available()>0){
							ClientChatMessage message = ClientChatMessage.parseDelimitedFrom(inToClient);
							if(message.getMessage().equals("exit()")){
								System.out.println("Unit "+message.getUser()+ " lost");
								ClientChatMessage exitMessage = ClientChatMessage.newBuilder().setUser(message.getUser()).setMessage("exit").build();
								allMessages.add(exitMessage);
								iter.remove();
							}
							else{
								System.out.println(message.getUser()+": "+ message.getMessage());
								allMessages.add(message);
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
		
				for (Iterator<ClientHandler> iter = clients.iterator(); iter.hasNext();) {
					try {
						ClientHandler client = (ClientHandler) iter.next();
						for (Iterator<ClientChatMessage> itera = allMessages.iterator() ; itera.hasNext();) {
							ClientChatMessage message = (ClientChatMessage) itera.next();
							if(message.getUser().equals(client.clientName)){
							}
							else{
								OutputStream output = client.clientSoc.getOutputStream();
								Data dataPack = Data.newBuilder().setChatMessage(message).build();
							    dataPack.writeDelimitedTo(output);
							}
						}
							
					} catch (Exception e) {
						System.out.println("Unit lost");
						iter.remove();
					}
				}
			}
		}
	}
		
}
}
