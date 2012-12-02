package elevators.blocks;

// D.E. - 1.6
import static net.minecraftforge.common.ForgeDirection.EAST;
import static net.minecraftforge.common.ForgeDirection.NORTH;
import static net.minecraftforge.common.ForgeDirection.SOUTH;
import static net.minecraftforge.common.ForgeDirection.WEST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import net.minecraft.src.BlockButton;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import elevators.core.DECore;
import elevators.network.ElevatorPacketHandler;
import elevators.tileentities.TileEntityElevator;

public class BlockElevatorButton extends BlockButton {

	public BlockElevatorButton(int i, int j, boolean sensible) {
		super(i, j, sensible);
		this.setCreativeTab(CreativeTabs.tabTransport);
	}

	Set<ChunkPosition> elvs = new HashSet<ChunkPosition>();
	Set<ChunkPosition> checkedBlocks = new HashSet<ChunkPosition>();

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float par7, float par8, float par9) {
		int metadata = world.getBlockMetadata(i, j, k);
		int direction = metadata & 7;
		int state = 8 - (metadata & 8);
		if (state == 0) {
			return true;
		}
		world.setBlockMetadataWithNotify(i, j, k, direction + state);
		world.markBlocksDirty(i, j, k, i, j, k);
		world.playSoundEffect(
				i + 0.5D,
				j + 0.5D,
				k + 0.5D,
				"random.click",
				0.3F,
				0.6F);
		world.scheduleBlockUpdate(i, j, k, this.blockID, this.tickRate());
		// world.notifyBlocksOfNeighborChange(i, j, k, blockID);
		ChunkPosition newPos = null;
		if (direction == 1) {
			newPos = new ChunkPosition(i - 1, j, k);
		} else if (direction == 2) {
			newPos = new ChunkPosition(i + 1, j, k);
		} else if (direction == 3) {
			newPos = new ChunkPosition(i, j, k - 1);
		} else if (direction == 4) {
			newPos = new ChunkPosition(i, j, k + 1);
		} else {
			dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
			world.setBlockWithNotify(i, j, k, 0);
		}
		if (newPos == null) {
			return false;
		}

		if (world.getBlockId(newPos.x, newPos.y, newPos.z) == DECore.ElevatorCaller.blockID) {
			boolean foundElevator = BlockElevatorCaller
					.findAndActivateElevator(
							world,
							newPos.x,
							newPos.y,
							newPos.z,
							0);
			if (!world.isRemote && foundElevator) {
				player.addChatMessage(DECore.message_elevator_called);
			} else if (!world.isRemote) {
				player.addChatMessage(DECore.message_elevator_notfound);
			}
			return true;
		}

		checkedBlocks.clear();
		elvs.clear();
		checkForElevators(world, new ChunkPosition(i, j, k), 0);
		checkForElevators(world, newPos, 0);
		DECore.say((new StringBuilder())
				.append("Checked ")
					.append(checkedBlocks.size())
					.append(" blocks")
					.toString());
		DECore.say((new StringBuilder())
				.append("Found ")
					.append(elvs.size())
					.append(" elevators")
					.toString());
		newPos = null;
		int dist = DECore.max_elevator_Y + 5;
		int destY = -1;
		if (!elvs.isEmpty()) {
			Iterator<ChunkPosition> iter = elvs.iterator();
			while (iter.hasNext()) {
				ChunkPosition curPos = iter.next();
				BlockElevator.refreshAndCombineAllAdjacentElevators(
						world,
						curPos);
				TileEntityElevator curTile = BlockElevator.getTileEntity(
						world,
						curPos.x,
						curPos.y,
						curPos.z);
				if (curTile != null) {
					int suggestedY = curTile
							.getClosestFloorFromYCoor_AlwaysDown(j);
					suggestedY = curTile.getYFromFloor(suggestedY);
					DECore.say("closest y: " + suggestedY);
					if (MathHelper.abs(suggestedY - curPos.y) < dist && curTile
							.hasFloorAt(suggestedY)) {
						dist = (int) MathHelper.abs(suggestedY - curPos.y);
						newPos = curPos;
						destY = suggestedY;
					}
				}
			}
		}
		if (newPos != null) {
			TileEntityElevator curTile = BlockElevator.getTileEntity(
					world,
					newPos.x,
					newPos.y,
					newPos.z);
			if (!world.isRemote && destY != newPos.y) {
				if (!curTile.props.getElevatorName().isEmpty()) {
					player
							.addChatMessage(curTile.props.getElevatorName() + " " + DECore.message_named_elevator_called);
				} else {
					player.addChatMessage(DECore.message_elevator_called);
				}
			}
			DECore.elevator_demandY(world, newPos, destY);
		} else {
			if (!world.isRemote) {
				player.addChatMessage(DECore.message_elevator_notfound);
			}
		}

		return true;
	}

	public void checkForElevators(World world, ChunkPosition pos, int numSolid) {
		if (checkedBlocks.contains(pos)) {
			return;
		}
		checkedBlocks.add(pos);
		boolean isCeiling = false;
		if (world.getBlockId(pos.x, pos.y, pos.z) == DECore.Elevator.blockID) {
			if (!BlockElevator.isCeiling(world, pos)) {
				elvs.add(pos);
				return;
			} else {
				isCeiling = true;
			}
		}
		if (isCeiling || DECore.isBlockOpeningMaterial(world, pos)) {
			// mod_Elevator.say((new
			// StringBuilder()).append("Checking ").append(pos.x).append(", ").append(pos.y).append(", ").append(pos.z).toString());
			if (pos.y > 0 && !DECore.isBlockLedgeMaterial(
					world,
					pos.x,
					pos.y - 1,
					pos.z)) {
				checkForElevators(world, new ChunkPosition(
						pos.x,
							pos.y - 1,
							pos.z), numSolid);
			}
			if (pos.y < DECore.max_elevator_Y && !DECore.isBlockLedgeMaterial(
					world,
					pos.x,
					pos.y + 1,
					pos.z)) {
				checkForElevators(world, new ChunkPosition(
						pos.x,
							pos.y + 1,
							pos.z), numSolid);
			}
		} else {
			numSolid++;
			if (numSolid > 2) {
				return;
			}
			for (int iter = 0; iter < 4; iter++) {
				int tempX = pos.x;
				int tempZ = pos.z;
				if (iter == 0) {
					tempZ--;
				} else if (iter == 1) {
					tempZ++;
				} else if (iter == 2) {
					tempX--;
				} else if (iter == 3) {
					tempX++;
				}
				ChunkPosition curPos = new ChunkPosition(tempX, pos.y, tempZ);
				checkForElevators(world, curPos, numSolid);
			}
		}
	}

	public boolean canBePlacedOnBlock(World world, int i, int j, int k) {
		return (world.isBlockNormalCube(i, j, k) || world.getBlockId(i, j, k) == DECore.ElevatorCaller.blockID);
	}

	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	/**
	 * Can this block stay at this position. Similar to canPlaceBlockAt except
	 * gets checked often with plants.
	 */
	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		int side = world.getBlockMetadata(x, y, z) & 7;

		switch (side) {
		case 1:
			return this.canBePlacedOnBlock(world, x - 1, y, z);
		case 2:
			return this.canBePlacedOnBlock(world, x + 1, y, z);
		case 3:
			return this.canBePlacedOnBlock(world, x, y, z - 1);
		case 4:
			return this.canBePlacedOnBlock(world, x, y, z + 1);
		default:
			return false;
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int notifierID) {
		if (!canBlockStay(world, x, y, z)) {
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlockWithNotify(x, y, z, 0);
		}
	}

	public void onBlockRemoval(World par1World, int par2, int par3, int par4) {
	}

    /**
     * Ticks the block if it's been scheduled
     */
	@Override
    public void updateTick(World world, int x, int y, int z, Random random)
    {
        if (!world.isRemote)
        {
            int metadata = world.getBlockMetadata(x, y, z);

            if ((metadata & 8) != 0)
            {            	
                world.setBlockMetadataWithNotify(x, y, z, metadata & 7);
                world.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.click", 0.3F, 0.5F);
                world.markBlocksDirty(x, y, z, x, y, z);
                ElevatorPacketHandler.sendButtonTickUpdate(world, x, y, z, metadata);
            }
        }
    }

	@Override
	public boolean isPoweringTo(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		return false;
	}

	@Override
	public boolean isIndirectlyPoweringTo(IBlockAccess world, int i, int j, int k, int l) {
		return false;
	}

	@Override
	public boolean canProvidePower() {
		return false;
	}
}
