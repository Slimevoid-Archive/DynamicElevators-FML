package elevators.api;

import net.minecraft.src.ChunkPosition;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import eurysmods.api.ICommonProxy;

public interface IDECommonProxy extends ICommonProxy {

	void openGui(World world, EntityPlayer player, Packet250CustomPayload packet, ChunkPosition loc);

}
