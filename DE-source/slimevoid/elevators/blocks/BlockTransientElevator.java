package slimevoid.elevators.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import slimevoid.elevators.entities.EntityElevator;

public class BlockTransientElevator extends Block {

	public BlockTransientElevator(int i) {
		super(i, Material.snow);
		setTickRandomly(true);
	}

	/**
	 * Adds to the supplied array any colliding bounding boxes with the passed
	 * in bounding box. Args: world, x, y, z, axisAlignedBB, arrayList
	 */
	public void getCollidingBoundingBoxes(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, ArrayList par6ArrayList) {
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		return null;
	}

	@Override
	public int tickRate() {
		return 2;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean isProvidingWeakPower(IBlockAccess world, int i, int j, int k, int l) {
		return isProvidingStrongPower(world, i, j, k, l);
	}

	@Override
	public boolean isProvidingStrongPower(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		if (l == 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
		return true;// false;
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public int quantityDropped(Random par1Random) {
		return 0;
	}

	@Override
	public int idDropped(int par1, Random par2Random, int par3) {
		return 0;
	}

	public void notifyExtendedNeighbors(World world, int i, int j, int k, int notifyID) {
		world.notifyBlocksOfNeighborChange(i, j, k, notifyID);
		world.notifyBlocksOfNeighborChange(i - 1, j, k, notifyID);
		world.notifyBlocksOfNeighborChange(i + 1, j, k, notifyID);
		world.notifyBlocksOfNeighborChange(i, j, k - 1, notifyID);
		world.notifyBlocksOfNeighborChange(i, j, k + 1, notifyID);
	}

	@Override
	public void onBlockAdded(World world, int i, int j, int k) {
		if (checkForEntity(world, i, j, k)) {
			world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
			notifyExtendedNeighbors(world, i, j, k, blockID);
		}
	}

	public void onBlockRemoval(World world, int i, int j, int k) {
		notifyExtendedNeighbors(world, i, j, k, blockID);
	}

	private boolean checkForEntity(World world, int i, int j, int k) {
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
				(i),
				(j),
				(k),
				i + 1,
				j + 1,
				k + 1);
		box.expand(0, -0.25D, 0);
		List entities = world.getEntitiesWithinAABBExcludingEntity(null, box);
		Iterator iter = entities.iterator();
		boolean hasEntity = false;
		while (iter.hasNext() && !hasEntity) {
			hasEntity = (iter.next() instanceof EntityElevator);
		}
		if (!hasEntity) {
			world.setBlockWithNotify(i, j, k, 0);
			return false;
		}
		return true;
	}

	@Override
	public void updateTick(World world, int i, int j, int k, Random rand) {
		if (checkForEntity(world, i, j, k)) {
			world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
		}
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return false;
	}
}
