package rpc.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.spi.LocaleServiceProvider;

import javax.xml.crypto.Data;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

import com.google.protobuf.ByteString;

/**
 * Biete einen RPC-Service auf einen gegebenen Port an; so das statischen
 * Methoden von beliebigen Klassen ueber Netzwerk mit Hilfe des
 * <tt>RPCRemoteServiceProvider</tt> aufgerufen werden koennen.
 */
public class RPCServerServiceProvider implements Runnable {
	private static final int BUFFER_SIZE = 4096;
	private RPCServiceProvider serviceProvider;
	DatagramSocket serverSocket = null;
	boolean run = true;
	int port;

	/**
	 * @param serviceProvider
	 *            der RPC-Service, der genutz werden soll, um die Methode
	 *            aufzurufen.
	 * @param port
	 *            Port, auf dem der Server den RPC Service anbietet
	 */
	public RPCServerServiceProvider(RPCServiceProvider serviceProvider, int port) throws SocketException {
		this.serviceProvider = serviceProvider;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			// start server
			serverSocket = new DatagramSocket(port);
			System.out.println("------------------------------");
			System.out.println("Open Server");
			System.out.println("");
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Server not started");
		}
		
		while (run) {
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			
			try {
				// wait for the packages
				serverSocket.receive(packet);

				try {
					// read the package
					ByteArrayInputStream input = new ByteArrayInputStream(packet.getData());
					RPCCall message = RPCCall.parseDelimitedFrom(input);
					String className = message.getClassname();
					String methodName = message.getMethodname();
					
					// serialize the parameters
					List<ByteString> paramList = message.getParametersList();
					Serializable[] params = new Serializable[paramList.size()];
					for (int i = 0; i < paramList.size(); i++) {
						params[i] = (Serializable) RPCSecrets.serialize(paramList.get(i));
					}

					// call the LocalServiceProvider
					Object result = serviceProvider.callexplicit(className, methodName, params);
					ByteString newResult = (ByteString) RPCSecrets.serialize(result);

					// bob builds our result-message
					RPCResult.Builder bob = RPCResult.newBuilder();
					bob.setResult(newResult);

					// send the result back to the client
					byte[] sendResult = bob.build().toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(sendResult, sendResult.length, packet.getAddress(),
							packet.getPort());
					serverSocket.send(sendPacket);
				
				} catch (RPCException e) {
					//send the excpetion to the client
					RPCResult.Builder bob = RPCResult.newBuilder();
					bob.setException(RPCSecrets.serialize(e));
					byte[] sendException = bob.build().toByteArray();
					DatagramPacket sendExceptionPacket = new DatagramPacket(sendException, sendException.length,packet.getAddress(),packet.getPort());
					serverSocket.send(sendExceptionPacket);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("");
				System.out.println("Server got closed");
				System.out.println("------------------------------");
			}


		}	
	}

	/**
	 * Terminiert den Server.
	 */
	public void terminate() {
		run = false;
		serverSocket.close();
	}

}
