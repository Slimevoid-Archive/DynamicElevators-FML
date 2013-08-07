package slimevoid.elevators.client.gui;

// DYNAMIC ELEVATORS - 1.6

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.ChunkPosition;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import slimevoid.elevators.core.DECore;
import slimevoid.elevators.core.DEProperties;
import slimevoid.elevators.core.lib.ResourceLib;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElevator extends GuiScreen {
	// Needs command execution?
	public static final int GUI_OPTIONS = DECore.GUI_OPTIONS; // N
	public static final int GUI_RESET = DECore.GUI_RESET; // Y
	public static final int GUI_CANCEL = DECore.GUI_CANCEL; // Y
	public static final int GUI_OPTIONS_CANCEL = DECore.GUI_OPTIONS_CANCEL; // N
	public static final int GUI_OPTIONS_SLIDER = DECore.GUI_OPTIONS_SLIDER; // N
	public static final int GUI_OPTIONS_NAMESLIST = DECore.GUI_OPTIONS_NAMESLIST; // N
	public static final int GUI_OPTIONS_FLOORNAME = DECore.GUI_OPTIONS_FLOORNAME; // N
	public static final int GUI_OPTIONS_ELEVATORNAME = DECore.GUI_OPTIONS_ELEVATORNAME; // N
	public static final int GUI_OPTIONS_APPLY = DECore.GUI_OPTIONS_APPLY; // Y
	public static final int GUI_OPTIONS_POWER = DECore.GUI_OPTIONS_POWER;
	public static final int GUI_OPTIONS_HALT = DECore.GUI_OPTIONS_HALT;
	public static final int GUI_OPTIONS_MOBILE = DECore.GUI_OPTIONS_MOBILE;
	public static final int GUI_RENAME_OK = DECore.GUI_RENAME_OK;
	public static final int GUI_RENAME_CANCEL = DECore.GUI_RENAME_CANCEL;

	public static final int NAMING_ELEVATOR = 1;
	public static final int NAMING_FLOOR = 2;

	protected int xSize = 215;
	protected int ySize = 213;

	ChunkPosition elevatorPos;

	private GuiTextField txtEntryBox;
	private GuiButton nameOk;
	private GuiButton nameCancel;

	private int nameMode = 0;
	private int curSelectedFloor = 0;

	private GuiElevatorSlider floorZeroSlider;

	private GuiElevatorList floorNamesList;
	private GuiButton RenameFloor;
	private GuiButton RenameElevator;

	private GuiElevatorRadialButton canProvidePower;
	private GuiElevatorRadialButton canBeHalted;
	private GuiElevatorRadialButton mobilePower;

	private List<GuiButton> floorButtons = new ArrayList<GuiButton>();

	private boolean sentPacket = false;

	private boolean optionsOpen = false;

	boolean isRemote = false;
	int numFloors = 0;
	int curFloor = 0;

	protected int guiLeft;
	protected int guiTop;

	protected String screenTitle;
	private String screenSubtitle;

	private int buttonId;

	public DEProperties props = new DEProperties();
	public List<Integer> yCoordList;

	int titleTop = 0;
	int subtitleTop = 0;

	int floorOne = 1;

	// Constructor for local (SSP) GUI
	public GuiElevator(Packet250CustomPayload packet, ChunkPosition pos) throws IOException {
		this(packet);
		elevatorPos = pos;
		isRemote = false;
	}

	// Constructor for remote (SMP) GUI
	public GuiElevator(Packet250CustomPayload packet) throws IOException {
		isRemote = true;
		buttonId = -1;
		screenTitle = "";

		DECore.say("GUI LOADING...");

		DataInputStream dataStream = new DataInputStream(
				new ByteArrayInputStream(packet.data));
		curFloor = dataStream.readInt();
		numFloors = dataStream.readInt();

		DECore.say("floors: " + numFloors + ", current: " + curFloor);

		props.readInData(packet);
		floorOne = props.getFloorOne();
		yCoordList = props.getSortedYCoordList();
		screenTitle = props.getElevatorName();
	}

	@Override
	public void initGui() {
		//StringTranslate stringtranslate = StringTranslate.getInstance();
		super.initGui();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		int size = numFloors;
		if (size > 70) {
			size = 70;
		}

		int spacing = 5;
		if (size > 48) {
			spacing = 1;
		}
		if (size > 63) {
			spacing = 0;
		}

		int numRows = 5;
		int numCols = 2;

		int startY = guiTop + 155;

		if (spacing == 5) {
			numRows = (size % 6 == 1) ? 5 : 6;
			numCols = (size > (numRows * 2)) ? ((size - 1) / numRows) + 1 : 2;
			if (numCols > 2) {
				while (size % numCols == 1) {
					numCols++;
				}
			}
			if (numCols > 8) {
				numRows = 6;
				numCols = (size > (numRows * 2)) ? ((size - 1) / numRows) + 1 : 2;
			}
			startY = (numRows == 6 || size < ((numRows - 1) * numCols + 1)) ? guiTop + 155 : guiTop + 130;
		} else {
			numRows = (size % 7 == 1) ? 6 : 7;
			numCols = ((size - 1) / numRows) + 1;
			if (numCols > 10) {
				numRows = 7;
				numCols = ((size - 1) / numRows) + 1;
			}
		}

		DECore
				.say("Size: " + size + "; rows: " + numRows + "; columns: " + numCols);

		int startX = width / 2;

		if (DECore.invertKeys) {
			startX += (numCols / 2 - 1) * (20) + (numCols / 2) * spacing;
			if (numCols % 2 == 1) {
				startX += 10;
			} else {
				startX += spacing / 2;
			}
		} else {
			startX -= (numCols / 2) * (spacing + 20);
			if (numCols % 2 == 1) {
				startX -= 10;
			} else {
				startX -= spacing / 2;
			}
		}

		spacing += 20;

		for (int j = size; j > 0; j--) {
			GuiButton curButton;
			if (!DECore.invertKeys) {
				curButton = new GuiButton(
						j,
							startX + ((j - 1) % numCols) * spacing,
							startY - spacing * ((j - 1) / numCols),
							20,
							20,
							(new StringBuilder()).append(j).toString());
			} else {
				curButton = new GuiButton(
						j,
							startX - ((j - 1) % numCols) * spacing,
							startY - spacing * ((j - 1) / numCols),
							20,
							20,
							(new StringBuilder()).append(j).toString());
			}
			if (curFloor == j) {
				curButton.enabled = false;
			}
			floorButtons.add(curButton);
		}
		buttonList.addAll(floorButtons);

		titleTop = guiTop + 5;
		subtitleTop = guiTop + 15;

		buttonList.add(new GuiElevatorOptionsButton(
				GUI_OPTIONS,
					guiLeft + 4,
					guiTop + 4));
		buttonList.add(new GuiButton(
				GUI_RESET,
					width / 2 - 95,
					guiTop + 180,
					90,
					20,
					"Reset Elevator"));
		buttonList.add(new GuiButton(
				GUI_CANCEL,
					width / 2 + 5,
					guiTop + 180,
					90,
					20,
					"Close"));

		buttonList.add(new GuiButton(
				GUI_OPTIONS_APPLY,
					width / 2 - 95,
					guiTop + 180,
					90,
					20,
					"Apply"));
		buttonList.add(new GuiButton(
				GUI_OPTIONS_CANCEL,
					width / 2 + 5,
					guiTop + 180,
					90,
					20,
					"Cancel"/**stringtranslate.translateKey("gui.cancel")**/));

		floorZeroSlider = new GuiElevatorSlider(
				GUI_OPTIONS_SLIDER,
					width / 2 - 75,
					guiTop + 110,
					floorOne,
					numFloors,
					true,
					"First Floor: ");
		buttonList.add(floorZeroSlider);

		// TODO :: Add interface for selecting textures for elevator floor, ceiling, and sides

		Set<Integer> floorNamesForList = new HashSet<Integer>();
		for (int i = 1; i <= numFloors; i++) {
			floorNamesForList.add(i);
		}

		floorNamesList = new GuiElevatorList(
				this,
					GUI_OPTIONS_NAMESLIST,
					width / 2 - 103,
					guiTop + 50,
					130,
					20,
					15,
					50,
					floorNamesForList,
					this.mc,
					this.fontRenderer);
		RenameFloor = new GuiButton(
				GUI_OPTIONS_FLOORNAME,
					width / 2 + 40,
					guiTop + 50,
					60,
					20,
					"Rename...");
		RenameElevator = new GuiButton(
				GUI_OPTIONS_ELEVATORNAME,
					width / 2 - 60,
					guiTop + 25,
					120,
					20,
					"Rename Elevator...");
		buttonList.add(RenameFloor);
		buttonList.add(RenameElevator);

		// RenameFloor.enabled = false;
		// RenameElevator.enabled = false;

		txtEntryBox = new GuiTextField(
				this.fontRenderer,
					this.width / 2 - 100,
					guiTop + 60,
					200,
					20);
		nameOk = new GuiButton(
				GUI_RENAME_OK,
					width / 2 - 50,
					guiTop + 90,
					40,
					20,
					"Apply"/**stringtranslate.translateKey("Apply")**/);
		nameCancel = new GuiButton(
				GUI_RENAME_CANCEL,
					width / 2 + 10,
					guiTop + 90,
					40,
					20,
					"Cancel"/**stringtranslate.translateKey("gui.cancel")**/);
		buttonList.add(nameOk);
		buttonList.add(nameCancel);

		canProvidePower = new GuiElevatorRadialButton(
				GUI_OPTIONS_POWER,
					width / 2 - 100,
					guiTop + 130,
					"Stationary elevators provide power");
		canBeHalted = new GuiElevatorRadialButton(
				GUI_OPTIONS_HALT,
					width / 2 - 100,
					guiTop + 160,
					"Moving elevators can be halted");
		mobilePower = new GuiElevatorRadialButton(
				GUI_OPTIONS_MOBILE,
					width / 2 - 100,
					guiTop + 145,
					"Moving elevators provide power");
		try {
			canProvidePower.enabled = props.getCanProvidePower();
			canBeHalted.enabled = props.getCanHalt();
			mobilePower.enabled = props.getMobilePower();
		} catch (Exception e) {
			DECore.say("Error occurred when getting properties");
		}

		buttonList.add(canProvidePower);
		buttonList.add(canBeHalted);
		buttonList.add(mobilePower);

		toggleVisibility();
		renameButtons();
	}

	public void renameButtons() {
		for (int iter = 0; iter < floorButtons.size(); iter++) {
			GuiButton curButton = floorButtons.get(iter);
			curButton.displayString = props.getAbbreviatedFloorName(
					curButton.id,
					floorOne);
		}
		screenSubtitle = props.getExtendedFloorName(curFloor, floorOne);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		drawDefaultBackground();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft, guiTop, 0.0F);
		mc.renderEngine.func_110577_a(ResourceLib.GUI_ELEVATOR); //mc.renderEngine.getTexture("/gui/elevatorgui.png")
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);
		GL11.glTranslatef(0.0F, 0.0F, 0.0F);
		GL11.glPopMatrix();

		super.drawScreen(i, j, f);

		if (!optionsOpen) {
			if (!screenTitle.equals("")) {
				drawUnshadedCenteredString(
						fontRenderer,
						screenTitle,
						width / 2,
						titleTop,
						0x000000);
				drawUnshadedCenteredString(
						fontRenderer,
						"" + screenSubtitle + "",
						width / 2,
						subtitleTop,
						0x000000);
			} else {
				drawUnshadedCenteredString(
						fontRenderer,
						"" + screenSubtitle + "",
						width / 2,
						titleTop,
						0x000000);
			}
		} else {
			if (screenTitle == null || screenTitle.equals("")) {
				drawUnshadedCenteredString(
						fontRenderer,
						"[Unnamed Elevator]",
						width / 2,
						subtitleTop,
						0x000000);
			} else {
				drawUnshadedCenteredString(
						fontRenderer,
						screenTitle,
						width / 2,
						subtitleTop,
						0x000000);
			}
			drawUnshadedCenteredString(
					fontRenderer,
					"Options",
					width / 2,
					titleTop,
					0x000000);

			if (nameMode == 0) {
				floorNamesList.drawScreen(i, j, f);
			}
			if (nameMode > 0) {
				String request = "Enter new name for ";
				if (nameMode == NAMING_ELEVATOR) {
					request += "this elevator:";
				} else {
					request += "floor " + curSelectedFloor + ":";
				}
				fontRenderer.drawString(request, width / 2 - 100, 50, 0x00000);
				txtEntryBox.drawTextBox();
			}
		}
	}

	private void toggleVisibility() {
		for (int i = 0; i < buttonList.size(); i++) {
			GuiButton button = (GuiButton) buttonList.get(i);
			if (button.id < GUI_OPTIONS_CANCEL) {
				button.drawButton = !optionsOpen;
			} else {
				button.drawButton = optionsOpen && nameMode == 0;
			}
			if (button.id == GUI_OPTIONS) {
				button.enabled = !optionsOpen;
			}
		}
		if (nameMode > 0) {
			nameOk.drawButton = optionsOpen;
			nameCancel.drawButton = optionsOpen;
		} else {
			nameOk.drawButton = false;
			nameCancel.drawButton = false;
		}

		canBeHalted.drawButton &= !isRemote;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		int command = guibutton.id;
		switch (command) {
		case GUI_OPTIONS:
			optionsOpen = true;
			toggleVisibility();
			break;
		case GUI_OPTIONS_APPLY:
			props.renameElevator(screenTitle);
			props.setFirstFloorYFromFloor(floorOne);

			props.setBooleans(
					canProvidePower.enabled,
					canBeHalted.enabled,
					mobilePower.enabled);

			if (!isRemote) {
				try {
					DECore.checkedProperties.put(elevatorPos, props
							.createPropertiesPacket(command, numFloors, false));
				} catch (IOException e) {
					DECore.say(
							"Error while creating elevator properties packet.",
							true);
					e.printStackTrace();
				}
			}
			exit(GUI_OPTIONS_APPLY, false);
		case GUI_OPTIONS_CANCEL:
			optionsOpen = false;
			toggleVisibility();
			renameButtons();
			break;
		case GUI_OPTIONS_ELEVATORNAME:
			nameMode = NAMING_ELEVATOR;
			txtEntryBox.setText(screenTitle);
			txtEntryBox.setFocused(true);
			toggleVisibility();
			break;
		case GUI_OPTIONS_FLOORNAME:
			if (floorNamesList.selectedElement > -1) {
				curSelectedFloor = floorNamesList.selectedElement + 1;
				nameMode = NAMING_FLOOR;
				if (props.isFloorNamed(curSelectedFloor)) {
					txtEntryBox.setText(props.getExtendedFloorName(
							curSelectedFloor,
							floorOne));
				} else {
					txtEntryBox.setText("");
				}
				toggleVisibility();
				txtEntryBox.setFocused(true);
			}
			break;
		case GUI_RENAME_OK:
			if (nameMode == NAMING_ELEVATOR) {
				screenTitle = txtEntryBox.getText();
			} else if (nameMode == NAMING_FLOOR) {
				props.nameFloor(curSelectedFloor, txtEntryBox.getText());
			}
		case GUI_RENAME_CANCEL:
			nameMode = 0;
			toggleVisibility();
			txtEntryBox.setText("");
			txtEntryBox.setFocused(false);
			break;
		case GUI_OPTIONS_SLIDER:
			floorOne = (int) floorZeroSlider.sliderValue;
			break;
		case GUI_CANCEL:
			exit(GUI_CANCEL);
			break;
		case GUI_RESET:
			if (!isRemote) {
				DECore.elevator_reset(
						FMLClientHandler.instance().getClient().theWorld,
						elevatorPos);
			}
			exit(GUI_RESET);
			break;
		default:
			int selectedFloor = guibutton.id;
			if (selectedFloor < 1 || selectedFloor > DECore.max_elevator_Y) {
				return;
			}
			if (selectedFloor > numFloors || curFloor == selectedFloor) {
				return;
			}

			if (!isRemote) {
				DECore.elevator_requestFloor(
						FMLClientHandler.instance().getClient().theWorld,
						elevatorPos,
						selectedFloor);
			}
			exit(selectedFloor);
			break;
		}
	}

	private void exit(int command) {
		exit(command, true);
	}

	private void exit(int command, boolean close) {
		if (isRemote) {
			try {
				sentPacket = DECore.packetHandler.sendGUIPacketToServer(props
						.createPropertiesPacket(command, numFloors, false));
			} catch (IOException e) {
				DECore.say("Error while creating packet:", true);
				e.printStackTrace();
				sentPacket = false;
			}
		} else if (elevatorPos != null) {
			DECore.refreshElevator(
					FMLClientHandler.instance().getClient().theWorld,
					elevatorPos);
			sentPacket = close;
		}
		if (close) {
			DECore.say("Exiting gui!");
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	public void drawUnshadedCenteredString(FontRenderer fontrenderer, String s, int i, int j, int k) {
		fontrenderer
				.drawString(s, i - fontrenderer.getStringWidth(s) / 2, j, k);
	}

	@Override
	public void onGuiClosed() {
		if (!sentPacket) {
			DECore.say("Exiting gui!");
			exit(GUI_CANCEL, false);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (!mc.thePlayer.isEntityAlive() || mc.thePlayer.isDead) {
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int par3) {
		if (par3 == 0 && !floorNamesList.mousePressed(x, y) && floorNamesList.extended) {
			floorNamesList.minimize();
		} else if (floorNamesList.mousePressed(x, y) && floorNamesList.extended) {
			return;
		}
		super.mouseClicked(x, y, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		this.txtEntryBox.textboxKeyTyped(par1, par2);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.getEventKeyState()) {
			if (Keyboard.getEventKey() == GUI_CANCEL && floorNamesList.selectedElement > 0) { // up
				floorNamesList.selectedElement--;
				floorNamesList.setAmountScrolled();
			} else if (Keyboard.getEventKey() == 208 && floorNamesList.selectedElement < (floorNamesList
					.getSize() - 1)) { // down
				floorNamesList.selectedElement++;
				floorNamesList.setAmountScrolled();
			}
		}
		super.handleKeyboardInput();
	}
}
