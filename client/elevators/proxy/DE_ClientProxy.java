package elevators.proxy;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import elevators.entities.EntityElevator;
import elevators.gui.GuiElevator;
import elevators.render.RenderElevator;

@SideOnly(Side.CLIENT)
public class DE_ClientProxy extends DE_CommonProxy {

	@Override
	public void registerRenderInformation() {
		RenderingRegistry.registerEntityRenderingHandler(EntityElevator.class,
				new RenderElevator());
	}

	@Override
	public String getMinecraftDir() {
		return Minecraft.getMinecraftDir().getPath();
	}

	@Override
	public void openGui(World world, EntityPlayer entityplayer,
			Packet250CustomPayload packet, ChunkPosition loc) {
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
