package ironclad49er.elevators.common;
// CLIENT - 1.6
import ironclad49er.elevators.client.GuiElevator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ElevatorPacketHandler implements IConnectionHandler, IPacketHandler{
	
	public static final String[] CHANNELS = {"DE_GUI_REQUEST", "DE_GUI_RESPONSE", "DE_UPDATE", "DE_EPROP", "DE_ERROR", "DE_SHCI"};
	
	public static final int GUI_REQUEST = 0;
	public static final int GUI_DATA = 1;
	public static final int UPDATE_RIDERS = 2;
	public static final int ELEVATOR_PROPERTIES = 3;
	public static final int GUI_COMMUNICATION_ERROR = 4;
	public static final int SHORT_CIRCUIT = 5;
	
	public static HashMap<String, ChunkPosition> elevatorRequests = new HashMap();
	
	public static void sendRiderUpdates(Set<Entity> entities, int x, int y, int z) { sendRiderUpdates(entities, x, y, z, false); }
	public static void sendRiderUpdates(Set<Entity> entities, int x, int y, int z, boolean ejectRiders) {
		boolean noSend = false;
		
		if (entities == null || entities.isEmpty()) { return; }
		
		if (noSend) {return;}
		
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        int dimensionID = 0;
        
		try {
			entities.remove(null);
			
			// Size
			data.writeInt(entities.size());
			
	    	Iterator<Entity> iter = entities.iterator();
			while(iter.hasNext()) {
				Entity curEntity = iter.next();
				dimensionID = curEntity.worldObj.getWorldInfo().getDimension();
				data.writeInt(curEntity.entityId);        //ID
				data.writeFloat((float)curEntity.posY);   //Ypos
				if (ejectRiders){
					data.writeInt(1);                     //Data (version 2)
				}
				else {
					data.writeInt(0);
				}
			}
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = CHANNELS[UPDATE_RIDERS];
			packet.data = bytes.toByteArray();
	        packet.length = packet.data.length;
    		
    		PacketDispatcher.sendPacketToAllAround(x, y, z, 400, dimensionID, packet);
    		
		}
		catch (IOException e) {
			mod_Elevator.say("Error while creating entity update packet.", true);
			e.printStackTrace();
		}
	}
	
	public boolean requestGUIMapping(World world, ChunkPosition loc, EntityPlayer player) {
		if (world == null || loc == null) {return false;}
		if (world.getBlockId(loc.x, loc.y, loc.z) != mod_Elevator.Elevator.blockID) {return false;}
		
		mod_Elevator.say((new StringBuilder()).append("Received elevator request from ").append(player.username).toString());
		
		BlockElevator elevator = (BlockElevator)mod_Elevator.Elevator;
		TileEntityElevator elevatorInfo = elevator.getTileEntity(world, loc.x, loc.y, loc.z);
		
		if (elevatorInfo == null) {return false;}
		
		try {
			Packet250CustomPayload packet = elevatorInfo.createPropertiesPacket(true);
			
    		elevatorRequests.put(player.username, loc);
    		
    		if (player instanceof EntityPlayerMP) {
    			mod_Elevator.say("Attempting to open GUI via packet");
    			PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
    		}
    		else {
    			mod_Elevator.say("Attempting to open GUI locally");
    			ModLoader.openGUI(player, new GuiElevator(packet, loc));
    		}

    		mod_Elevator.say((new StringBuilder()).append("Successfully added request for ").append(player.username).toString());
		}
		catch (IOException e) {
			mod_Elevator.say("Error while creating packet - unable to open GUI for " + player.username, true);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	//Send information back to the server about the selections made by the player in the GUI
	public boolean sendGUIPacketToServer(Packet250CustomPayload packet) {
		PacketDispatcher.sendPacketToServer(packet);
        return true;
	}	

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
		DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		mod_Elevator.say("Packet received on channel " + packet.channel);
		
		try {
			
			EntityPlayer playerMP = (EntityPlayer) player;
			
			if (player == null) { return; }
			World world = playerMP.worldObj;
			if (world == null) { 
				mod_Elevator.say("World is null. Returning.");
				return; 
			}
			
			// GUI Request received
			if (packet.channel.equals(CHANNELS[GUI_REQUEST])) {		        
				// Attempt to open GUI screen with received data
				GuiScreen screen = new GuiElevator(packet);
				ModLoader.openGUI(ModLoader.getMinecraftInstance().thePlayer, screen);
			}
			else if (packet.channel.equals(CHANNELS[GUI_DATA])) {				
				ElevatorProperties props = new ElevatorProperties();
				
				props.readInData(packet);
				
				int command = props.command;
				
				ChunkPosition pos = elevatorRequests.get(playerMP.username);
				if (pos == null) {return;}
				
				if (world.getBlockId(pos.x, pos.y, pos.z) != mod_Elevator.Elevator.blockID) { return; }
				BlockElevator elevator = (BlockElevator)mod_Elevator.Elevator;
				TileEntityElevator tile = elevator.getTileEntity(world, pos.x, pos.y, pos.z);
				if (tile == null) {return;}
				
	    		mod_Elevator.say("Received elevator response from " + playerMP.username + " requesting GUI command " + command);
	    		
	    		switch (command) { 
	    			case mod_Elevator.GUI_OPTIONS_APPLY:
	    				mod_Elevator.checkedProperties.put(pos, packet);
						mod_Elevator.refreshElevator(world, pos);
						break;
	    			case mod_Elevator.GUI_RESET:
	    				mod_Elevator.elevator_reset(world, pos); 
	    				if (elevatorRequests.containsKey(playerMP.username)) {elevatorRequests.remove(playerMP.username);}
	    				break;
	    			default:
	    				if (command < 1 || command > mod_Elevator.max_elevator_Y) {break;}
	    		    	if ( command > tile.numFloors() || command == tile.curFloor()) { break; }
	    		    	mod_Elevator.elevator_requestFloor(world, pos, command);
	    		    	if (elevatorRequests.containsKey(playerMP.username)) {elevatorRequests.remove(playerMP.username);}
	    		    	break;
	    		}
			}
			// Elevator/Rider update request received
			else if (packet.channel.equals(CHANNELS[UPDATE_RIDERS])) {
				// Find all entities corresponding to the given entity ids and update them to given y coords
				
				// Size (1 integer)
				int numEntities = dataStream.readInt();
				
				for (int i = 0; i < numEntities; i++) {
					int entityID = dataStream.readInt();           // ID
					float newEntityYPos = dataStream.readFloat();  // Ypos
					int entity_data = dataStream.readInt();        // Data
					
					Entity entity = mod_Elevator.getEntityByID(entityID);
					mod_Elevator.say("Received request for entity id " + entityID + " to be set to Y: " + newEntityYPos);
					if (entity != null){
						if (entity instanceof EntityElevator) {
							EntityElevator curElevator = (EntityElevator)entity;
							curElevator.setPosition(entity.posX, (double)newEntityYPos, entity.posZ);
						}
						else {
							if (entity instanceof EntityLiving) {
								entity.posY = (double)newEntityYPos + entity.yOffset;
								entity.onGround = true;
					    		entity.fallDistance = 0.0F;
					    		entity.isCollidedVertically = true;
							}
							else {
								entity.posY = (double)newEntityYPos;
								entity.onGround = false;
							}
							if (entity_data == 1) {
								entity.motionY = 0.1;
							}
						}
						
						mod_Elevator.say("Entity with id " + entity.entityId + " was set to " + newEntityYPos);
					}
					else {
						mod_Elevator.say("Entity with that ID does not exist");
					}
				}
			}
			else if (packet.channel.equals(CHANNELS[ELEVATOR_PROPERTIES])) {
				int entityID = dataStream.readInt();
				int dest = dataStream.readInt();
				boolean center = dataStream.readBoolean();
				int metadata = dataStream.readInt();
				
				mod_Elevator.say("Received prop update info for elevator id " + entityID);
				
				Entity entity = mod_Elevator.getEntityByID(entityID);
				if (entity == null || !(entity instanceof EntityElevator)) {
					mod_Elevator.say("Entity with that ID does not exist");
				}
				else {
					EntityElevator elevator = (EntityElevator)entity;
					elevator.setProperties(dest, center, true, metadata);
				}
			}
		}
		catch (IOException e) {
			// Send error packet if something goes wrong
			if (packet.channel.equals(CHANNELS[GUI_DATA])) {
				Packet250CustomPayload responsePacket = new Packet250CustomPayload();
				responsePacket.channel = CHANNELS[GUI_COMMUNICATION_ERROR];
				responsePacket.data = new byte[1];
				responsePacket.data[0] = 0x01;
				responsePacket.length = 1;
				
				PacketDispatcher.sendPacketToServer(responsePacket);
				
			}
			mod_Elevator.say("Error while reading incoming packet.", true);
			e.printStackTrace();
			return;
		}
	}
	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, NetworkManager manager) {		
		if (mod_Elevator.shortCircuit) {
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = CHANNELS[SHORT_CIRCUIT];
			packet.data = new byte[1];
			packet.data[0] = 0x11;
			packet.length = 1;
			PacketDispatcher.sendPacketToServer(packet);
		}
	}
	@Override
	public String connectionReceived(NetLoginHandler netHandler, NetworkManager manager) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, NetworkManager manager) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, NetworkManager manager) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void connectionClosed(NetworkManager manager) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "UNREGISTER";
		
		try {
			for (int i = 0; i < CHANNELS.length; i++) {
				data.writeUTF(CHANNELS[i]);
			}
			
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;
			PacketDispatcher.sendPacketToServer(packet);
		}
		catch (Exception e) {
			mod_Elevator.say("Unable to unregister channels!!", true);
		}
	}
	@Override
	public void clientLoggedIn(NetHandler clientHandler, NetworkManager manager, Packet1Login login) {
    	mod_Elevator.say("Sending channel registration packet...");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "REGISTER";
		
		try {
			for (int i = 0; i < CHANNELS.length; i++) {
				data.writeUTF(CHANNELS[i]);
			}
			
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;
			PacketDispatcher.sendPacketToServer(packet);
		}
		catch (Exception e) {
			mod_Elevator.say("Unable to register channels!!", true);
		}
	}
}
