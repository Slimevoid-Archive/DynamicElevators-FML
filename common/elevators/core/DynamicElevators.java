package elevators.core;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import elevators.network.ElevatorPacketHandler;
import eurysmods.api.ICommonProxy;

@Mod(
		modid = "dynamic_elevators",
		name = "Dynamic Elevators",
		version = "1.7.5",
		useMetadata = true
)
@NetworkMod(
		clientSideRequired = true,
		channels = {
				"DE_GUI_DATA",
				"DE_UPDATE",
				"DE_EPROP",
				"DE_ERROR",
				"DE_SHCI"
				},
		packetHandler = ElevatorPacketHandler.class,
		connectionHandler = ElevatorPacketHandler.class
)

public class DynamicElevators {
	@SidedProxy(
			clientSide = "elevators.proxy.DE_ClientProxy",
			serverSide = "elevators.proxy.DE_CommonProxy"
	)
	public static ICommonProxy proxy;

	public static Object instance;

	@Init
	public void load(FMLInitializationEvent evt) {
		instance = this;
		DEInit.initialize(proxy);
	}
}