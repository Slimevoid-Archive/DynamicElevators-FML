package elevators.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import net.minecraft.src.Block;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import elevators.core.DECore;
import elevators.tileentities.TileEntityElevator;

public class BlockElevatorCaller extends Block {

	public BlockElevatorCaller(int i, Material material) {
		super(i, material);
		this.blockIndexInTexture = Block.stoneOvenIdle.blockIndexInTexture + 17;
		this.setCreativeTab(CreativeTabs.tabTransport);
	}

	private boolean isBeingPoweredByNonElevator(World world, int i, int j, int k) {
		for (int iter = 0; iter < 6; iter++) {
			int tempX = i;
			int tempZ = k;
			int tempY = j;
			if (iter == 0) {
				tempY--;
			} else if (iter == 1) {
				tempY++;
			} else if (iter == 2) {
				tempZ--;
			} else if (iter == 3) {
				tempZ++;
			} else if (iter == 4) {
				tempX--;
			} else if (iter == 5) {
				tempX++;
			}
			int ID = world.getBlockId(tempX, tempY, tempZ);
			DECore.say("Checking: " + tempX + ", " + tempY + ", " + tempZ
					+ ": has block ID" + ID);
			if (ID > 0
					&& ID != DECore.Elevator.blockID
					&& Block.blocksList[ID].isIndirectlyPoweringTo(world,
							tempX, tempY, tempZ, iter)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	public boolean isBlockSolid(IBlockAccess par1IBlockAccess, int par2,
			int par3, int par4, int par5) {
		return true;
	}

	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z,
			ForgeDirection side) {
		return true;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public void updateTick(World world, int x, int y, int z, Random par5Random) {
		if (isBeingPoweredByNonElevator(world, x, y, z) && !previouslyPowered) {
			findAndActivateElevator(world, x, y, z, 0);
			previouslyPowered = true;
		} else if (!isBeingPoweredByNonElevator(world, x, y, z)) {
			previouslyPowered = false;
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k,
			int notifierID) {
		if (notifierID != DECore.ElevatorButton.blockID) {
			if (notifierID <= 0 || notifierID == DECore.Elevator.blockID
					|| !Block.blocksList[notifierID].canProvidePower()
					|| !isBeingPoweredByNonElevator(world, i, j, k)) {
				previouslyPowered = false;
				return;
			}
			if (!previouslyPowered) {
				world.scheduleBlockUpdate(i, j, k, blockID, 2);
			}
		} else {
			boolean foundButton = false;
			for (int iter = 0; iter < 6; iter++) {
				int tempX = i;
				int tempZ = k;
				int tempY = j;
				if (iter == 0) {
					tempX--;
				} else if (iter == 1) {
					tempX++;
				} else if (iter == 2) {
					tempZ--;
				} else if (iter == 3) {
					tempZ++;
				}
				if (world.getBlockId(tempX, j, tempZ) == DECore.ElevatorButton.blockID
						&& (world.getBlockMetadata(tempX, j, tempZ) & 8) > 0) {
					foundButton = true;
				}
			}
			if (foundButton) {
				findAndActivateElevator(world, i, j, k, 0);
			}
		}

	}

	public static boolean findAndActivateElevator(World world, int i, int j,
			int k, int depth) {
		if (depth > 32) {
			return false;
		}

		if (depth == 0) {
			checkedCallers.clear();
			checkedBlocks.clear();
			elvs.clear();
		}

		checkedCallers.add(new ChunkPosition(i, j, k));

		// Look for adjacent elevator caller blocks - if any are found, activate
		// them.
		// Also, if any are found, do not search at this location
		boolean foundOtherCallerBlock = false;
		for (int iter = 0; iter < 6; iter++) {
			int tempX = i;
			int tempY = j;
			int tempZ = k;
			if (iter == 0) {
				tempY--;
			} else if (iter == 1) {
				tempY++;
			} else if (iter == 2) {
				tempZ--;
			} else if (iter == 3) {
				tempZ++;
			} else if (iter == 4) {
				tempX--;
			} else if (iter == 5) {
				tempX++;
			}
			if (world.getBlockId(tempX, tempY, tempZ) == DECore.ElevatorCaller.blockID
					&& !checkedCallers.contains(new ChunkPosition(tempX, tempY,
							tempZ))) {
				findAndActivateElevator(world, tempX, tempY, tempZ, depth + 1);
				foundOtherCallerBlock = true;
			}
		}
		clearSets(depth);

		if (foundOtherCallerBlock) {
			return true;
		}
		// No uncalled elevator callers were found, so search for elevators here
		checkForElevators(world, new ChunkPosition(i, j, k), 0);
		DECore.say("ElevatorCaller activated at: " + i + ", " + j + ", " + k);
		DECore.say((new StringBuilder()).append("Checked ")
				.append(checkedBlocks.size()).append(" blocks").toString());
		DECore.say((new StringBuilder()).append("Found ").append(elvs.size())
				.append(" elevators").toString());
		int dist = 500;
		int destY = -1;
		ChunkPosition newPos = null;
		if (!elvs.isEmpty()) {
			Iterator<ChunkPosition> iter = elvs.iterator();
			while (iter.hasNext()) {
				ChunkPosition curPos = iter.next();
				BlockElevator.refreshAndCombineAllAdjacentElevators(world,
						curPos);
				TileEntityElevator curTile = BlockElevator.getTileEntity(world,
						curPos.x, curPos.y, curPos.z);
				if (curTile != null) {
					int suggestedY = curTile.getClosestYFromYCoor(j);
					if (MathHelper.abs(suggestedY - curPos.y) < dist) {
						dist = (int) MathHelper.abs(suggestedY - curPos.y);
						newPos = curPos;
						destY = suggestedY;
					}
				}
			}
		}
		if (newPos != null) {
			DECore.elevator_demandY(world, newPos, j);
			clearSets(depth);
			return true;
		}
		return false;
	}

	public static void checkForElevators(World world, ChunkPosition pos,
			int numSolid) {
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
			if (pos.y > 0
					&& !DECore.isBlockLedgeMaterial(world, pos.x, pos.y - 1,
							pos.z)) {
				checkForElevators(world, new ChunkPosition(pos.x, pos.y - 1,
						pos.z), numSolid);
			}
			if (pos.y < DECore.max_elevator_Y
					&& !DECore.isBlockLedgeMaterial(world, pos.x, pos.y + 1,
							pos.z)) {
				checkForElevators(world, new ChunkPosition(pos.x, pos.y + 1,
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

	private static void clearSets(int depth) {
		if (depth == 0) {
			elvs.clear();
			checkedBlocks.clear();
			checkedCallers.clear();
		}
	}

	static Set<ChunkPosition> elvs = new HashSet<ChunkPosition>();
	static Set<ChunkPosition> checkedBlocks = new HashSet<ChunkPosition>();
	static Set<ChunkPosition> checkedCallers = new HashSet<ChunkPosition>();

	boolean previouslyPowered = false;
}
