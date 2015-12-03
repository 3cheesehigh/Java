package rpc.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

public class RPC_TCPServerServiceProvider implements Runnable {

	private RPCServiceProvider local;
	private ServerSocket socket;
	private boolean run = false;

	public RPC_TCPServerServiceProvider(RPCServiceProvider serviceProvider, int port) throws SocketException {
		local = serviceProvider;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("FAILDE TO BUILD SOCKET");
		}
	}

	@Override
	public void run() {
		run = true;
		while (run) {
			byte[] recieve = new byte[4056];
			DatagramPacket fromClient = new DatagramPacket(recieve, recieve.length);
			RPCCall msg = null;
			Socket client = null;
			try {
				client = socket.accept();
				msg = RPCCall.parseDelimitedFrom(client.getInputStream());
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
			int leng = msg.getParametersList().size();
			Serializable[] params = new Serializable[leng];
			for (int i = 0; i < leng; i++) {
				try {
					params[i] = RPCSecrets.deserialize(msg.getParameters(i));
				} catch (ClassNotFoundException e) {
					System.out.println("FAILE: " + e);
				}
			}
			RPCResult resToClient = null;
			try {
				Object response = local.callexplicit(msg.getClassname(), msg.getMethodname(), params);
				ByteString responseForMessage = RPCSecrets.serialize(response);
				resToClient = rpcResultMessageBuild(responseForMessage, null);
			} catch (RPCException e) {
				resToClient = rpcResultMessageBuild(null, e);
			}
			try{
				resToClient.writeDelimitedTo(client.getOutputStream());
			}
			catch (IOException e) {
				System.out.println("FAILED TO SEND RESULT");
			}
			try {
				client.close();
			} catch (IOException e) {
				System.out.println("FAILED TO CLOSE CLIENTSOCKET");
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
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("FAILED TO CLOSE SERVERSOCKET");		}
	}
	
}
