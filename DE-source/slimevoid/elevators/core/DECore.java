package slimevoid.elevators.core;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import slimevoid.elevators.blocks.BlockElevator;
import slimevoid.elevators.blocks.BlockElevatorButton;
import slimevoid.elevators.blocks.BlockElevatorCaller;
import slimevoid.elevators.blocks.BlockTransientElevator;
import slimevoid.elevators.core.lib.ConfigurationLib;
import slimevoid.elevators.core.lib.CoreLib;
import slimevoid.elevators.items.ItemElevator;
import slimevoidlib.ICommonProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class DECore {

	public void initialize(ICommonProxy proxy) {
		DEInit.initialize(proxy);
	}

	public static void addItems() {
		// checkProps();
		// loadProps();

		DECore.say("Starting in verbose mode!");

		ConfigurationLib.ElevatorButton = (new BlockElevatorButton(ConfigurationLib.elevator_button_blockID, false)).setHardness(0.5F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("elevatorbutton");
		ConfigurationLib.Elevator = (new BlockElevator(ConfigurationLib.elevator_blockID).setHardness(3.0F).setStepSound(Block.soundMetalFootstep).setResistance(15F).setUnlocalizedName("elevator"));
		ConfigurationLib.ElevatorCaller = (new BlockElevatorCaller(ConfigurationLib.elevator_caller_blockID, Material.ground)).setHardness(0.5F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("elevatorcaller");
		ConfigurationLib.Transient = (new BlockTransientElevator(ConfigurationLib.transient_elevator_blockID)).setBlockUnbreakable().setUnlocalizedName("transient");

		GameRegistry.registerBlock(	ConfigurationLib.Elevator,
									ItemElevator.class,
									"Elevator");
		Item.itemsList[ConfigurationLib.Elevator.blockID] = null;
		Item.itemsList[ConfigurationLib.Elevator.blockID] = new ItemElevator(ConfigurationLib.Elevator.blockID
																				- ConfigurationLib.OFFSET).setUnlocalizedName("ElevatorItem");

		GameRegistry.registerBlock(	ConfigurationLib.ElevatorButton,
									"Elevator Button");
		GameRegistry.registerBlock(	ConfigurationLib.ElevatorCaller,
									"Elevator Caller");
		GameRegistry.registerBlock(	ConfigurationLib.Transient,
									"Elevator Transient");
	}

	public static void addNames() {

		// Register names with ModLoader
		LanguageRegistry.addName(	ConfigurationLib.Elevator,
									"Elevator");
		LanguageRegistry.addName(	ConfigurationLib.ElevatorButton,
									"Elevator Button");
		LanguageRegistry.addName(	ConfigurationLib.ElevatorCaller,
									"Elevator Caller");
		LanguageRegistry.addName(	ConfigurationLib.Transient,
									"You shouldn't have this!");
		LanguageRegistry.instance().addStringLocalization(	"entity.ironclad_elevator.name",
															"en_US",
															"Elevator");

	}

	public static void addRecipes() {
		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.ElevatorButton, 1),
								new Object[] {
										"I",
										"I",
										Character.valueOf('I'),
										Item.ingotIron });
		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.Elevator, 4),
								new Object[] {
										"IDI",
										"IRI",
										"III",
										Character.valueOf('I'),
										Item.ingotIron,
										Character.valueOf('D'),
										Item.diamond,
										Character.valueOf('R'),
										Item.redstone });
		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.ElevatorCaller, 1),
								new Object[] {
										"SSS",
										"SRS",
										"SSS",
										Character.valueOf('S'),
										Block.stone,
										Character.valueOf('R'),
										Item.redstone });
	}

	public static boolean canShortCircuit(String username) {
		return ConfigurationLib.shortCircuit;
	}

	// Determine if a given entity is on a the block at a given chunk position
	public static boolean isEntityOnBlock(World world, ChunkPosition pos, Entity entity) {
		AxisAlignedBB box = ConfigurationLib.Elevator.getCollisionBoundingBoxFromPool(	world,
																						pos.x,
																						pos.y,
																						pos.z);
		box.maxY = box.maxY + 0.5;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(	null,
																		box);
		return list.contains(entity);
	}

	// Convert chunk position to a printable string
	public static String pos2Str(ChunkPosition pos) {
		return (new StringBuilder()).append(pos.x).append(", ").append(pos.y).append(", ").append(pos.z).toString();
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
		String[] list = str.split(	",",
									0);
		try {
			for (int i = 0; i < list.length; i++) {
				if (!list[i].trim().isEmpty()) {
					s.add(Integer.parseInt(list[i].trim()));
				}
			}
		} catch (Exception e) {
			DECore.say("There was a problem reading the properties file, using default list instead.");
			s.clear();
			popIntSetFromString(s,
								def,
								"");
		}
	}

	public static void say(String s) {
		say(s,
			false);
	}

	public static void say(String s, boolean ignoreVerbose) {
		if (ConfigurationLib.verbose || ignoreVerbose) {
			System.out.println("[ElevatorMod] " + s);
		}
	}
}
