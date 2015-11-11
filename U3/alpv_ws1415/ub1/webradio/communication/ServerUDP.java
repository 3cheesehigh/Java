package alpv_ws1415.ub1.webradio.communication;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

public class ServerUDP implements Server {
	
	private DatagramSocket serverSocket;
	private int port;
	
	public ServerUDP(int port ){
		this.port = port;
	}
	
	

	@Override
	public void run() {
		//schickt erstmal nur ein hello world an server
		

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void playSong(String path) throws MalformedURLException, UnsupportedAudioFileException, IOException {
		// TODO Auto-generated method stub

	}

}
