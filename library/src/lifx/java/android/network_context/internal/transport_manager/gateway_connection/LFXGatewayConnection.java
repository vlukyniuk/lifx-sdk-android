package lifx.java.android.network_context.internal.transport_manager.gateway_connection;

import java.util.ArrayList;

import lifx.java.android.entities.internal.LFXGatewayDescriptor;
import lifx.java.android.entities.internal.LFXMessage;
import lifx.java.android.entities.internal.structle.LxProtocol;
import lifx.java.android.entities.internal.structle.LxProtocol.Type;

public abstract class LFXGatewayConnection
{
	private static ArrayList<LxProtocol.Type> permittedMessageTypes;
	
	static
	{
		permittedMessageTypes = new ArrayList<LxProtocol.Type>();
		permittedMessageTypes.add( Type.LX_PROTOCOL_LIGHT_SET); 
		permittedMessageTypes.add( Type.LX_PROTOCOL_LIGHT_SET_DIM_ABSOLUTE); 
		permittedMessageTypes.add( Type.LX_PROTOCOL_DEVICE_SET_POWER); 
		permittedMessageTypes.add( Type.LX_PROTOCOL_LIGHT_GET); 
		permittedMessageTypes.add( Type.LX_PROTOCOL_LIGHT_SET_WAVEFORM); 
		permittedMessageTypes.add( Type.LX_PROTOCOL_DEVICE_GET_MESH_FIRMWARE); 
		permittedMessageTypes.add( Type.LX_PROTOCOL_DEVICE_GET_WIFI_FIRMWARE); 
		permittedMessageTypes.add( Type.LX_PROTOCOL_DEVICE_GET_VERSION); 
	}
	
	public enum LFXGatewayConnectionState
	{
		NOT_CONNECTED,
		CONNECTING,
		CONNECTED,
	};

	public void setListener( LFXGatewayConnectionListener listener)
	{
		this.listener = listener;
	}
	
	public static LFXGatewayConnection getGatewayConnectionWithGatewayDescriptor( LFXGatewayDescriptor gatewayDescriptor, LFXGatewayConnectionListener listener)
	{
		// TODO: implement TCP
		
		switch( gatewayDescriptor.getService())
		{
			case LX_PROTOCOL_DEVICE_SERVICE_TCP:
				//return [[LFXTCPGatewayCwonnection alloc] initWithGatewayDescriptor:gateway delegate:delegate];
			case LX_PROTOCOL_DEVICE_SERVICE_UDP:
				return new LFXUDPGatewayConnection( gatewayDescriptor, listener);
			default:
				break;
		}
		
		return null;
	}

	public LFXGatewayConnection( LFXGatewayDescriptor gatewayDescriptor, LFXGatewayConnectionListener listener)
	{
		this.gatewayDescriptor = gatewayDescriptor;
		this.listener = listener;
	}

	private LFXGatewayDescriptor gatewayDescriptor;
	
	public LFXGatewayDescriptor getGatewayDescriptor()
	{
		return gatewayDescriptor;
	}

	private LFXGatewayConnectionListener listener;

	// Connection State
	public abstract void connect();
	public abstract void disconnect();

	private LFXGatewayConnectionState connectionState;

	public LFXGatewayConnectionState getConnectionState()
	{
		return connectionState;
	}
	
	protected void setConnectionState( LFXGatewayConnectionState connectionState)
	{
		this.connectionState = connectionState;
	}

	protected LFXGatewayConnectionListener getListener()
	{
		return listener;
	}
	
	// To be called externally (subclasses to override)
	public abstract void sendMessage( LFXMessage message);

	// temp hack for OTA work
	public abstract void sendData( byte[] data);

	public interface LFXGatewayConnectionListener
	{
		public void gatewayConnectionDidReceiveMessageFromHost( LFXGatewayConnection connection, LFXMessage message, String host);
		public void gatewayConnectionDidConnect( LFXGatewayConnection connection);
		public void gatewayConnectionDidDisconnectWithError( LFXGatewayConnection connection, String error);
	}

	public String toString()
	{
		return "";
		//return [NSString stringWithFormat:@"<%p (%@/%@:%hu)>", self, self.gatewayDescriptor.protocolString, self.gatewayDescriptor.host, self.gatewayDescriptor.port];
	}

	public String getConnectionStateString()
	{
		return this.connectionState.toString();
	}

	// For subclasses to use when queueing messages in an outbox
	//protected static boolean newMessage:(LFXMessage *)newMessage makesQueuedMessageRedundant:(LFXMessage *)queuedMessage
	protected static boolean newMessageMakesQueuedMessageRedundant( LFXMessage newMessage , LFXMessage queuedMessage)
	{
		if( newMessage.getType() != queuedMessage.getType()) 				// if the message types are not equal
		{
			return false;
		}
		
		if( !newMessage.getPath().equals( queuedMessage.getPath()))			// if the message paths are not equal
		{
			System.out.println( "Paths are not equal.");
			System.out.println( newMessage.getPath().toString() + " :: " + queuedMessage.getPath().toString());
			return false;
		}
		
		// TODO:
//		if( !permittedMessageTypes.contains( newMessage.getType()))
//		{
//			return false;
//		}
		
		//System.out.println( "REPLACING MESSAGE IN QUEUE!");
		return true;
	}

}
