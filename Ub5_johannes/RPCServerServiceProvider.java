package rpc.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

/**
 * Biete einen RPC-Service auf einen gegebenen Port an; so das statischen
 * Methoden von beliebigen Klassen ueber Netzwerk mit Hilfe des
 * <tt>RPCRemoteServiceProvider</tt> aufgerufen werden koennen.
 */
public class RPCServerServiceProvider implements Runnable {
	/**
	 * @param serviceProvider
	 *            der RPC-Service, der genutz werden soll, um die Methode
	 *            aufzurufen.
	 * @param port
	 *            Port, auf dem der Server den RPC Service anbietet
	 */
	private RPCServiceProvider local;
	private DatagramSocket socket;
	private boolean run = false;

	public RPCServerServiceProvider(RPCServiceProvider serviceProvider, int port) throws SocketException {
		local = serviceProvider;
		socket = new DatagramSocket(port);
	}

	@Override
	public void run() {
		run = true;
		while (run) {
			byte[] recieve = new byte[4056];
			DatagramPacket fromClient = new DatagramPacket(recieve, recieve.length);
			try {
				socket.receive(fromClient);
			} catch (IOException e) {
				if(socket.isClosed()){
					if(!run){
						System.out.println("SERVER TERMINATED");
					}else{
						System.out.println("SOCKET CLOSED");
					}
				}else{
					System.out.println("FAILDED TO GET CALL");
				}
				break;
			}
			RPCCall msg = null;
			try {
				msg = RPCCall.parseFrom(Arrays.copyOfRange(fromClient.getData(), fromClient.getOffset(), fromClient.getLength()));
			} catch (InvalidProtocolBufferException e1) {
				System.out.println("FAILED TO RECIEVE");
			}
			int leng = msg.getParametersList().size();
			Serializable[] params = new Serializable[leng];
			for (int i = 0; i < leng; i++) {
				try {
					params[i] = RPCSecrets.deserialize(msg.getParameters(i));
				} catch (ClassNotFoundException e) {
					System.out.println("FAILE: " + e);
				}
			}
			DatagramPacket packet = null;
			try {
				Object response = local.callexplicit(msg.getClassname(), msg.getMethodname(), params);
				ByteString responseForMessage = RPCSecrets.serialize(response);
				RPCResult resToClient = rpcResultMessageBuild(responseForMessage, null);
				packet = new DatagramPacket(resToClient.toByteArray(), resToClient.toByteArray().length,fromClient.getAddress(), fromClient.getPort());
			} catch (RPCException e) {
				RPCResult resToClient = rpcResultMessageBuild(null, e);
				packet = new DatagramPacket(resToClient.toByteArray(), resToClient.toByteArray().length,fromClient.getAddress(), fromClient.getPort());
			}
			try{
				socket.send(packet);
			}
			catch (IOException e) {
				System.out.println("FAILED TO SEND RESULT");
			}
		}
//		socket.close();
	}

	public static RPCResult rpcResultMessageBuild(ByteString res, Exception exc) {
		RPCResult.Builder builder = RPCResult.newBuilder();
		if (res != null) {
			builder.setResult(res);
		}
		if (exc != null) {
			builder.setException(RPCSecrets.serialize(exc));
		}

		RPCResult callMsg = builder.build();
		assert (callMsg.isInitialized());
		return callMsg;
	}

	/**
	 * Terminiert den Server.
	 */
	public void terminate() {
		run = false;
		socket.close();
	}

}
