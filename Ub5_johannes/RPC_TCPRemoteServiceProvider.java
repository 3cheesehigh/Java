package rpc.client;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import com.google.protobuf.InvalidProtocolBufferException;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

public class RPC_TCPRemoteServiceProvider extends RPCServiceProvider{
	private int serverPort;
	private InetAddress serverAddress;
	
	public RPC_TCPRemoteServiceProvider(final InetAddress inetAddress, int port) throws SocketException {
		this.serverPort = port;
		this.serverAddress = inetAddress;
	}
	
	@Override
	public <R> R callexplicit(String classname, String methodname, Serializable[] params) throws RPCException {
		Socket connect = null;
		try {
			connect = new Socket(serverAddress,serverPort);
		} catch (IOException e) {
			System.out.println("FAILED TO OPEN SOCKET");
		}
		RPCCall rpcToSend = rpcCallMessageBuild(classname, methodname, params);
		DatagramPacket send = new DatagramPacket(rpcToSend.toByteArray(), rpcToSend.toByteArray().length, serverAddress, serverPort);
		try {
			rpcToSend.writeDelimitedTo(connect.getOutputStream());
		} catch (IOException e) {
			System.out.println("FAILED: sending to server");
		}
		byte[] recieve = new byte[4056];
		DatagramPacket fromServer = new DatagramPacket(recieve, recieve.length);
		RPCResult msg = null;
		try {
//			connect.receive(fromServer);
//			msg = RPCResult.parseFrom(Arrays.copyOfRange(fromServer.getData(), fromServer.getOffset(), fromServer.getLength()));
			msg = RPCResult.parseDelimitedFrom(connect.getInputStream());
		} catch (InvalidProtocolBufferException e1) {
			System.out.println("FAILED TO RECIEVE");
		} catch (IOException e) {
			System.out.println("FAILED TO RECIEVE FROM SERVER");
		}
		if(msg.hasException()){
			byte[] exceptionBytes =  msg.getException().toByteArray();
			throw new RPCException(new String(exceptionBytes));
		}else{
			if(connect != null){
				try {
					connect.close();
				} catch (IOException e) {
					System.out.println("FAILED TO CLOSE SOCKET");
				}
			}
			try {
				return RPCSecrets.deserialize(msg.getResult());
			} catch (ClassNotFoundException e) {
				System.out.println("FAILED TO GET RESULT");			}
		}
		return null;
	}
	
	public static RPCCall rpcCallMessageBuild(String className, String methodName, Serializable[] params){
		RPCCall.Builder builder = RPCCall.newBuilder();
		builder.setClassname(className);
		builder.setMethodname(methodName);
		if(params != null){
			for(Serializable param : params){
				builder.addParameters(RPCSecrets.serialize(param));
			}
		}else{
			builder.addParameters(null);
		}
		RPCCall callMsg = builder.build();
		assert (callMsg.isInitialized());
		return callMsg;
	}
	
}
