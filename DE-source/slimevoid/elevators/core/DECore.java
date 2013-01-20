package slimevoid.elevators.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.src.ModLoader;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import slimevoid.elevators.blocks.BlockElevator;
import slimevoid.elevators.blocks.BlockElevatorButton;
import slimevoid.elevators.blocks.BlockElevatorCaller;
import slimevoid.elevators.blocks.BlockTransientElevator;
import slimevoid.elevators.items.ItemElevator;
import slimevoid.elevators.network.ElevatorPacketHandler;
import slimevoid.elevators.tileentities.TileEntityElevator;
import slimevoid.lib.ICommonProxy;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class DECore {

	public static final int GUI_OPTIONS = 198;
	public static final int GUI_RESET = 199;
	public static final int GUI_CANCEL = 200;
	public static final int GUI_OPTIONS_CANCEL = 201;
	public static final int GUI_OPTIONS_SLIDER = 202;
	public static final int GUI_OPTIONS_NAMESLIST = 203;
	public static final int GUI_OPTIONS_FLOORNAME = 204;
	public static final int GUI_OPTIONS_ELEVATORNAME = 205;
	public static final int GUI_OPTIONS_APPLY = 206;
	public static final int GUI_OPTIONS_POWER = 207;
	public static final int GUI_OPTIONS_HALT = 208;
	public static final int GUI_OPTIONS_MOBILE = 209;
	public static final int GUI_RENAME_OK = 210;
	public static final int GUI_RENAME_CANCEL = 211;

	public static final int OFFSET = 256;
	public static final byte NOUPDATE = (byte) -1;
	private static final String PLACEHOLDER = "-";

	protected static Props props;

	// Checked properties lists for recovering floor names and properties after
	// a GUI change or entity movement
	public static Map<ChunkPosition, Packet250CustomPayload> checkedProperties = new HashMap<ChunkPosition, Packet250CustomPayload>();

	// Messages printed in-game by the elevators and elevator buttons
	public static String message_elevator_called;// =
													// props.getString("elevator_called_message");
	public static String message_named_elevator_called;// =
														// props.getString("named_elevator_called_message");
	public static String message_elevator_notfound;// =
													// props.getString("elevator_notfound_message");
	public static String message_elevator_arrival;// =
													// props.getString("arrival_message");
	public static String message_elevator_outoforder;// =
														// props.getString("outoforder_message");

	public static String floorName;// = props.getString("floor_title");
	public static String basementName;// = props.getString("basement_title");

	// Testing mode (having this on slows down the game a bit, at least on my
	// computer)
	public static boolean verbose;// = props.getBoolean("verbose");
	// Can elevators kill things/people?
	public static boolean killBelow;// = props.getBoolean("kill_below");
	// Use short circuit evaluation?
	public static boolean shortCircuit;// =
										// props.getBoolean("shortcircuit_floorRequests");

	// TODO :: Add option for allowing/disallowing elevator options to be changed/accessed
	public static boolean invertKeys;// =
										// props.getBoolean("invertElevatorKeys");
	public static boolean strictShaft;// =
										// props.getBoolean("entireShaftMustBeClear");
	public static int elevator_entityID;// = props.getInt("elevator_entityID");

	public static int max_elevator_Y;// = props.getInt("Max_Elevator_Y");

	static int guiElevatorID;// = props.getInt("Elevator_GUI_ID");

	// Which render types cannot be openings?
	public static Set<Integer> disallowed_renderTypes;// = new
														// HashSet<Integer>();
	// Which block IDs are allowed to be openings?
	public static Set<Integer> allowed_blockIDs;// = new HashSet<Integer>();
	// Which block IDs are not allowed to be openings?
	public static Set<Integer> disallowed_blockIDs;// = new HashSet<Integer>();

	// Which block IDs are allowed to be ledges?
	public static Set<Integer> solid_allowed_blockIDs;// = new
														// HashSet<Integer>();
	// Which block IDs are not allowed to be ledges?
	public static Set<Integer> solid_disallowed_blockIDs;// = new
															// HashSet<Integer>();

	private static String DRT;// =
								// props.getString("opening_disallowed_renderTypes");
	private static String OABID;// =
								// props.getString("opening_allowed_blockIDs");
	private static String ODBID;// =
								// props.getString("opening_disallowed_blockIDs");
	private static String SABID;// = props.getString("solid_allowed_blockIDs");
	private static String SDBID;// =
								// props.getString("solid_disallowed_blockIDs");

	// TODO :: Make these specific to individual elevators using the GUI
	// TODO :: Add custom rendering for elevators to allow for individual
	// selection of elevator chunk textures
	public static int topTexture;// = props.getInt("Elevator_Top_Texture");
	public static int sideTexture;// =
									// props.getInt("Elevator_SideAndBottom_Texture");

	public static int elevator_button_blockID;// =
												// props.getInt("ElevatorButton_blockID");
	public static int elevator_caller_blockID;// =
												// props.getInt("ElevatorCaller_blockID");
	public static int elevator_blockID;// = props.getInt("Elevator_blockID");
	public static int transient_elevator_blockID;// =
													// props.getInt("TransientElevator_blockID");

	public static Block ElevatorButton;// = (new
										// BlockElevatorButton(elevator_button_blockID,
										// Block.blockSteel.blockIndexInTexture,
										// false)).setHardness(0.5F).setStepSound(Block.soundMetalFootstep).setBlockName("elevatorbutton");
	public static Block Elevator;// = (new
									// BlockElevator(elevator_blockID).setHardness(3.0F).setStepSound(Block.soundMetalFootstep).setResistance(15F).setBlockName("elevator"));
	public static Block ElevatorCaller;// = (new
										// BlockElevatorCaller(elevator_caller_blockID,
										// Material.ground)).setHardness(0.5F).setStepSound(Block.soundMetalFootstep).setBlockName("elevatorcaller");
	public static Block Transient;// = (new
									// BlockTransientElevator(transient_elevator_blockID)).setBlockUnbreakable().setBlockName("transient");

	public static void say(String s) {
		say(s, false);
	}

	public static void say(String s, boolean ignoreVerbose) {
		if (verbose || ignoreVerbose) {
			System.out.println("[ElevatorMod] " + s);
		}
	}

	public void initialize(ICommonProxy proxy) {
		DEInit.initialize(proxy);
	}

	public static void addItems() {
		checkProps();
		loadProps();

		say("Starting in verbose mode!");

		ElevatorButton = (new BlockElevatorButton(
				elevator_button_blockID,
					Block.blockSteel.blockIndexInTexture,
					false))
				.setHardness(0.5F)
					.setStepSound(Block.soundMetalFootstep)
					.setBlockName("elevatorbutton");
		Elevator = (new BlockElevator(elevator_blockID)
				.setHardness(3.0F)
					.setStepSound(Block.soundMetalFootstep)
					.setResistance(15F).setBlockName("elevator"));
		ElevatorCaller = (new BlockElevatorCaller(
				elevator_caller_blockID,
					Material.ground))
				.setHardness(0.5F)
					.setStepSound(Block.soundMetalFootstep)
					.setBlockName("elevatorcaller");
		Transient = (new BlockTransientElevator(transient_elevator_blockID))
				.setBlockUnbreakable()
					.setBlockName("transient");

		GameRegistry.registerBlock(Elevator, ItemElevator.class, "Elevator");
		Item.itemsList[Elevator.blockID] = null;
		Item.itemsList[Elevator.blockID] = new ItemElevator(
				Elevator.blockID - OFFSET).setItemName("ElevatorItem");

		GameRegistry.registerBlock(ElevatorButton, "Elevator Button");
		GameRegistry.registerBlock(ElevatorCaller, "Elevator Caller");
		GameRegistry.registerBlock(Transient, "Elevator Transient");
	}

	private static void loadProps() {
		// Messages printed in-game by the elevators and elevator buttons
		message_elevator_called = props.getString("elevator_called_message");
		message_named_elevator_called = props
				.getString("named_elevator_called_message");
		message_elevator_notfound = props
				.getString("elevator_notfound_message");
		message_elevator_arrival = props.getString("arrival_message");
		message_elevator_outoforder = props.getString("outoforder_message");

		floorName = props.getString("floor_title");
		basementName = props.getString("basement_title");

		// Testing mode (having this on slows down the game a bit, at least on
		// my computer)
		verbose = props.getBoolean("verbose");
		// Can elevators kill things/people?
		killBelow = props.getBoolean("kill_below");
		// Use short circuit evaluation?
		shortCircuit = props.getBoolean("shortcircuit_floorRequests");

		// TODO :: Add option for allowing/disallowing elevator options to be changed/accessed
		invertKeys = props.getBoolean("invertElevatorKeys");
		strictShaft = props.getBoolean("entireShaftMustBeClear");
		elevator_entityID = props.getInt("elevator_entityID");

		max_elevator_Y = props.getInt("Max_Elevator_Y");

		props.getInt("Elevator_GUI_ID");

		// Which render types cannot be openings?
		disallowed_renderTypes = new HashSet<Integer>();
		// Which block IDs are allowed to be openings?
		allowed_blockIDs = new HashSet<Integer>();
		// Which block IDs are not allowed to be openings?
		disallowed_blockIDs = new HashSet<Integer>();

		// Which block IDs are allowed to be ledges?
		solid_allowed_blockIDs = new HashSet<Integer>();
		// Which block IDs are not allowed to be ledges?
		solid_disallowed_blockIDs = new HashSet<Integer>();

		DRT = props.getString("opening_disallowed_renderTypes");
		OABID = props.getString("opening_allowed_blockIDs");
		ODBID = props.getString("opening_disallowed_blockIDs");
		SABID = props.getString("solid_allowed_blockIDs");
		SDBID = props.getString("solid_disallowed_blockIDs");

		// TODO :: Make these specific to individual elevators using the GUI
		// TODO :: Add custom rendering for elevators to allow for individual
		// selection of elevator chunk textures
		topTexture = props.getInt("Elevator_Top_Texture");
		sideTexture = props.getInt("Elevator_SideAndBottom_Texture");

		elevator_button_blockID = props.getInt("ElevatorButton_blockID");
		elevator_caller_blockID = props.getInt("ElevatorCaller_blockID");
		elevator_blockID = props.getInt("Elevator_blockID");
		transient_elevator_blockID = props.getInt("TransientElevator_blockID");
	}

	private static void checkProps() {
		props.getInt("TransientElevator_blockID", 216);
		props.getInt("ElevatorButton_blockID", 215);
		props.getInt("Elevator_blockID", 214);
		props.getInt("ElevatorCaller_blockID", 213);

		props.getInt("Max_Elevator_Y", 255);

		props.getInt("Elevator_GUI_ID", 553);
		props.getInt("elevator_entityID", ModLoader.getUniqueEntityId());

		props.getBoolean("invertElevatorKeys", false);
		props.getBoolean("entireShaftMustBeClear", true);
		props.getBoolean("verbose", false);
		props.getBoolean("kill_below", false);
		props.getBoolean("shortcircuit_floorRequests", false);

		props.getInt(
				"Elevator_Top_Texture",
				Block.blockDiamond.blockIndexInTexture);
		props.getInt(
				"Elevator_SideAndBottom_Texture",
				Block.blockSteel.blockIndexInTexture);

		props.getString(
				"opening_disallowed_renderTypes",
				"0, 10, 11, 13, 14, 16, 17, 18, 24, 25, 26");
		props.getString("opening_allowed_blockIDs", "215, 77, 34");
		props.getString("opening_disallowed_blockIDs", "");

		props.getString("solid_allowed_blockIDs", "");
		props.getString("solid_disallowed_blockIDs", "61, 62, 54, 58");

		props.getString("elevator_called_message", "An elevator is on its way");
		props.getString("named_elevator_called_message", "is on its way");
		props.getString("elevator_notfound_message", "No elevator was found");

		props.getString("arrival_message", "You have arrived at the");

		props.getString(
				"outoforder_message",
				"Out of order. Please replace elevator.");

		props.getString("floor_title", "Floor");
		props.getString("basement_title", "Basement Level");

	}

	public static void addNames() {

		// Register names with ModLoader
		LanguageRegistry.addName(Elevator, "Elevator");
		LanguageRegistry.addName(ElevatorButton, "Elevator Button");
		LanguageRegistry.addName(ElevatorCaller, "Elevator Caller");
		LanguageRegistry.addName(Transient, "You shouldn't have this!");
		LanguageRegistry.instance().addStringLocalization(
				"entity.ironclad_elevator.name",
				"en_US",
				"Elevator");

	}

	public static void addRecipes() {
		GameRegistry.addRecipe(new ItemStack(ElevatorButton, 1), new Object[] {
				"I",
				"I",
				Character.valueOf('I'),
				Item.ingotIron });
		GameRegistry.addRecipe(new ItemStack(Elevator, 4), new Object[] {
				"IDI",
				"IRI",
				"III",
				Character.valueOf('I'),
				Item.ingotIron,
				Character.valueOf('D'),
				Item.diamond,
				Character.valueOf('R'),
				Item.redstone });
		GameRegistry.addRecipe(new ItemStack(ElevatorCaller, 1), new Object[] {
				"SSS",
				"SRS",
				"SSS",
				Character.valueOf('S'),
				Block.stone,
				Character.valueOf('R'),
				Item.redstone });
	}

	public static boolean canShortCircuit(String username) {
		return shortCircuit;
	}

	// Determine if a given entity is on a the block at a given chunk position
	public static boolean isEntityOnBlock(World world, ChunkPosition pos, Entity entity) {
		AxisAlignedBB box = Elevator.getCollisionBoundingBoxFromPool(
				world,
				pos.x,
				pos.y,
				pos.z);
		box.maxY = box.maxY + 0.5;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(
				null,
				box);
		return list.contains(entity);
	}

	// Convert chunk position to a printable string
	public static String pos2Str(ChunkPosition pos) {
		return (new StringBuilder())
				.append(pos.x)
					.append(", ")
					.append(pos.y)
					.append(", ")
					.append(pos.z)
					.toString();
	}

	public static boolean isNamed(int curFloor, String[] properties) {
		int index = curFloor + 2;
		if (index >= 3 && index < properties.length) {
			return (properties[index] != null && !properties[index].equals(""));
		}
		return false;
	}

	// Convert a set of integers to a string separated by commas
	public static String convertIntSetToString(Set s) {
		int i = 0;
		String str = "";
		Iterator it = s.iterator();
		while (it.hasNext()) {
			i++;
			str += it.next();
			if (i < s.size()) {
				str += ", ";
			}
		}
		return str;
	}

	// Convert a string separated by commas to a set of integers
	public static void popIntSetFromString(Set s, String str, String def) {
		String[] list = str.split(",", 0);
		try {
			for (int i = 0; i < list.length; i++) {
				if (!list[i].trim().isEmpty()) {
					s.add(Integer.parseInt(list[i].trim()));
				}
			}
		} catch (Exception e) {
			say("There was a problem reading the properties file, using default list instead.");
			s.clear();
			popIntSetFromString(s, def, "");
		}
	}

	public static boolean isBlockOpeningMaterial(World world, ChunkPosition pos) {
		return isBlockOpeningMaterial(world, pos.x, pos.y, pos.z);
	}

	public static boolean isBlockOpeningMaterial(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		if (block == null) {
			return true;
		}
		if (block.blockMaterial.func_85157_q()) {
			return true;
		}
		if (disallowed_blockIDs.contains(world.getBlockId(x, y, z))) {
			return false;
		}
		if (allowed_blockIDs.contains(world.getBlockId(x, y, z))) {
			return true;
		}
		if (disallowed_renderTypes.contains(block.getRenderType())) {
			return false;
		}
		return true;
	}

	public static boolean isBlockLedgeMaterial(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		if (block == null) {
			return false;
		}
		if (solid_disallowed_blockIDs.contains(world.getBlockId(x, y, z))) {
			return false;
		}
		if (block.blockMaterial.func_85157_q()) {
			return false;
		}
		if (block.blockID == Elevator.blockID) {
			return false;
		}
		if (solid_allowed_blockIDs.contains(world.getBlockId(x, y, z))) {
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
		refreshElevator(world, pos, 2);
	}

	public static void refreshElevator(World world, ChunkPosition pos, int delay) {
		int blockID = world.getBlockId(pos.x, pos.y, pos.z);
		if (blockID != Elevator.blockID) {
			return;
		}
		world.scheduleBlockUpdate(pos.x, pos.y, pos.z, blockID, delay);
	}

	public static void elevator_requestFloor(World world, ChunkPosition pos, int floor) {
		TileEntityElevator info = BlockElevator.getTileEntity(
				world,
				pos.x,
				pos.y,
				pos.z);
		if (info == null) {
			return;
		}
		if (info.requestFloor(floor)) {
			say("Destination set: " + info.getDestination());
			refreshElevator(world, pos, 10);
		}
	}

	public static void elevator_demandY(World world, ChunkPosition pos, int Y) {
		TileEntityElevator info = BlockElevator.getTileEntity(
				world,
				pos.x,
				pos.y,
				pos.z);
		if (info == null) {
			return;
		}
		if (info.demandY(Y)) {
			say("Destination set: " + info.getDestination());
			refreshElevator(world, pos, 10);
		}
	}

	public static void elevator_reset(World world, ChunkPosition pos) {
		TileEntityElevator info = BlockElevator.getTileEntity(
				world,
				pos.x,
				pos.y,
				pos.z);
		if (info == null) {
			return;
		}
		if (info.reset()) {
			say("Destination set: " + info.getDestination());
			refreshElevator(world, pos, 10);
		}
	}

	public static void elevator_powerOn(World world, ChunkPosition pos) {
		TileEntityElevator info = BlockElevator.getTileEntity(
				world,
				pos.x,
				pos.y,
				pos.z);
		if (info == null) {
			return;
		}
		info.setFirstRefresh();
		refreshElevator(world, pos, 10);
	}

	public static void addConfig() {

		// --------LEDGES AND OPENINGS--------//
		popIntSetFromString(
				disallowed_renderTypes,
				DRT,
				"0, 10, 11, 13, 14, 16, 17, 18, 24, 25, 26");
		popIntSetFromString(allowed_blockIDs, OABID, "215, 77, 34");
		popIntSetFromString(disallowed_blockIDs, ODBID, "");

		popIntSetFromString(solid_allowed_blockIDs, SABID, "");
		popIntSetFromString(solid_disallowed_blockIDs, SDBID, "61, 62, 54, 58");

		allowed_blockIDs.removeAll(disallowed_blockIDs);
		solid_allowed_blockIDs.removeAll(solid_disallowed_blockIDs);

		solid_allowed_blockIDs.add(ElevatorCaller.blockID);
		solid_disallowed_blockIDs.remove(ElevatorCaller.blockID);
		if (!solid_disallowed_blockIDs.contains(Block.glass.blockID)) {
			solid_allowed_blockIDs.add(Block.glass.blockID);
		}

		if (!disallowed_renderTypes.isEmpty())
			say(
					"Opening: Disallowed render types: " + convertIntSetToString(disallowed_renderTypes),
					true);
		if (!disallowed_blockIDs.isEmpty())
			say(
					"Opening: Disallowed block IDs: " + convertIntSetToString(disallowed_blockIDs),
					true);
		if (!allowed_blockIDs.isEmpty())
			say(
					"Opening: Allowed block IDs: " + convertIntSetToString(allowed_blockIDs),
					true);
		if (!solid_disallowed_blockIDs.isEmpty())
			say(
					"Ledge: Disallowed block IDs: " + convertIntSetToString(solid_disallowed_blockIDs),
					true);
		if (!solid_allowed_blockIDs.isEmpty())
			say(
					"Ledge: Allowed block IDs: " + convertIntSetToString(solid_allowed_blockIDs),
					true);
	}

	public static final ElevatorPacketHandler packetHandler = new ElevatorPacketHandler();

	public static void registerPackets() {
		for (int i = 0; i < ElevatorPacketHandler.CHANNELS.length; i++) {
			NetworkRegistry.instance().registerChannel(
					packetHandler,
					ElevatorPacketHandler.CHANNELS[i]);
		}
	}
}
