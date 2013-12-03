package slimevoid.elevators.core.lib;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.Configuration;
import slimevoid.elevators.core.DECore;
import cpw.mods.fml.common.registry.EntityRegistry;

public class ConfigurationLib {

	private static Configuration								configuration;

	public static final int										OFFSET							= 256;
	public static final byte									NOUPDATE						= (byte) -1;
	private static final String									PLACEHOLDER						= "-";
	// Checked properties lists for recovering floor names and properties after
	// a GUI change or entity movement
	public static Map<ChunkPosition, Packet250CustomPayload>	checkedProperties				= new HashMap<ChunkPosition, Packet250CustomPayload>();
	// Messages printed in-game by the elevators and elevator buttons
	public static String										message_elevator_called;
	public static String										message_named_elevator_called;
	public static String										message_elevator_notfound;
	public static String										message_elevator_arrival;
	public static String										message_elevator_outoforder;
	public static String										floorName;
	public static String										basementName;
	// Can elevators kill things/people?
	public static boolean										killBelow;
	// Use short circuit evaluation?
	public static boolean										shortCircuit;
	// TODO: Add option for allowing/disallowing elevator options to be
	// changed/accessed
	public static boolean										invertKeys;
	public static boolean										strictShaft;
	public static int											elevator_entityID;
	public static int											max_elevator_Y;
	public static int											guiElevatorID;
	// Which render types cannot be openings?
	public static HashSet<Integer>								disallowed_renderTypes;
	// Which block IDs are allowed to be openings?
	public static Set<Integer>									allowed_blockIDs;
	// Which block IDs are not allowed to be openings?
	public static Set<Integer>									disallowed_blockIDs;
	// Which block IDs are allowed to be ledges?
	public static Set<Integer>									solid_allowed_blockIDs;
	// Which block IDs are not allowed to be ledges?
	public static Set<Integer>									solid_disallowed_blockIDs;

	// TODO: Make these specific to individual elevators using the GUI
	// TODO: Add custom rendering for elevators to allow for individual
	// selection of elevator chunk textures
	public static int											topTextureID;
	public static int											sideTextureID;
	public static int											elevator_button_blockID;
	public static int											elevator_caller_blockID;
	public static int											elevator_blockID;
	public static int											transient_elevator_blockID;
	public static Block											ElevatorButton;
	public static Block											Elevator;
	public static Block											ElevatorCaller;
	public static Block											Transient;
	// Testing mode (having this on slows down the game a bit, at least on my
	// computer)
	public static boolean										verbose;

	private static final String									CATEGORY_ELEVATOR_MESSAGES		= "messages";
	private static final String									CATEGORY_ELEVATOR_RESTRICTIONS	= "restrictions";

	public static void ClientConfig() {

	}

	public static void CommonConfig(File configFile) {
		configuration = new Configuration(configFile);
		configuration.load();
		setupMessages();
		setupProperties();
		setupBlocks();
		setupRestrictions();
		configuration.save();
	}

	private static void setupProperties() {
		// Testing mode (having this on may slow down the game a bit)

		verbose = configuration.get(Configuration.CATEGORY_GENERAL,
									"verbose",
									false).getBoolean(false);

		// Can elevators kill things/people?
		killBelow = configuration.get(	Configuration.CATEGORY_GENERAL,
										"kill_below",
										false).getBoolean(false);

		// Use short circuit evaluation?
		shortCircuit = configuration.get(	Configuration.CATEGORY_GENERAL,
											"shortcircuit_floorRequests",
											false).getBoolean(false);

		// TODO: Add option for allowing/disallowing elevator options to be
		// changed/accessed
		invertKeys = configuration.get(	Configuration.CATEGORY_GENERAL,
										"invertElevatorKeys",
										false).getBoolean(false);
		strictShaft = configuration.get(Configuration.CATEGORY_GENERAL,
										"entireShaftMustBeClear",
										true).getBoolean(true);

		elevator_entityID = configuration.get(	Configuration.CATEGORY_GENERAL,
												"elevator_entityID",
												EntityRegistry.findGlobalUniqueEntityId()).getInt();

		max_elevator_Y = configuration.get(	Configuration.CATEGORY_GENERAL,
											"Max_Elevator_Y",
											255).getInt();

		topTextureID = configuration.get(	Configuration.CATEGORY_GENERAL,
											"top_textureID",
											57).getInt();

		sideTextureID = configuration.get(	Configuration.CATEGORY_GENERAL,
											"side_textureID",
											42).getInt();

		// props.getInt("Elevator_GUI_ID");
	}

