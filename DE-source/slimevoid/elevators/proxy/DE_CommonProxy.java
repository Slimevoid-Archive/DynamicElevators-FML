package slimevoid.elevators.proxy;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import slimevoid.elevators.api.IDECommonProxy;
import slimevoid.elevators.core.lib.ConfigurationLib;
import slimevoidlib.IPacketHandling;
import cpw.mods.fml.common.network.Player;

public class DE_CommonProxy implements IDECommonProxy {

	@Override
	public void registerRenderInformation() {
	}

	@Override
	public void openGui(World world, EntityPlayer entityplayer, Packet250CustomPayload packet, ChunkPosition loc) {
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public String getMinecraftDir() {
		return ".";
	}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
	}

	@Override
	public IPacketHandling getPacketHandler() {
		return null;
	}

	@Override
	public void registerTileEntitySpecialRenderer(Class<? extends TileEntity> clazz) {
	}

	@Override
	public void preInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerConfigurationProperties(File configFile) {
		ConfigurationLib.CommonConfig(configFile);
	}

	@Override
	public void registerTickHandlers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerEventHandlers() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isClient(World world) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
		// TODO Auto-generated method stub

	}
}
