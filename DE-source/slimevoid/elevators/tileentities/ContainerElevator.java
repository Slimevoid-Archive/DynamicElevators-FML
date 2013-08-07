package slimevoid.elevators.tileentities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

public class ContainerElevator extends Container {
	
	private TileEntityElevator elevator;
	
	public ContainerElevator(TileEntity tileentity) {
		super();
		if (tileentity instanceof TileEntityElevator) {
			this.elevator = (TileEntityElevator) tileentity;
		}
	}
	
	public TileEntityElevator getElevatorInfo() {
		return this.elevator;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

}