	private static void setupMessages() {
		// Messages printed in-game by the elevators and elevator buttons
		message_elevator_called = configuration.get(CATEGORY_ELEVATOR_MESSAGES,
													"elevator_called_message",
													"An elevator is on its way").getString();
		message_named_elevator_called = configuration.get(	CATEGORY_ELEVATOR_MESSAGES,
															"message_named_elevator_called",
															"is on its way").getString();
		message_elevator_notfound = configuration.get(	CATEGORY_ELEVATOR_MESSAGES,
														"message_elevator_notfound",
														"No elevator was found").getString();
		message_elevator_arrival = configuration.get(	CATEGORY_ELEVATOR_MESSAGES,
														"arrival_message",
														"You have arrived at the").getString();
		message_elevator_outoforder = configuration.get(CATEGORY_ELEVATOR_MESSAGES,
														"outoforder_message",
														"Out of order. Please replace elevator.").getString();
		floorName = configuration.get(	CATEGORY_ELEVATOR_MESSAGES,
										"floor_title",
										"Floor").getString();
		basementName = configuration.get(	CATEGORY_ELEVATOR_MESSAGES,
											"basement_title",
											"Basement Level").getString();
	}

	private static void setupBlocks() {
		// TODO: Make these specific to individual elevators using the GUI
		// TODO: Add custom rendering for elevators to allow for individual
		// selection of elevator chunk textures

		elevator_caller_blockID = configuration.get(Configuration.CATEGORY_BLOCK,
													"ElevatorCaller_blockID",
													213).getInt();
		elevator_blockID = configuration.get(	Configuration.CATEGORY_BLOCK,
												"Elevator_blockID",
												214).getInt();
		elevator_button_blockID = configuration.get(Configuration.CATEGORY_BLOCK,
													"ElevatorButton_blockID",
													215).getInt();
		transient_elevator_blockID = configuration.get(	Configuration.CATEGORY_BLOCK,
														"TransientElevator_blockID",
														216).getInt();

	}

	private static void setupRestrictions() {
		// Which render types cannot be openings?
		disallowed_renderTypes = new HashSet<Integer>();
		int[] temp = configuration.get(	CATEGORY_ELEVATOR_RESTRICTIONS,
										"disallowed_renderTypes",
										new int[] {
												0,
												10,
												11,
												13,
												14,
												16,
												17,
												18,
												24,
												25,
												26 }).getIntList();
		for (int i = 0; i < temp.length; i++) {
			disallowed_renderTypes.add(temp[i]);
		}

		// Which block IDs are allowed to be openings?
		allowed_blockIDs = new HashSet<Integer>();
		temp = configuration.get(	CATEGORY_ELEVATOR_RESTRICTIONS,
									"allowed_blockIDs",
									new int[] { 215, 77, 34 }).getIntList();
		for (int i = 0; i < temp.length; i++) {
			allowed_blockIDs.add(temp[i]);
		}

		// Which block IDs are not allowed to be openings?
		disallowed_blockIDs = new HashSet<Integer>();
		temp = configuration.get(	CATEGORY_ELEVATOR_RESTRICTIONS,
									"disallowed_blockIDs",
									new int[] {}).getIntList();
		for (int i = 0; i < temp.length; i++) {
			disallowed_blockIDs.add(temp[i]);
		}

		// Which block IDs are allowed to be ledges?
		solid_allowed_blockIDs = new HashSet<Integer>();
		temp = configuration.get(	CATEGORY_ELEVATOR_RESTRICTIONS,
									"solid_allowed_blockIDs",
									new int[] {}).getIntList();
		for (int i = 0; i < temp.length; i++) {
			solid_allowed_blockIDs.add(temp[i]);
		}

		// Which block IDs are not allowed to be ledges?
		solid_disallowed_blockIDs = new HashSet<Integer>();
		temp = configuration.get(	CATEGORY_ELEVATOR_RESTRICTIONS,
									"solid_disallowed_blockIDs",
									new int[] { 61, 62, 54, 58 }).getIntList();
		for (int i = 0; i < temp.length; i++) {
			solid_disallowed_blockIDs.add(temp[i]);
		}
	}

	public static void finalizeConfiguration() {
		allowed_blockIDs.removeAll(disallowed_blockIDs);
		solid_allowed_blockIDs.removeAll(solid_disallowed_blockIDs);

		solid_allowed_blockIDs.add(ElevatorCaller.blockID);
		solid_disallowed_blockIDs.remove(ElevatorCaller.blockID);
		if (!solid_disallowed_blockIDs.contains(Block.glass.blockID)) {
			solid_allowed_blockIDs.add(Block.glass.blockID);
		}

		if (!disallowed_renderTypes.isEmpty()) {
			DECore.say("Opening: Disallowed render types: "
								+ DECore.convertIntSetToString(disallowed_renderTypes),
						true);
		}

		if (!disallowed_blockIDs.isEmpty()) {
			DECore.say("Opening: Disallowed block IDs: "
								+ DECore.convertIntSetToString(disallowed_blockIDs),
						true);
		}
		if (!allowed_blockIDs.isEmpty()) {
			DECore.say("Opening: Allowed block IDs: "
								+ DECore.convertIntSetToString(allowed_blockIDs),
						true);
		}
		if (!solid_disallowed_blockIDs.isEmpty()) {
			DECore.say("Ledge: Disallowed block IDs: "
								+ DECore.convertIntSetToString(solid_disallowed_blockIDs),
						true);
		}
		if (!solid_allowed_blockIDs.isEmpty()) {
			DECore.say("Ledge: Allowed block IDs: "
								+ DECore.convertIntSetToString(solid_allowed_blockIDs),
						true);
		}
	}
}
