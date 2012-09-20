package ironclad49er.elevators.common;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class ItemElevator extends ItemBlock {
	
	public ItemElevator(int i) {
		super(i);
	}
	
	@Override
	 public boolean tryPlaceIntoWorld(ItemStack itemstack, EntityPlayer player, World world, int i, int j, int k, int l, float x, float y, float z) {
	    boolean clickElevator = false;
    	if (world.getBlockId(i, j, k) == mod_Elevator.Elevator.blockID) {	
    		clickElevator = true;
    	}
    	//mod_Elevator.say("Collect!", true);
    	if(world.getBlockId(i, j, k) != Block.snow.blockID) {
            if(l == 0) { j--; }
            if(l == 1) { j++; }
            if(l == 2) { k--; }
            if(l == 3) { k++; }
            if(l == 4) { i--; }
            if(l == 5) { i++; }
    	}
    	if (l == 1 && clickElevator) {
    		if(!world.isAirBlock(i, j + 2, k)) { return false; }
            itemstack.stackSize--;
    		world.setBlockAndMetadataWithNotify(i, j + 2, k, mod_Elevator.Elevator.blockID, 0x01);
    	}
    	else if ( !clickElevator || (clickElevator && l != 0) ){
            if(!world.isAirBlock(i, j, k)) { return false; }
            itemstack.stackSize--;
	        world.setBlockWithNotify(i, j, k, mod_Elevator.Elevator.blockID);
    	}
    	
    	return true;
    }
}
