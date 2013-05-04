package slimevoid.elevators.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import slimevoid.elevators.core.DECore;

public class ItemElevator extends ItemBlock {

	public ItemElevator(int i) {
		super(i);
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int i, int j, int k, int l, float x, float y, float z) {
		boolean clickElevator = false;
		if (world.getBlockId(i, j, k) == DECore.Elevator.blockID) {
			clickElevator = true;
		}
		// mod_Elevator.say("Collect!", true);
		if (world.getBlockId(i, j, k) != Block.snow.blockID) {
			if (l == 0) {
				j--;
			}
			if (l == 1) {
				j++;
			}
			if (l == 2) {
				k--;
			}
			if (l == 3) {
				k++;
			}
			if (l == 4) {
				i--;
			}
			if (l == 5) {
				i++;
			}
		}
		if (l == 1 && clickElevator) {
			if (!world.isAirBlock(i, j + 2, k)) {
				return false;
			}
			itemstack.stackSize--;
			world.setBlock(
					i,
					j + 2,
					k,
					DECore.Elevator.blockID,
					0x01,
					3);
		} else if (!clickElevator || (clickElevator && l != 0)) {
			if (!world.isAirBlock(i, j, k)) {
				return false;
			}
			itemstack.stackSize--;
			world.setBlock(i, j, k, DECore.Elevator.blockID, 0, 3);
		}

		return true;
	}
}
