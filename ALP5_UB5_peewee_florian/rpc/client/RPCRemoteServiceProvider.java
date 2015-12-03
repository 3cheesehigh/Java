package rpc.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import com.google.protobuf.ByteString;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

public class RPCRemoteServiceProvider extends RPCServiceProvider {
	private static final int BUFFER_SIZE = 4096;
	DatagramSocket socket;
	InetSocketAddress serverAddress;

	public RPCRemoteServiceProvider(final InetAddress inetAddress, int port) throws SocketException {
		this.socket = new DatagramSocket();
		this.serverAddress = new InetSocketAddress(inetAddress, port);

	}

	/**
	 * Diese Methode soll alle benötigten Informationen zum Ausführen des
	 * Methodenaufrufs serialisieren, dann alles in eine RPCCall-Message packen
	 * und diese übertragen. Danach wartet sie auf eine Antwort des Servers,
	 * wertet diese aus und gibt dann entweder das Ergebnis zurück oder wirft
	 * eine Exception.
	 */
	@Override
	public <R> R callexplicit(String classname, String methodname, Serializable[] params) throws RPCException {

		// build the RPCCall request fpr the server
		RPCCall.Builder requestBuilder = RPCProtocol.RPCCall.newBuilder().setClassname(classname)
				.setMethodname(methodname);

		for (int i = 0; i < params.length; i++) {

			ByteString serializedParams = RPCSecrets.serialize(params[i]);
			requestBuilder.addParameters(serializedParams);
		}

		// set the data in the DatagramPacket
		RPCCall callRequest = requestBuilder.build();
		byte[] sendCallByte = callRequest.toByteArray();
		DatagramPacket sendRequest = new DatagramPacket(sendCallByte, sendCallByte.length, serverAddress);

		// try to send the packet to the server
		try {
			socket.send(sendRequest);
		} catch (IOException e1) {
			System.out.println("Could not send the request to the server");
		}

		try {

			// recieve answer
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			// read the data
			ByteArrayInputStream input = new ByteArrayInputStream(packet.getData());
			RPCResult result = RPCResult.parseDelimitedFrom(input);
			if (result.hasException()) {
				throw (RPCException) RPCSecrets.deserialize(result.getException());
			} else {
				return RPCSecrets.deserialize(result.getResult());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

}
