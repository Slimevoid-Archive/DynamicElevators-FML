package slimevoid.elevators.core;

import java.io.File;

import slimevoid.elevators.core.lib.CoreLib;
import slimevoid.elevators.entities.EntityElevator;
import slimevoid.elevators.tileentities.TileEntityElevator;
import slimevoidlib.core.SlimevoidCore;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class DEInit {
	private static boolean initialized = false;

	public static void initialize() {
		if (initialized)
			return;
		initialized = true;
		DECore.props = new Props(
				new File(
						DynamicElevators.proxy.getMinecraftDir() + "/config/DynamicElevators.cfg")
						.getPath());
		load();
		DECore.props.save();
	}

	public static void load() {
		SlimevoidCore.console(CoreLib.MOD_NAME, "Registering Items...");

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

		DynamicElevators.proxy.registerRenderInformation();

		DECore.addConfig();

		DECore.addNames();
		DECore.addRecipes();

		DECore.registerPackets();
	}

}
