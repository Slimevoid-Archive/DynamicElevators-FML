package elevators.core;

import java.io.File;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import elevators.entities.EntityElevator;
import elevators.tileentities.TileEntityElevator;
import eurysmods.api.ICommonProxy;
import eurysmods.api.ICore;
import eurysmods.core.Core;
import eurysmods.core.EurysCore;

public class DEInit {
	public static ICore DEM;
	private static boolean initialized = false;

	public static void initialize(ICommonProxy proxy) {
		if (initialized)
			return;
		initialized = true;
		DEM = new Core(proxy);
		DEM.setModName("DynamicElevators");
		DEM.setModChannel("DELEVATORS");
		DECore.props = new Props(
				new File(
						DEM.getProxy().getMinecraftDir() + "/config/DynamicElevators.cfg")
						.getPath());
		load();
		DECore.props.save();
	}

	public static void load() {
		EurysCore.console(DEM.getModName(), "Registering Items...");

		DECore.addItems();

		GameRegistry.registerTileEntity(
				TileEntityElevator.class,
				"dynamicelevator");

		EntityRegistry.instance();
		// EntityRegistry.registerGlobalEntityID(EntityElevator.class,
		// "delv", DECore.elevator_entityID);
		EntityRegistry.registerModEntity(
				EntityElevator.class,
				"delv",
				DECore.elevator_entityID,
				DynamicElevators.instance,
				400,
				1,
				true);

		DEInit.DEM.getProxy().registerRenderInformation();

		DECore.addConfig();

		DECore.addNames();
		DECore.addRecipes();

		DECore.registerPackets();
	}

}
