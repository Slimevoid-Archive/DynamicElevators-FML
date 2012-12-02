package elevators.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetLoginHandler;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import elevators.api.IDECommonProxy;
import elevators.blocks.BlockElevator;
import elevators.core.DECore;
import elevators.core.DEInit;
import elevators.core.DEProperties;
import elevators.entities.EntityElevator;
import elevators.network.packets.PacketButtonUpdate;
import elevators.tileentities.TileEntityElevator;
import eurysmods.network.packets.core.PacketIds;

public class ElevatorPacketHandler implements IConnectionHandler, IPacketHandler {

	public static final String[] CHANNELS = {
			"DE_GUI_REQUEST",
			"DE_GUI_RESPONSE",
			"DE_UPDATE",
			"DE_EPROP",
			"DE_ERROR",
			"DE_SHCI",
			"DE_BUPDATE" };

	public static final int GUI_REQUEST = 0;
	public static final int GUI_DATA = 1;
	public static final int UPDATE_RIDERS = 2;
	public static final int ELEVATOR_PROPERTIES = 3;
	public static final int GUI_COMMUNICATION_ERROR = 4;
	public static final int SHORT_CIRCUIT = 5;
	public static final int BLOCK_UPDATE = 6;

	public static HashMap<String, ChunkPosition> elevatorRequests = new HashMap();

	public static void sendRiderUpdates(Set<Entity> entities, int x, int y, int z) {
		sendRiderUpdates(entities, x, y, z, false);
	}

	public static void sendRiderUpdates(Set<Entity> entities, int x, int y, int z, boolean ejectRiders) {
		boolean noSend = false;

		if (entities == null || entities.isEmpty()) {
			return;
		}

		if (noSend) {
			return;
		}

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		int dimensionID = 0;

		try {
			entities.remove(null);

			// Size
			data.writeInt(entities.size());

			Iterator<Entity> iter = entities.iterator();
			while (iter.hasNext()) {
				Entity curEntity = iter.next();
				dimensionID = curEntity.worldObj.getWorldInfo().getDimension();
				data.writeInt(curEntity.entityId); // ID
				data.writeFloat((float) curEntity.posY); // Ypos
				if (ejectRiders) {
					data.writeInt(1); // Data (version 2)
				} else {
					data.writeInt(0);
				}
			}
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = CHANNELS[UPDATE_RIDERS];
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;

			PacketDispatcher.sendPacketToAllAround(
					x,
					y,
					z,
					400,
					dimensionID,
					packet);

		} catch (IOException e) {
			DECore.say("Error while creating entity update packet.", true);
			e.printStackTrace();
		}
	}

