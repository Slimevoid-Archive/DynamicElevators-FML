package slimevoid.elevators.client.proxy;

import java.io.IOException;

import slimevoid.elevators.client.gui.GuiElevator;
import slimevoid.elevators.client.render.RenderElevator;
import slimevoid.elevators.entities.EntityElevator;
import slimevoid.elevators.proxy.DE_CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DE_ClientProxy extends DE_CommonProxy {

	@Override
	public void registerRenderInformation() {
		RenderingRegistry.registerEntityRenderingHandler(
				EntityElevator.class,
				new RenderElevator());
	}

	@Override
	public String getMinecraftDir() {
		return Minecraft.getMinecraftDir().getPath();
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
			e.printStackTrace();
		}
	}
}
