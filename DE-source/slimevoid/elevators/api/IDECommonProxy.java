package slimevoid.elevators.api;

import slimevoid.lib.ICommonProxy;
import net.minecraft.world.ChunkPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;

public interface IDECommonProxy extends ICommonProxy {

	void openGui(World world, EntityPlayer player, Packet250CustomPayload packet, ChunkPosition loc);

}
