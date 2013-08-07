package slimevoid.elevators.core;

import slimevoid.elevators.core.lib.CoreLib;
import slimevoid.elevators.network.ElevatorPacketHandler;
import slimevoidlib.ICommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(
		modid = CoreLib.MOD_ID,
		name = CoreLib.MOD_NAME,
		version = CoreLib.MOD_VERSION,
		useMetadata = true,
		dependencies = CoreLib.MOD_DEPENDENCIES)
@NetworkMod(
		clientSideRequired = true,
		channels = {
				"DE_GUI_DATA",
				"DE_UPDATE",
				"DE_EPROP",
				"DE_ERROR",
				"DE_SHCI" },
		packetHandler = ElevatorPacketHandler.class,
		connectionHandler = ElevatorPacketHandler.class)
public class DynamicElevators {
	@SidedProxy(
			clientSide = "slimevoid.elevators.client.proxy.DE_ClientProxy",
			serverSide = "slimevoid.elevators.proxy.DE_CommonProxy")
	public static ICommonProxy proxy;

	@Instance(CoreLib.MOD_ID)
	public static DynamicElevators instance;

	@Init
	public void load(FMLInitializationEvent evt) {
		instance = this;
		DEInit.initialize();
	}
}