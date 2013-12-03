package slimevoid.elevators.core.lib;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import slimevoid.elevators.blocks.BlockElevator;
import slimevoid.elevators.core.DECore;
import slimevoid.elevators.tileentities.TileEntityElevator;

public class BlockLib {

	private static final String	BLOCK_PREFIX	= "";

	private static final String	BLOCK_TRANSPORT	= "dt.";

	public static final String	BLOCK_ELEVATOR	= BLOCK_TRANSPORT + "elevator";

	public static boolean isBlockOpeningMaterial(World world, ChunkPosition pos) {
		return isBlockOpeningMaterial(	world,
										pos.x,
										pos.y,
										pos.z);
	}

	public static boolean isBlockOpeningMaterial(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x,
														y,
														z)];
		if (block == null) {
			return true;
		}
		if (block.blockMaterial.isLiquid()) {
			return true;
		}
		if (ConfigurationLib.disallowed_blockIDs.contains(world.getBlockId(	x,
																			y,
																			z))) {
			return false;
		}
		if (ConfigurationLib.allowed_blockIDs.contains(world.getBlockId(x,
																		y,
																		z))) {
			return true;
		}
		if (ConfigurationLib.disallowed_renderTypes.contains(block.getRenderType())) {
			return false;
		}
		return true;
	}

	public static boolean isBlockLedgeMaterial(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x,
														y,
														z)];
		if (block == null) {
			return false;
		}
		if (ConfigurationLib.solid_disallowed_blockIDs.contains(world.getBlockId(	x,
																					y,
																					z))) {
			return false;
		}
		if (block.blockMaterial.isLiquid()) {
			return false;
		}
		if (block.blockID == ConfigurationLib.Elevator.blockID) {
			return false;
		}
		if (ConfigurationLib.solid_allowed_blockIDs.contains(world.getBlockId(	x,
																				y,
																				z))) {
			return true;
		}
		if (block.isOpaqueCube()) {
			return true;
		}
		if (block.renderAsNormalBlock()) {
			return true;
		}
		return false;
	}

	// Set elevator block to update after a certain number of ticks (default is
	// 2)
	public static void refreshElevator(World world, ChunkPosition pos) {
		refreshElevator(world,
						pos,
						2);
	}

	public static void refreshElevator(World world, ChunkPosition pos, int delay) {
		int blockID = world.getBlockId(	pos.x,
										pos.y,
										pos.z);
		if (blockID != ConfigurationLib.Elevator.blockID) {
			return;
		}
		world.scheduleBlockUpdate(	pos.x,
									pos.y,
									pos.z,
									blockID,
									delay);
	}

	public static void elevator_requestFloor(World world, ChunkPosition pos, int floor) {
		TileEntityElevator info = BlockElevator.getTileEntity(	world,
																pos.x,
																pos.y,
																pos.z);
		if (info == null) {
			return;
		}
		if (info.requestFloor(floor)) {
			DECore.say("Destination set: " + info.getDestination());
			refreshElevator(world,
							pos,
							10);
		}
	}

	public static void elevator_demandY(World world, ChunkPosition pos, int Y) {
		TileEntityElevator info = BlockElevator.getTileEntity(	world,
																pos.x,
																pos.y,
																pos.z);
		if (info == null) {
			return;
		}
		if (info.demandY(Y)) {
			DECore.say("Destination set: " + info.getDestination());
			refreshElevator(world,
							pos,
							10);
		}
	}

	public static void elevator_reset(World world, ChunkPosition pos) {
		TileEntityElevator info = BlockElevator.getTileEntity(	world,
																pos.x,
																pos.y,
																pos.z);
		if (info == null) {
			return;
		}
		if (info.reset()) {
			DECore.say("Destination set: " + info.getDestination());
			refreshElevator(world,
							pos,
							10);
		}
	}

	public static void elevator_powerOn(World world, ChunkPosition pos) {
		TileEntityElevator info = BlockElevator.getTileEntity(	world,
																pos.x,
																pos.y,
																pos.z);
		if (info == null) {
			return;
		}
		info.setFirstRefresh();
		refreshElevator(world,
						pos,
						10);
	}
}
