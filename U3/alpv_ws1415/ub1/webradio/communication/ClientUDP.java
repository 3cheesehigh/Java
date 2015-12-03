/**
 * 
 */
package alpv_ws1415.ub1.webradio.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import alpv_ws1415.ub1.webradio.ui.ClientGUI;

/**
 * @author Ben & Peewee
 *
 */
public class ClientUDP implements Client {
	
	int port;
	private String addr;
	private String username;
	private Socket host;
	private ClientGUI gui;
	
	
	
	private DatagramSocket clientDatagramSocket;
	
	
	
	public ClientUDP(String addr, int port, String username, ClientGUI gui) {
		// TODO Auto-generated constructor stub
		this.addr = addr;
		this.username = username;
		this.port = port;
		this.gui = gui;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try{
		// Sendet erstmal nur ein test an server
		clientDatagramSocket = new DatagramSocket();

		byte[] buffer = "test".getBytes();
		InetAddress receiverAddress =InetAddress.getByName(addr);
		DatagramPacket packet = new DatagramPacket(
		        buffer, buffer.length, receiverAddress, port);
		clientDatagramSocket.send(packet);
		}catch(Exception e ){
			System.out.println("Something went wrong sending stuff");
		}
	}

	/* (non-Javadoc)
	 * @see alpv_ws1415.ub1.webradio.communication.Client#connect(java.net.InetSocketAddress)
	 */
	@Override
	public void connect(InetSocketAddress serverAddress) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alpv_ws1415.ub1.webradio.communication.Client#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alpv_ws1415.ub1.webradio.communication.Client#sendChatMessage(java.lang.String)
	 */
	@Override
	public void sendChatMessage(String message) throws IOException {
		// TODO Auto-generated method stub

	}

}
