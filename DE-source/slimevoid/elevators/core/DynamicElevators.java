package slimevoid.elevators.core;

import slimevoid.elevators.core.lib.CoreLib;
import slimevoid.elevators.core.lib.PacketLib;
import slimevoid.elevators.network.ElevatorPacketHandler;
import slimevoidlib.ICommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
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
				PacketLib.GUI_REQUEST,
				PacketLib.GUI_DATA,
				PacketLib.UPDATE_RIDERS,
				PacketLib.ELEVATOR_PROPERTIES,
				PacketLib.GUI_COMMUNICATION_ERROR,
				PacketLib.SHORT_CIRCUIT,
				PacketLib.BLOCK_UPDATE },
		packetHandler = ElevatorPacketHandler.class,
		connectionHandler = ElevatorPacketHandler.class)
public class DynamicElevators {
	@SidedProxy(
			clientSide = "slimevoid.elevators.client.proxy.DE_ClientProxy",
			serverSide = "slimevoid.elevators.proxy.DE_CommonProxy")
	public static ICommonProxy		proxy;

	@Instance("DynamicElevators")
	public static DynamicElevators	instance;

	@PreInit
	public void DynamicElevatorsPreInit(FMLPreInitializationEvent event) {
		proxy.registerConfigurationProperties(event.getSuggestedConfigurationFile());
	}

	@Init
	public void DynamicElevatorsInit(FMLInitializationEvent evt) {
		DEInit.initialize(proxy);
	}
}