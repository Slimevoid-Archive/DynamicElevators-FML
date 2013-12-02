package slimevoid.elevators.core.lib;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import slimevoid.elevators.api.IDECommonProxy;
import slimevoid.elevators.blocks.BlockElevator;
import slimevoid.elevators.core.DynamicElevators;
import slimevoid.elevators.network.ElevatorPacketHandler;
import slimevoid.elevators.network.packets.PacketButtonUpdate;
import slimevoid.elevators.tileentities.TileEntityElevator;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketLib {

	public static final ElevatorPacketHandler		packetHandler			= new ElevatorPacketHandler();

	// public static final String[] CHANNELSLIST = {};
	public static final String						GUI_REQUEST				= "DE_GUI_REQUEST";
	public static final String						GUI_DATA				= "DE_GUI_DATA";
	public static final String						UPDATE_RIDERS			= "DE_UPDATE";
	public static final String						ELEVATOR_PROPERTIES		= "DE_EPROP";
	public static final String						GUI_COMMUNICATION_ERROR	= "DE_ERROR";
	public static final String						SHORT_CIRCUIT			= "DE_SHCI";
	public static final String						BLOCK_UPDATE			= "DE_BUPDATE";

	public static HashMap<String, ChunkPosition>	elevatorRequests		= new HashMap();

	public static void sendRiderUpdates(Set<Entity> entities, double x, double y, double z) {
		sendRiderUpdates(	entities,
							x,
							y,
							z,
							false);
	}

	public static void sendRiderUpdates(Set<Entity> entities, double x, double y, double z, boolean ejectRiders) {
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
				data.writeDouble(curEntity.posY);
				data.writeDouble(curEntity.motionY);
				data.writeBoolean(ejectRiders);
			}
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = PacketLib.UPDATE_RIDERS;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;

			PacketDispatcher.sendPacketToAllAround(	x,
													y,
													z,
													400,
													dimensionID,
													packet);

		} catch (IOException e) {
			CoreLib.say("Error while creating entity update packet.",
						true);
			e.printStackTrace();
		}
	}

	public static boolean requestGUIMapping(World world, ChunkPosition loc, EntityPlayer player) {
		if (world == null || loc == null) {
			return false;
		}
		if (world.getBlockId(	loc.x,
								loc.y,
								loc.z) != ConfigurationLib.Elevator.blockID) {
			return false;
		}

		CoreLib.say((new StringBuilder()).append("Received elevator request from ").append(player.username).toString());

		BlockElevator elevator = (BlockElevator) ConfigurationLib.Elevator;
		TileEntityElevator elevatorInfo = BlockElevator.getTileEntity(	world,
																		loc.x,
																		loc.y,
																		loc.z);

		if (elevatorInfo == null) {
			return false;
		}

		try {
			Packet250CustomPayload packet = elevatorInfo.createPropertiesPacket(true);

			elevatorRequests.put(	player.username,
									loc);
			if (player instanceof EntityPlayerMP) {
				CoreLib.say("Attempting to open GUI via packet");
				PacketDispatcher.sendPacketToPlayer(packet,
													(Player) player);
			} else {
				CoreLib.say("Attempting to open GUI locally");
				((IDECommonProxy) DynamicElevators.proxy).openGui(	world,
																	player,
																	packet,
																	loc);
			}

			CoreLib.say((new StringBuilder()).append("Successfully added request for ").append(player.username).toString());
		} catch (IOException e) {
			CoreLib.say("Error while creating packet - unable to open GUI for "
								+ player.username,
						true);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// Send information back to the server about the selections made by the
	// player in the GUI
	public static boolean sendGUIPacketToServer(Packet250CustomPayload packet) {
		PacketDispatcher.sendPacketToServer(packet);
		return true;
	}

	public static void sendButtonTickUpdate(World world, int x, int y, int z, int metadata) {
		PacketButtonUpdate packet = new PacketButtonUpdate(x, y, z, metadata);
		PacketDispatcher.sendPacketToAllAround(	x,
												y,
												z,
												400,
												world.getWorldInfo().getDimension(),
												packet.getPacket());
	}

	public static void handleButtonUpdatePacket(Player player, PacketButtonUpdate packetBU) {
		if (player instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer) player;
			World world = entityplayer.worldObj;
			if (packetBU.targetExists(world)) {
				int metadata = world.getBlockMetadata(	packetBU.xPosition,
														packetBU.yPosition,
														packetBU.zPosition);
				if ((metadata & 8) != 0) {
					world.setBlockMetadataWithNotify(	packetBU.xPosition,
														packetBU.yPosition,
														packetBU.zPosition,
														metadata & 7,
														3);
					world.markBlockRangeForRenderUpdate(packetBU.xPosition,
														packetBU.yPosition,
														packetBU.zPosition,
														packetBU.xPosition,
														packetBU.yPosition,
														packetBU.zPosition);
				}
			}
		}
	}
}
