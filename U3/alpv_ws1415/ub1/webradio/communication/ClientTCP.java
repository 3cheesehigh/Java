package alpv_ws1415.ub1.webradio.communication;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;

import alpv_ws1415.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1415.ub1.webradio.protobuf.myProto.*;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.AudioDataMessage;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.AudioFormatMessage;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.ClientChatMessage;
import alpv_ws1415.ub1.webradio.protobuf.myProto.Data.HelloMessage;
import alpv_ws1415.ub1.webradio.ui.ClientGUI;

public class ClientTCP implements Client {

	int port;
	private String addr;
	private String username;
	private Socket host;
	private boolean musicIsPlaying;
	private ClientGUI gui;
	private String taken = "That name has already been taken. Please choose another";
	private String msg;
	private boolean clientRunning;

	public ClientTCP(String addr, int port, String username, ClientGUI gui)
			throws InvocationTargetException, InterruptedException {
		this.addr = addr;
		this.username = username;
		this.port = port;
		this.gui = gui;
		clientRunning = true;

	}

	public String getUsername() {
		return username;
	}

	public boolean socketIsOpen() {
		return !host.isClosed();
	}

	@Override
	public void run() {
		// connects to server
		try {
			InetSocketAddress sockAddr = new InetSocketAddress(addr, port);
			connect(sockAddr);
			// send Username
			sendUserName(username);
			// Hello World receiving and printing
			InputStream serverIn = host.getInputStream();
			HelloMessage hello = HelloMessage.parseDelimitedFrom(serverIn);
			if (hello.getHelloMsg().equals(taken)) {
				System.out.println(hello.getHelloMsg());
				close();
				System.exit(0);
			} else {
				System.out.println(hello.getHelloMsg());
			}

			// Get the AudioFormat

			AudioFormatMessage format = AudioFormatMessage.parseDelimitedFrom(serverIn);
			AudioFormat audioFormat = new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(),
					format.getChannels(), format.getProperty(), format.getBigEndian());
			System.out.println(("The audioformat: " + audioFormat + '\n'));

			// Start Messenger
			Thread listener = new Thread(new InputListener());
			listener.start();
			// Start Player
			AudioPlayer audioPlayer = new AudioPlayer(audioFormat);
			audioPlayer.start();

			// Play Music

			while (clientRunning) {
				Data data = null;
				try {
					data = Data.parseDelimitedFrom(serverIn);
					synchronized (data) {
						if (data.hasAudioData()) {
							AudioDataMessage din = data.getAudioData();
							byte[] nData;
							nData = din.getDatapack().toByteArray();
							if (nData != null) {
								audioPlayer.writeBytes(nData);
							}
						}
						if (data.hasChatMessage()) {
							ClientChatMessage message = data.getChatMessage();
							if (gui == null) {
								System.out.println(message.getUser() + ": " + message.getMessage());
							} else {
								gui.pushChatMessage(message.getUser() + ": " + message.getMessage());
							}
						}
					}
				} catch (Exception e) {
					System.out.println("Unit lost");
				}
			}

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close();
		}

	}

	private void sendUserName(String user) throws IOException {
		OutputStream output = host.getOutputStream();
		ClientChatMessage userNameMessage = ClientChatMessage.newBuilder().setUser(user).build();
		userNameMessage.writeDelimitedTo(output);

	}

	@Override
	public void connect(InetSocketAddress serverAddress) throws IOException {
		// connects to server
		try {
			host = new Socket(serverAddress.getHostName(), port);
			musicIsPlaying = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			host.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendChatMessage(String message) throws IOException {
		OutputStream output = host.getOutputStream();
		ClientChatMessage clientMessage = ClientChatMessage.newBuilder().setUser(username).setMessage(message).build();
		clientMessage.writeDelimitedTo(output);
	}

	class InputListener implements Runnable {
		@Override
		public void run() {
			if (gui == null) {
				System.out.println("Chat Online");
				System.out.println("USAGE: exit() for disconnect");
			} else {
				gui.pushChatMessage("Chat Online");
				gui.pushChatMessage("USAGE: exit() for disconnect");
			}
			while (true) {
				try {
					if (gui == null) {
						Thread.sleep(300L);
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						System.out.println((">>"));
						String line = in.readLine();
						if (line.equals("exit()")) {
							sendChatMessage("exit()");
							close();
							System.exit(0);
						}
						sendChatMessage(line);
					} else {
						if ((msg = gui.getChatMessage()) != null) {

							gui.pushChatMessage(username + " : " + msg);
							sendChatMessage(msg);
						}
						if (gui.clientShutdown()) {
							clientRunning = false;
							close();
						}
					}
				} catch (Exception e) {
					System.exit(0);
				}
			}

		}

	}

}