	public boolean requestGUIMapping(World world, ChunkPosition loc, EntityPlayer player) {
		if (world == null || loc == null) {
			return false;
		}
		if (world.getBlockId(loc.x, loc.y, loc.z) != DECore.Elevator.blockID) {
			return false;
		}

		DECore.say((new StringBuilder())
				.append("Received elevator request from ")
					.append(player.username)
					.toString());

		BlockElevator elevator = (BlockElevator) DECore.Elevator;
		TileEntityElevator elevatorInfo = BlockElevator.getTileEntity(
				world,
				loc.x,
				loc.y,
				loc.z);

		if (elevatorInfo == null) {
			return false;
		}

		try {
			Packet250CustomPayload packet = elevatorInfo
					.createPropertiesPacket(true);

			elevatorRequests.put(player.username, loc);
			if (player instanceof EntityPlayerMP) {
				DECore.say("Attempting to open GUI via packet");
				PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
			} else {
				DECore.say("Attempting to open GUI locally");
				((IDECommonProxy) (DEInit.DEM.getProxy())).openGui(
						world,
						player,
						packet,
						loc);
			}

			DECore.say((new StringBuilder())
					.append("Successfully added request for ")
						.append(player.username)
						.toString());
		} catch (IOException e) {
			DECore
					.say(
							"Error while creating packet - unable to open GUI for " + player.username,
							true);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// Send information back to the server about the selections made by the
	// player in the GUI
	public boolean sendGUIPacketToServer(Packet250CustomPayload packet) {
		PacketDispatcher.sendPacketToServer(packet);
		return true;
	}

	public static void sendButtonTickUpdate(World world, int x, int y, int z, int metadata) {
		PacketButtonUpdate packet = new PacketButtonUpdate(x, y, z, metadata);
		PacketDispatcher.sendPacketToAllAround(x, y, z, 400, world.getWorldInfo().getDimension(), packet.getPacket());
	}

	private void handleButtonUpdatePacket(Player player, PacketButtonUpdate packetBU) {
		if (player instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer)player;
			World world = entityplayer.worldObj;
			if (packetBU.targetExists(world)) {
				int metadata = world.getBlockMetadata(packetBU.xPosition, packetBU.yPosition, packetBU.zPosition);
				if ((metadata & 8) != 0) {
					world.setBlockMetadataWithNotify(packetBU.xPosition, packetBU.yPosition, packetBU.zPosition, metadata & 7);
	                world.markBlocksDirty(packetBU.xPosition, packetBU.yPosition, packetBU.zPosition, packetBU.xPosition, packetBU.yPosition, packetBU.zPosition);
				}
			}
		}
	}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		DataInputStream dataStream = new DataInputStream(
				new ByteArrayInputStream(packet.data));

		DECore.say("Packet received on channel " + packet.channel);

		try {

			EntityPlayer playerMP = (EntityPlayer) player;

			if (player == null) {
				return;
			}
			World world = playerMP.worldObj;
			if (world == null) {
				DECore.say("World is null. Returning.");
				return;
			}

			// GUI Request received
			if (packet.channel.equals(CHANNELS[GUI_REQUEST])) {
				// Attempt to open GUI screen with received data
				// EURY EDIT
				((IDECommonProxy) DEInit.DEM.getProxy()).openGui(
						world,
						null,
						packet,
						null);
			} else if (packet.channel.equals(CHANNELS[GUI_DATA])) {
				DEProperties props = new DEProperties();

				props.readInData(packet);

				int command = props.command;

				ChunkPosition pos = elevatorRequests.get(playerMP.username);
				if (pos == null) {
					return;
				}

				if (world.getBlockId(pos.x, pos.y, pos.z) != DECore.Elevator.blockID) {
					return;
				}
				BlockElevator elevator = (BlockElevator) DECore.Elevator;
				TileEntityElevator tile = BlockElevator.getTileEntity(
						world,
						pos.x,
						pos.y,
						pos.z);
				if (tile == null) {
					return;
				}

				DECore
						.say("Received elevator response from " + playerMP.username + " requesting GUI command " + command);

				switch (command) {
				case DECore.GUI_OPTIONS_APPLY:
					DECore.checkedProperties.put(pos, packet);
					DECore.refreshElevator(world, pos);
					break;
				case DECore.GUI_RESET:
					DECore.elevator_reset(world, pos);
					if (elevatorRequests.containsKey(playerMP.username)) {
						elevatorRequests.remove(playerMP.username);
					}
					break;
				default:
					if (command < 1 || command > DECore.max_elevator_Y) {
						break;
					}
					if (command > tile.numFloors() || command == tile
							.curFloor()) {
						break;
					}
					DECore.elevator_requestFloor(world, pos, command);
					if (elevatorRequests.containsKey(playerMP.username)) {
						elevatorRequests.remove(playerMP.username);
					}
					break;
				}
			}
			// Elevator/Rider update request received
			else if (packet.channel.equals(CHANNELS[UPDATE_RIDERS])) {
				// Find all entities corresponding to the given entity ids and
				// update them to given y coords

				// Size (1 integer)
				int numEntities = dataStream.readInt();

				for (int i = 0; i < numEntities; i++) {
					int entityID = dataStream.readInt(); // ID
					float newEntityYPos = dataStream.readFloat(); // Ypos
					int entity_data = dataStream.readInt(); // Data

					Entity entity = DECore.getEntityByID(entityID);
					DECore
							.say("Received request for entity id " + entityID + " to be set to Y: " + newEntityYPos);
					if (entity != null) {
						if (entity instanceof EntityElevator) {
							EntityElevator curElevator = (EntityElevator) entity;
							curElevator.setPosition(
									entity.posX,
									newEntityYPos,
									entity.posZ);
						} else {
							if (entity instanceof EntityLiving) {
								entity.posY = (double) newEntityYPos + entity.yOffset;
								entity.onGround = true;
								entity.fallDistance = 0.0F;
								entity.isCollidedVertically = true;
							} else {
								entity.posY = newEntityYPos;
								entity.onGround = false;
							}
							if (entity_data == 1) {
								entity.motionY = 0.1;
							}
						}

						DECore
								.say("Entity with id " + entity.entityId + " was set to " + newEntityYPos);
					} else {
						DECore.say("Entity with that ID does not exist");
					}
				}
			} else if (packet.channel.equals(CHANNELS[ELEVATOR_PROPERTIES])) {
				int entityID = dataStream.readInt();
				int dest = dataStream.readInt();
				boolean center = dataStream.readBoolean();
				int metadata = dataStream.readInt();

				DECore
						.say("Received prop update info for elevator id " + entityID);

				Entity entity = DECore.getEntityByID(entityID);
				if (entity == null || !(entity instanceof EntityElevator)) {
					DECore.say("Entity with that ID does not exist");
				} else {
					EntityElevator elevator = (EntityElevator) entity;
					elevator.setProperties(dest, center, true, metadata);
				}
			} else if (packet.channel.equals(CHANNELS[BLOCK_UPDATE])) {
				int packetID = dataStream.read();
				if (packetID == PacketIds.UPDATE) {
					PacketButtonUpdate packetBU = new PacketButtonUpdate();
					packetBU.readData(dataStream);
					handleButtonUpdatePacket(player, packetBU);
				}
			}
		} catch (IOException e) {
			// Send error packet if something goes wrong
			if (packet.channel.equals(CHANNELS[GUI_DATA])) {
				Packet250CustomPayload responsePacket = new Packet250CustomPayload();
				responsePacket.channel = CHANNELS[GUI_COMMUNICATION_ERROR];
				responsePacket.data = new byte[1];
				responsePacket.data[0] = 0x01;
				responsePacket.length = 1;

				PacketDispatcher.sendPacketToServer(responsePacket);

			}
			DECore.say("Error while reading incoming packet.", true);
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
		if (DECore.shortCircuit) {
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = CHANNELS[SHORT_CIRCUIT];
			packet.data = new byte[1];
			packet.data[0] = 0x11;
			packet.length = 1;
			PacketDispatcher.sendPacketToServer(packet);
		}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionClosed(INetworkManager manager) {
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
		} catch (Exception e) {
			DECore.say("Unable to unregister channels!!", true);
		}
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
		DECore.say("Sending channel registration packet...");
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
		} catch (Exception e) {
			DECore.say("Unable to register channels!!", true);
		}
	}
}
