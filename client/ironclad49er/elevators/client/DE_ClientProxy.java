package ironclad49er.elevators.client;

import java.io.IOException;

import ironclad49er.elevators.common.DE_CommonProxy;
import ironclad49er.elevators.common.EntityElevator;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod.Instance;

public class DE_ClientProxy extends DE_CommonProxy {
	public void registerRenderInformation() {
		RenderingRegistry.registerEntityRenderingHandler(EntityElevator.class, new RenderElevator());
	}
	
	@Override
	public void openGui(World world, EntityPlayer entityplayer, Packet250CustomPayload packet, ChunkPosition loc) {
		if (entityplayer == null) {
			entityplayer = ModLoader.getMinecraftInstance().thePlayer;
		}
		try {
			if (loc != null) {
				ModLoader.openGUI(entityplayer, new GuiElevator(packet, loc));
			} else {
				ModLoader.openGUI(entityplayer, new GuiElevator(packet));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
