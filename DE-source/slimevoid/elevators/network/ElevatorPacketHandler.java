package slimevoid.elevators.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import slimevoid.elevators.api.IDECommonProxy;
import slimevoid.elevators.blocks.BlockElevator;
import slimevoid.elevators.core.DECore;
import slimevoid.elevators.core.DEProperties;
import slimevoid.elevators.core.DynamicElevators;
import slimevoid.elevators.core.lib.BlockLib;
import slimevoid.elevators.core.lib.ConfigurationLib;
import slimevoid.elevators.core.lib.GuiLib;
import slimevoid.elevators.core.lib.PacketLib;
import slimevoid.elevators.entities.EntityElevator;
import slimevoid.elevators.network.packets.PacketButtonUpdate;
import slimevoid.elevators.tileentities.TileEntityElevator;
import slimevoidlib.network.PacketIds;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ElevatorPacketHandler implements IConnectionHandler,
		IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(packet.data));

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
			if (packet.channel.equals(PacketLib.GUI_REQUEST)) {
				// Attempt to open GUI screen with received data
				// EURY EDIT
				((IDECommonProxy) DynamicElevators.proxy).openGui(	world,
																	null,
																	packet,
																	null);
			} else if (packet.channel.equals(PacketLib.GUI_DATA)) {
				DEProperties props = new DEProperties();

				props.readInData(packet);

				int command = props.command;

				ChunkPosition pos = PacketLib.elevatorRequests.get(playerMP.username);
				if (pos == null) {
					return;
				}

				if (world.getBlockId(	pos.x,
										pos.y,
										pos.z) != ConfigurationLib.Elevator.blockID) {
					return;
				}
				BlockElevator elevator = (BlockElevator) ConfigurationLib.Elevator;
				TileEntityElevator tile = BlockElevator.getTileEntity(	world,
																		pos.x,
																		pos.y,
																		pos.z);
				if (tile == null) {
					return;
				}

				DECore.say("Received elevator response from "
							+ playerMP.username + " requesting GUI command "
							+ command);

				switch (command) {
				case GuiLib.GUI_OPTIONS_APPLY:
					ConfigurationLib.checkedProperties.put(	pos,
															packet);
					BlockLib.refreshElevator(	world,
												pos);
					break;
				case GuiLib.GUI_RESET:
					BlockLib.elevator_reset(world,
											pos);
					if (PacketLib.elevatorRequests.containsKey(playerMP.username)) {
						PacketLib.elevatorRequests.remove(playerMP.username);
					}
					break;
				default:
					if (command < 1
						|| command > ConfigurationLib.max_elevator_Y) {
						break;
					}
					if (command > tile.numFloors()
						|| command == tile.curFloor()) {
						break;
					}
					BlockLib.elevator_requestFloor(	world,
													pos,
													command);
					if (PacketLib.elevatorRequests.containsKey(playerMP.username)) {
						PacketLib.elevatorRequests.remove(playerMP.username);
					}
					break;
				}
			}
			// Elevator/Rider update request received
			else if (packet.channel.equals(PacketLib.UPDATE_RIDERS)) {
				// Find all entities corresponding to the given entity ids and
				// update them to given y coords

				// Size (1 integer)
				int numEntities = dataStream.readInt();

				for (int i = 0; i < numEntities; i++) {
					int entityID = dataStream.readInt(); // ID
					double posY = dataStream.readDouble();
					double motionY = dataStream.readDouble();
					boolean ejectRiders = dataStream.readBoolean();
					// int entity_data = dataStream.readInt(); // Data

					Entity entity = ((EntityPlayer) player).worldObj.getEntityByID(entityID);
					DECore.say("Received request for entity id " + entityID
								+ " to be set to Y: " + posY);
					if (entity != null) {
						if (entity instanceof EntityElevator) {
							EntityElevator curElevator = (EntityElevator) entity;
							curElevator.setPosition(entity.posX,
													posY,
													entity.posZ);
							if (ejectRiders) {
								entity.updateRiderPosition();
							}
						} else {
							if (entity instanceof EntityLiving) {
								// if (entity.entityId ==
								// FMLClientHandler.instance().getClient().thePlayer.entityId)
								// {
								// System.out.println("Is Me!");
								// } else {
								entity.posY = posY;
								entity.motionY = motionY;
								entity.fallDistance = 0;
								// entity.onGround = true;
								// entity.isCollidedVertically = true;
								// }
							} else {
								entity.motionY = motionY;
								// entity.onGround = false;
							}
							if (ejectRiders) {
								entity.motionY = 0.1;
								entity.fallDistance = 0;
								entity.onGround = true;
							}
						}

						DECore.say("Entity with id " + entity.entityId
									+ " was set to " + motionY);
					} else {
						DECore.say("Entity with that ID does not exist");
					}
				}
			} else if (packet.channel.equals(PacketLib.ELEVATOR_PROPERTIES)) {
				int entityID = dataStream.readInt();
				int dest = dataStream.readInt();
				boolean center = dataStream.readBoolean();
				int metadata = dataStream.readInt();

				DECore.say("Received prop update info for elevator id "
							+ entityID);

				Entity entity = world.getEntityByID(entityID);
				if (entity == null || !(entity instanceof EntityElevator)) {
					DECore.say("Entity with that ID does not exist");
				} else {
					EntityElevator elevator = (EntityElevator) entity;
					elevator.setProperties(	dest,
											center,
											true,
											metadata);
				}
			} else if (packet.channel.equals(PacketLib.BLOCK_UPDATE)) {
				int packetID = dataStream.read();
				if (packetID == PacketIds.UPDATE) {
					PacketButtonUpdate packetBU = new PacketButtonUpdate();
					packetBU.readData(dataStream);
					PacketLib.handleButtonUpdatePacket(	player,
														packetBU);
				}
			}
		} catch (IOException e) {
			// Send error packet if something goes wrong
			if (packet.channel.equals(PacketLib.GUI_DATA)) {
				Packet250CustomPayload responsePacket = new Packet250CustomPayload();
				responsePacket.channel = PacketLib.GUI_COMMUNICATION_ERROR;
				responsePacket.data = new byte[1];
				responsePacket.data[0] = 0x01;
				responsePacket.length = 1;

				PacketDispatcher.sendPacketToServer(responsePacket);

			}
			DECore.say(	"Error while reading incoming packet.",
						true);
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
		if (ConfigurationLib.shortCircuit) {
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = PacketLib.SHORT_CIRCUIT;
			packet.data = new byte[1];
			packet.data[0] = 0x11;
			packet.length = 1;
			PacketDispatcher.sendPacketToServer(packet);
		}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
	}

	@Override
	public void connectionClosed(INetworkManager manager) {
		// ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		// DataOutputStream data = new DataOutputStream(bytes);
		//
		// Packet250CustomPayload packet = new Packet250CustomPayload();
		// packet.channel = "UNREGISTER";
		//
		// try {
		// for (int i = 0; i < PacketLib.CHANNELS.length; i++) {
		// data.writeUTF(PacketLib.CHANNELS[i]);
		// }
		//
		// packet.data = bytes.toByteArray();
		// packet.length = packet.data.length;
		// PacketDispatcher.sendPacketToServer(packet);
		// } catch (Exception e) {
		// DECore.say("Unable to unregister channels!!",
		// true);
		// }
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
		// DECore.say("Sending channel registration packet...");
		// ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		// DataOutputStream data = new DataOutputStream(bytes);
		//
		// Packet250CustomPayload packet = new Packet250CustomPayload();
		// packet.channel = "REGISTER";
		//
		// try {
		// for (int i = 0; i < PacketLib.CHANNELS.length; i++) {
		// data.writeUTF(PacketLib.CHANNELS[i]);
		// }
		//
		// packet.data = bytes.toByteArray();
		// packet.length = packet.data.length;
		// PacketDispatcher.sendPacketToServer(packet);
		// } catch (Exception e) {
		// DECore.say("Unable to register channels!!",
		// true);
		// }
	}
}
