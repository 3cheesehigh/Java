package rpc.client;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

public class RPCRemoteServiceProvider extends RPCServiceProvider{
	private int serverPort;
	private InetAddress serverAddress;
	
	public RPCRemoteServiceProvider(final InetAddress inetAddress, int port) throws SocketException {
		this.serverPort = port;
		this.serverAddress = inetAddress;
	}
	
	/**
	 * Diese Methode soll alle benötigten Informationen zum Ausführen des
	 * Methodenaufrufs serialisieren, dann alles in eine RPCCall-Message packen
	 * und diese übertragen. Danach wartet sie auf eine Antwort des Servers,
	 * wertet diese aus und gibt dann entweder das Ergebnis zurück oder wirft
	 * eine Exception.
	 * @throws RPCException 
	 */
	@Override
	public <R> R callexplicit(String classname, String methodname, Serializable[] params) throws RPCException {
		DatagramSocket connect = null;
		try {
			connect = new DatagramSocket();
			
		} catch (SocketException e) {
			System.out.println("Socket build fail");
		}
		RPCCall rpcToSend = rpcCallMessageBuild(classname, methodname, params);
		DatagramPacket send = new DatagramPacket(rpcToSend.toByteArray(), rpcToSend.toByteArray().length, serverAddress, serverPort);
		try {
			connect.send(send);
		} catch (IOException e) {
			System.out.println("FAILED: sending to server");
		}
		byte[] recieve = new byte[4056];
		DatagramPacket fromServer = new DatagramPacket(recieve, recieve.length);
		RPCResult msg = null;
		try {
			connect.receive(fromServer);
			msg = RPCResult.parseFrom(Arrays.copyOfRange(fromServer.getData(), fromServer.getOffset(), fromServer.getLength()));
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
				connect.close();
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
