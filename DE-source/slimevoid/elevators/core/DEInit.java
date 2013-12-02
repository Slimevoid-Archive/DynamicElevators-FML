package slimevoid.elevators.core;

import slimevoid.elevators.core.lib.BlockLib;
import slimevoid.elevators.core.lib.ConfigurationLib;
import slimevoid.elevators.core.lib.CoreLib;
import slimevoid.elevators.entities.EntityElevator;
import slimevoid.elevators.tileentities.TileEntityElevator;
import slimevoidlib.ICommonProxy;
import slimevoidlib.core.SlimevoidCore;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class DEInit {
	private static boolean	initialized	= false;

	public static void initialize(ICommonProxy proxy) {
		if (initialized) return;
		initialized = true;
		load();
	}

	public static void load() {
		SlimevoidCore.console(	CoreLib.MOD_ID,
								"Registering Items...");

		DECore.addItems();

		GameRegistry.registerTileEntity(TileEntityElevator.class,
										BlockLib.BLOCK_ELEVATOR);

		EntityRegistry.registerModEntity(	EntityElevator.class,
											BlockLib.BLOCK_ELEVATOR,
											ConfigurationLib.elevator_entityID,
											DynamicElevators.instance,
											400,
											1,
											true);

		EntityRegistry.registerGlobalEntityID(	EntityElevator.class,
												BlockLib.BLOCK_ELEVATOR,
												ConfigurationLib.elevator_entityID);

		DynamicElevators.proxy.registerRenderInformation();

		DECore.addNames();
		DECore.addRecipes();

		ConfigurationLib.finalizeConfiguration();

		// PacketLib.registerPackets();
	}

}
