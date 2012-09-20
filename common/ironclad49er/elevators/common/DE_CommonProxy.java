package ironclad49er.elevators.common;

import net.minecraft.src.*;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.network.IGuiHandler;

public class DE_CommonProxy implements IGuiHandler {
	public static void registerRenderInformation() {
	         //No rendering for servers.
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
}
