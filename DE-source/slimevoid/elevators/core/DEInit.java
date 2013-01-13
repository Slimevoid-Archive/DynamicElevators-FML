package slimevoid.elevators.core;

import java.io.File;

import slimevoid.elevators.entities.EntityElevator;
import slimevoid.elevators.tileentities.TileEntityElevator;
import slimevoid.lib.ICommonProxy;
import slimevoid.lib.ICore;
import slimevoid.lib.core.Core;
import slimevoid.lib.core.SlimevoidCore;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

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
		SlimevoidCore.console(DEM.getModName(), "Registering Items...");

		DECore.addItems();

		GameRegistry.registerTileEntity(
				TileEntityElevator.class,
				"dynamicelevator");

		EntityRegistry.registerModEntity(
				EntityElevator.class,
				"delv",
				DECore.elevator_entityID,
				DynamicElevators.instance,
				400,
				1,
				true);

		EntityRegistry.instance();
		EntityRegistry.registerGlobalEntityID(
				EntityElevator.class,
				"delv",
				DECore.elevator_entityID);

		DEInit.DEM.getProxy().registerRenderInformation();

		DECore.addConfig();

		DECore.addNames();
		DECore.addRecipes();

		DECore.registerPackets();
	}

}
