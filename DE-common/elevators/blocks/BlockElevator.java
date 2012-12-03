package elevators.blocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import elevators.core.DECore;
import elevators.core.DEProperties;
import elevators.entities.EntityElevator;
import elevators.tileentities.TileEntityElevator;

public class BlockElevator extends BlockContainer {

	public BlockElevator(int i) {
		super(i, 22, Material.iron);
		this.minX = 0.0D;
		this.maxX = 1.0D;
		this.minY = 0.0D;
		this.maxY = 1.0D;
		this.minZ = 0.0D;
		this.maxZ = 1.0D;
		this.isBlockContainer = true;
		this.setCreativeTab(CreativeTabs.tabTransport);
	}

	private static boolean verbose = true;

	private static void say(String s) {
		say(s, false);
	}

	private static void say(String s, boolean always) {
		if (always || verbose) {
			DECore.say(s, always);
		}
	}

	// -------------------------------------------------------------------- //
	// ------------------ SERVER/CLIENT SENSITIVE CODE! ------------------- //
	static boolean isClient = true;

	private static void openGUI(World world, ChunkPosition loc, EntityPlayer player) {
		DECore.packetHandler.requestGUIMapping(world, loc, player);
	}

	// ---------------- END SERVER/CLIENT SENSITIVE CODE ------------------ //
	// -------------------------------------------------------------------- //

	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		if (isCeiling(meta)) {
			if (side != 0) {
				return DECore.sideTexture;
			} else {
				return DECore.topTexture;
			}
		} else {
			if (side != 1) {
				return DECore.sideTexture;
			} else {
				return DECore.topTexture;
			}
		}
	}

	public static TileEntityElevator getTileEntity(World world, int i, int j, int k) {
		if (isCeiling(world, i, j, k)) {
			return null;
		}
		if (world.getBlockId(i, j, k) != DECore.Elevator.blockID) {
			say("That's not an elevator.");
			return null;
		}
		TileEntityElevator tile = (TileEntityElevator) world
				.getBlockTileEntity(i, j, k);
		if (tile != null && tile instanceof TileEntityElevator) {
			return tile;
		} else {
			world.setBlockTileEntity(i, j, k, new TileEntityElevator());
			TileEntityElevator tileTry2 = (TileEntityElevator) world
					.getBlockTileEntity(i, j, k);
			if (tileTry2 != null && tileTry2 instanceof TileEntityElevator) {
				return tileTry2;
			}
		}
		return null;
	}

	public static TileEntityElevator getTileEntity(IBlockAccess world, int i, int j, int k) {
		if (isCeiling(world.getBlockMetadata(i, j, k))) {
			return null;
		}
		if (world.getBlockId(i, j, k) != DECore.Elevator.blockID) {
			say("That's not an elevator.");
			return null;
		}
		TileEntityElevator tile = (TileEntityElevator) world
				.getBlockTileEntity(i, j, k);
		if (tile != null && tile instanceof TileEntityElevator) {
			return tile;
		}
		return null;
	}

	@Override
	public void onBlockAdded(World world, int i, int j, int k) {
		ChunkPosition curPos = new ChunkPosition(i, j, k);
		updateCeilingStatus(world, i, j, k);
		updateCeilingStatus(world, i, j + 3, k);
		getTileEntity(world, i, j, k);
		refreshAndCombineAllAdjacentElevators(world, curPos, true);
		try {
			checkoutWaitingNames(world, i, j, k);
		} catch (IOException e) {
			say("Unable to load properties.", true);
			e.printStackTrace();
		}
		DECore.elevator_powerOn(world, curPos);
		say("Elevator Added: " + DECore.pos2Str(curPos));
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, int blockID, int metadata) {
		ChunkPosition curPos = new ChunkPosition(i, j, k);
		super.breakBlock(world, i, j, k, blockID, metadata);
		world.removeBlockTileEntity(i, j, k);
		if (hasCeiling(world, i, j, k)) {
			updateCeilingStatus(world, i, j + 3, k);
		}
		refreshAndCombineAllAdjacentElevators(world, curPos);
		notifyNeighbors(world, i, j, k);
		say("Elevator Removed: " + DECore.pos2Str(curPos));

	}

	public static boolean checkoutWaitingNames(World world, int i, int j, int k) throws IOException {
		TileEntityElevator curTile = getTileEntity(world, i, j, k);
		say("Looking for waiting property entries...");
		if (curTile != null) {
			// Check to see if we have mappings waiting to be claimed
			return curTile.checkoutData();
		}
		return false;
	}

	public static boolean hasCeiling(World world, ChunkPosition pos) {
		return hasCeiling(world, pos.x, pos.y, pos.z);
	}

	public static boolean hasCeiling(World world, int i, int j, int k) {
		if (j > DECore.max_elevator_Y - 3) {
			return false;
		}
		if (isCeiling(world, i, j, k)) {
			return false;
		}
		return (world.getBlockId(i, j + 3, k) == DECore.Elevator.blockID);
	}

	public static boolean hasFloor(World world, int i, int j, int k) {
		if (j < 3) {
			return false;
		}
		return (world.getBlockId(i, j - 3, k) == DECore.Elevator.blockID);
	}

	public static boolean isCeiling(World world, ChunkPosition pos) {
		return isCeiling(world, pos.x, pos.y, pos.z);
	}

	public static boolean isCeiling(World world, int i, int j, int k) {
		return isCeiling(world.getBlockMetadata(i, j, k));
	}

	public static boolean isCeiling(int meta) {
		return ((meta & 0x01) == 1);
	}

	public void updateCeilingStatus(World world, int i, int j, int k) {
		if (world.getBlockId(i, j, k) != DECore.Elevator.blockID) {
			return;
		}
		int metadata = world.getBlockMetadata(i, j, k);
		if (hasFloor(world, i, j, k) && !isCeiling(world, i, j, k)) {
			metadata |= 0x01;
			world.setBlockMetadataWithNotify(i, j, k, metadata);
		} else if (!hasFloor(world, i, j, k) && isCeiling(world, i, j, k)) {
			dropBlockAsItem(world, i, j, k, 0, 0);
			world.setBlockWithNotify(i, j, k, 0);
		}
	}

	public static boolean hasOpening(World world, ChunkPosition pos, int origY, boolean strict, boolean testingShaft) {
		return hasOpening(
				world,
				pos.x,
				pos.y,
				pos.z,
				origY,
				strict,
				testingShaft);
	}

	public static boolean hasOpening(World world, int x, int y, int z, int origY, boolean strict, boolean testingShaft) {
		boolean ceil = hasCeiling(world, x, origY, z);
		int max = ceil ? y + 3 : y + 2;

		if (y != origY && !world.isAirBlock(x, y, z) && !(ceil && y == origY + 3) && !testingShaft) {
			return false;
		}
		if (!strict && !ceil) {
			return true;
		}

		for (int testY = y + 1; testY <= max; testY++) {
			boolean valid = DECore.isBlockOpeningMaterial(world, x, testY, z) || origY == testY;
			valid = valid || (ceil && testY == origY + 3);
			if (!valid) {
				return false;
			}
		}

		return true;
	}

	public static boolean hasPossibleFloor(World world, int x, int y, int z, int origY) {
		// Check the current location
		if (!hasOpening(world, x, y, z, origY, true, false)) {
			return false;
		}
		if (hasCeiling(world, x, origY, y) && (y + 3) >= DECore.max_elevator_Y) {
			return false;
		}

		// If all of those didn't already kick us out of the function, check for
		// ledges
		for (int iter = 0; iter < 4; iter++) {
			int tempX = x;
			int tempZ = z;
			if (iter == 0) {
				tempZ--;
			} else if (iter == 1) {
				tempZ++;
			} else if (iter == 2) {
				tempX--;
			} else if (iter == 3) {
				tempX++;
			}
			if (DECore.isBlockLedgeMaterial(world, tempX, y, tempZ) && DECore
					.isBlockOpeningMaterial(world, tempX, y + 1, tempZ) && DECore
					.isBlockOpeningMaterial(world, tempX, y + 2, tempZ)) {
				return true;
			}
		}
		return false;
	}

	public static Set<Integer> refreshElevator(World world, ChunkPosition pos) {
		return refreshElevator(world, pos.x, pos.y, pos.z);
	}

	public static Set<Integer> refreshElevator(World world, int x, int y, int z) {

		Set<Integer> blockNum = new HashSet<Integer>();
		if (world.getBlockId(x, y, z) != DECore.Elevator.blockID) {
			return blockNum;
		}

		int curFloor = 0;
		boolean betweenFloors = true;

		for (int curY = y; curY > 0 && (curY == y || DECore
				.isBlockOpeningMaterial(world, x, curY, z)); curY--) {
			if (hasPossibleFloor(world, x, curY, z, y)) {
				blockNum.add(curY);
				if (curY == y) {
					betweenFloors = false;
				}
			}
		}

		boolean addCheck = true;
		for (int curY = y + 1; curY < (DECore.max_elevator_Y - 3) && (DECore
				.isBlockOpeningMaterial(world, x, curY, z) || (curY == y + 3 && hasCeiling(
				world,
				x,
				y,
				z))); curY++) {
			if (hasPossibleFloor(world, x, curY, z, y)) {
				blockNum.add(curY);
				addCheck = false;
			}
		}

		TileEntityElevator elevatorInfo = getTileEntity(world, x, y, z);
		if (elevatorInfo != null) {
			elevatorInfo.setFloors(blockNum);
		}

		return blockNum;
	}

	public static boolean isReachable(World world, ChunkPosition testFloor, int origY) {
		return isReachable(world, testFloor, origY, DECore.strictShaft);
	}

	public static boolean isReachable(World world, ChunkPosition testFloor, int origY, boolean strict) {
		if (testFloor.y <= 0 || testFloor.y >= DECore.max_elevator_Y) {
			return false;
		}
		if (hasCeiling(world, testFloor.x, origY, testFloor.z) && (testFloor.y + 3) >= DECore.max_elevator_Y) {
			return false;
		}

		if (origY < testFloor.y) {
			for (int testY = origY; testY <= testFloor.y; testY++) {
				if (!hasOpening(
						world,
						testFloor.x,
						testY,
						testFloor.z,
						origY,
						strict,
						(testY != origY && testY != testFloor.y))) {
					say("Blocked at: " + testFloor.x + ", " + testY + ", " + testFloor.z);
					return false;
				}
			}
		} else if (origY == testFloor.y) {
			return true;
		} else {
			for (int testY = origY; testY >= testFloor.y; testY--) {
				if (!hasOpening(
						world,
						testFloor.x,
						testY,
						testFloor.z,
						origY,
						strict,
						(testY != origY && testY != testFloor.y))) {
					say("Blocked at: " + testFloor.x + ", " + testY + ", " + testFloor.z);
					return false;
				}
			}
		}
		return true;
	}

	public static void refreshAndCombineAllAdjacentElevators(World world, ChunkPosition pos) {
		refreshAndCombineAllAdjacentElevators(world, pos, false);
	}

	public static void refreshAndCombineAllAdjacentElevators(World world, ChunkPosition pos, boolean justAdded) {
		DEProperties props = new DEProperties();

		boolean updateProperties = false;

		TileEntityElevator thisTile = getTileEntity(world, pos.x, pos.y, pos.z);

		if (!justAdded && thisTile != null) {
			try {
				updateProperties = checkoutWaitingNames(
						world,
						pos.x,
						pos.y,
						pos.z);
				if (updateProperties) {
					props.mergeProperties(thisTile);
				}
			} catch (IOException e) {
				say("Unable to interpret or merge properties", true);
				e.printStackTrace();
				updateProperties = false;
			}
		}
		say("Combining adjacent elevators...");

		resetAdjacenciesList(world, pos);
		Iterator<ChunkPosition> iter = conjoinedElevators.iterator();
		Set<Integer> floorsList = new HashSet<Integer>();
		while (iter.hasNext()) {
			ChunkPosition curPos = iter.next();
			floorsList.addAll(refreshElevator(world, curPos));
			if (updateProperties) {
				TileEntityElevator curInfo = getTileEntity(
						world,
						curPos.x,
						curPos.y,
						curPos.z);
				try {
					curInfo.props.mergeProperties(props);
				} catch (IOException e) {
					say("Unable to merge properties", true);
					e.printStackTrace();
				}
			}
		}
		if (verbose) {
			say("----------------------------------------------------");
			say("Testing the following floors: ");
			listFloors(floorsList);
		}
		Iterator<Integer> it = floorsList.iterator();
		Set<Integer> invalids = new HashSet<Integer>();
		while (it.hasNext()) {
			int curTestFloorY = it.next();
			if (verbose) {
				say((new StringBuilder())
						.append("testing y = ")
							.append(curTestFloorY)
							.toString());
			}
			boolean canBeUsed = true;
			for (int i = 0; i < conjoinedElevators.size() && canBeUsed; i++) {
				ChunkPosition curElvPos = conjoinedElevators.get(i);
				if (!isReachable(world, new ChunkPosition(
						curElvPos.x,
							curTestFloorY,
							curElvPos.z), curElvPos.y)) {
					canBeUsed = false;
				}
			}
			if (!canBeUsed) {
				invalids.add(curTestFloorY);
			}
		}
		if (verbose) {
			say("----------------------------------------------------");
			say("The following floors will be removed: ");
			listFloors(invalids);
		}
		floorsList.removeAll(invalids);
		if (verbose) {
			say("----------------------------------------------------");
			say("Populating all adjacent elevators with the following floors: ");
			listFloors(floorsList);
			say("----------------------------------------------------");
		}

		for (int i = 0; i < conjoinedElevators.size(); i++) {
			ChunkPosition curPos = conjoinedElevators.get(i);
			TileEntityElevator curInfo = getTileEntity(
					world,
					curPos.x,
					curPos.y,
					curPos.z);
			if (curInfo != null) {
				curInfo.setFloors(floorsList);
			}
		}
	}

	public static void listFloors(Set<Integer> floors) {
		Iterator<Integer> i = floors.iterator();
		while (i.hasNext()) {
			say((new StringBuilder())
					.append("Floor at y = ")
						.append(i.next())
						.append(".")
						.toString());
		}
	}

	// right clicked?
	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float par7, float par8, float par9) {
		if (world.isRemote) {
			return true;
		}
		updateCeilingStatus(world, i, j, k);
		ItemStack curItem = player.getCurrentEquippedItem();
		if (curItem != null && curItem.itemID == DECore.Elevator.blockID) {
			return false;
		}
		boolean isCeiling = false;
		if (isCeiling(world, i, j, k)) {
			j -= 3;
			isCeiling = true;
		}
		say("----------------------------------------------------");
		say("Elevator Activated! - at " + i + ", " + j + ", " + k);
		ChunkPosition loc = new ChunkPosition(i, j, k);
		refreshAndCombineAllAdjacentElevators(world, loc);

		TileEntityElevator elevatorInfo = getTileEntity(world, i, j, k);

		if (elevatorInfo == null) {
			player.addChatMessage(DECore.message_elevator_outoforder);
			return true;
		}

		int floorsBelow = elevatorInfo.floorsBelow();
		int floorsAbove = elevatorInfo.floorsAbove();
		int curFloor = elevatorInfo.curFloor();

		say("Floors Below: " + floorsBelow);
		say("Floors Above: " + floorsAbove);
		say("Current floor: " + curFloor);
		say("Player is on elevator: " + isEntityOnThisElevator(
				world,
				loc,
				player));

		/*
		 * if (mod_Elevator.canShortCircuit(player.username) && floorsBelow == 1
		 * && ( (!isCeiling && hasCeiling(world, loc)) || floorsAbove == 0) &&
		 * isEntityOnThisElevator(world, loc, player)) {
		 * say("'Short circuit' request to bottom floor!");
		 * mod_Elevator.elevator_requestFloor(world, loc, curFloor - 1); } else
		 * if (mod_Elevator.canShortCircuit(player.username) && floorsAbove == 1
		 * && (isCeiling || floorsBelow == 0) && isEntityOnThisElevator(world,
		 * loc, player)) { say("'Short circuit' request to top floor!");
		 * mod_Elevator.elevator_requestFloor(world, loc, curFloor + 1); } else
		 * {
		 */
		openGUI(world, loc, player);
		// }
		say("----------------------------------------------------");
		return true;
	}

	public boolean isEntityOnThisElevator(World world, ChunkPosition pos, EntityPlayer player) {
		Iterator<ChunkPosition> iter = conjoinedElevators.iterator();
		while (iter.hasNext()) {
			if (DECore.isEntityOnBlock(world, iter.next(), player)) {
				return true;
			}
		}
		return false;
	}

	public static void resetAdjacenciesList(World world, ChunkPosition startPos) {
		if (!conjoinedElevators.isEmpty()) {
			conjoinedElevators.clear();
		}
		populateAdjacenciesList(world, startPos, 0);
	}

	public static void populateAdjacenciesList(World world, ChunkPosition pos, int dist) {
		conjoinedElevators.add(pos);
		if (dist > 127) {
			return;
		}
		for (int iter = 0; iter < 4; iter++) {
			int curX = pos.x;
			int curZ = pos.z;
			if (iter == 0) {
				curX++;
			} else if (iter == 1) {
				curX--;
			} else if (iter == 2) {
				curZ++;
			} else if (iter == 3) {
				curZ--;
			}

			ChunkPosition curPos = new ChunkPosition(curX, pos.y, curZ);
			if (!conjoinedElevators.contains(curPos) && world.getBlockId(
					curX,
					pos.y,
					curZ) == DECore.Elevator.blockID) {
				populateAdjacenciesList(world, curPos, dist + 1);
			}
		}
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean isIndirectlyPoweringTo(IBlockAccess world, int i, int j, int k, int l) {
		return isPoweringTo(world, i, j, k, l);
	}

	@Override
	public boolean isPoweringTo(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		if (isCeiling(iblockaccess.getBlockMetadata(i, j, k))) {
			return false;
		}
		if (l == 0) {
			return false;
		} else {
			TileEntityElevator elevatorInfo = getTileEntity(
					iblockaccess,
					i,
					j,
					k);

			if (elevatorInfo == null) {
				return false;
			}
			return elevatorInfo.getProvidesPower();
		}
	}

	@Override
	public void updateTick(World world, int i, int j, int k, Random random) {
		ChunkPosition curPos = new ChunkPosition(i, j, k);

		TileEntityElevator elevatorInfo = getTileEntity(world, i, j, k);
		if (elevatorInfo == null) {
			return;
		}

		int curState = elevatorInfo.getCurrentState();
		int curDest = elevatorInfo.getDestination();
		boolean providesPower = elevatorInfo.getProvidesPower();

		say("Elevator Updated at " + DECore.pos2Str(curPos));
		say("Current Y: " + j + ", requested Y: " + curDest + "; Proving power: " + providesPower);

		refreshAndCombineAllAdjacentElevators(world, curPos);

		switch (curState) {
		case NO_ACTION:
			say("No action is currently required");
			if (providesPower) {
				break;
			}
		case POWER_ON:
			say("Block needs to be powered on!");
			toggleConjoinedPower(world, true);
			notifyNeighbors(world, i, j, k);
			elevatorInfo.clearState();
			break;
		case DEMAND_NEW_FLOOR:
			say("Block has been demanded to " + curDest);
			for (int iter = 0; iter < conjoinedElevators.size() && (curDest != j); iter++) {
				ChunkPosition conjPos = conjoinedElevators.get(iter);
				if (!isReachable(world, new ChunkPosition(
						conjPos.x,
							curDest,
							conjPos.z), conjPos.y, false)) {
					curDest = j;
					say("Unable to meet demand - part of the elevator is blocked!");
					say("Elevator chunk blocked at: " + DECore.pos2Str(conjPos));
				}
			}
			if (curDest == j) {
				elevatorInfo.clearState();
				break;
			}
		case REQUEST_NEW_FLOOR:
			if (providesPower && curDest != j) {
				say("Block has been demanded to " + curDest + " - toggling power in preparation for travel");
				toggleConjoinedPower(world, false);
				// Refresh for actual movement
				DECore.refreshElevator(world, curPos, 2);
			} else if (!world.isRemote && (curDest != j) && (elevatorInfo
					.hasFloorAt(curDest) || curState == DEMAND_NEW_FLOOR)) {
				say("Current is not the same as requested! Move requested!");

				Set<EntityElevator> allEntities = new HashSet<EntityElevator>();
				EntityElevator centerElevator = null;

				for (int iter = 0; iter < conjoinedElevators.size(); iter++) {
					ChunkPosition pos = conjoinedElevators.get(iter);

					say("Adjoined at " + pos.x + ", " + pos.y + ", " + pos.z);
					TileEntityElevator curInfo = getTileEntity(
							world,
							pos.x,
							pos.y,
							pos.z);

					if (curInfo != null && !isCeiling(world, pos)) {
						curInfo.demandY(curDest);

						int metadata = world.getBlockMetadata(
								pos.x,
								pos.y,
								pos.z);
						boolean isCenter = (pos.x == i && pos.y == j && pos.z == k); // ||
																						// isClient;
						EntityElevator curElevator = new EntityElevator(
								world,
									pos.x,
									pos.y,
									pos.z);
						if (hasCeiling(world, pos)) {
							// Create ceiling entity
							EntityElevator ceilingElevator = new EntityElevator(
									world,
										pos.x,
										pos.y + 3,
										pos.z);

							ceilingElevator.setProperties(
									curDest + 3,
									false,
									isClient,
									world.getBlockMetadata(
											pos.x,
											pos.y + 3,
											pos.z));

							// Set current elevator's ceiling as this ceiling
							curElevator.joinToCeiling(ceilingElevator);

							world.spawnEntityInWorld(ceilingElevator);
						}
						curElevator.setProperties(
								curDest,
								isCenter,
								isClient,
								metadata);
						world.spawnEntityInWorld(curElevator);
					}
				}
				elevatorInfo.clearState();
			} else {
				elevatorInfo.clearState();
			}

			break;
		}
		// End switch statement
	}

	public void toggleConjoinedPower(World world, boolean newPowerState) {
		for (int iter = 0; iter < conjoinedElevators.size(); iter++) {
			ChunkPosition pos = conjoinedElevators.get(iter);
			TileEntityElevator curInfo = getTileEntity(
					world,
					pos.x,
					pos.y,
					pos.z);
			if (curInfo != null) {
				curInfo.setPower(newPowerState);
			}
		}
	}

	public void notifyNeighbors(World world, int i, int j, int k) {
		say("Notifying neighbors of change...");
		world.notifyBlocksOfNeighborChange(i, j, k, blockID);
		world.notifyBlocksOfNeighborChange(i - 1, j, k, blockID);
		world.notifyBlocksOfNeighborChange(i + 1, j, k, blockID);
		world.notifyBlocksOfNeighborChange(i, j, k - 1, blockID);
		world.notifyBlocksOfNeighborChange(i, j, k + 1, blockID);
	}

	static List<ChunkPosition> conjoinedElevators = new ArrayList<ChunkPosition>();

	public static final int NO_ACTION = 0;
	public static final int REQUEST_NEW_FLOOR = 1;
	public static final int DEMAND_NEW_FLOOR = 2;
	public static final int POWER_ON = 3;

	public static final int NO_FLOOR = 0;

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityElevator();
	}
}