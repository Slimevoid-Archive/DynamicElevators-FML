package ironclad49er.elevators.client;

import ironclad49er.elevators.common.DE_CommonProxy;
import ironclad49er.elevators.common.EntityElevator;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod.Instance;

public class DE_ClientProxy extends DE_CommonProxy {
	public static void registerRenderInformation() {
		RenderingRegistry.registerEntityRenderingHandler(EntityElevator.class, new RenderElevator());
	}
}
