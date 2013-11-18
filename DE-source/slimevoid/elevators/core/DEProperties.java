package slimevoid.elevators.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.MathHelper;
import slimevoid.elevators.network.ElevatorPacketHandler;
import slimevoid.elevators.tileentities.TileEntityElevator;

public class DEProperties {
	private Map<Integer, String>	floorNames			= new HashMap<Integer, String>();

	private String					elevatorName		= "";
	private int						firstFloorYValue	= 0;
	private int						colorData			= 0x00;

	private boolean					canProvidePower		= true;
	private boolean					canBeHalted			= true;
	private boolean					enableMobilePower	= false;

	public int						command				= 0;

	public DEProperties() {
		elevatorName = "";

		firstFloorYValue = 0;
		colorData = 0x00;
	}

	public String getElevatorName() {
		return elevatorName;
	}

	public void renameElevator(String name) {
		if (name == null) {
			return;
		}
		elevatorName = name;
	}

	public void setFirstFloorYFromFloor(int floor) {
		DECore.say("Received request to set floor one as " + floor);
		firstFloorYValue = getYCoordFromFloor(floor);
		DECore.say("Set floor one as " + firstFloorYValue);
	}

	public void setColorData(int color) {
		// if (color < 0 || color > mod_Elevator.whatever)
		colorData = color;
	}

	public int getColorData() {
		return colorData;
	}

	public boolean getCanProvidePower() {
		return canProvidePower;
	}

	public boolean getCanHalt() {
		return canBeHalted;
	}

	public boolean getMobilePower() {
		return enableMobilePower;
	}

	public int getFloorOneYValue() {
		return this.firstFloorYValue;
	}

	public void setBooleans(boolean providePower, boolean canHalt, boolean enableMobile) {
		this.canProvidePower = providePower;
		this.canBeHalted = canHalt;
		this.enableMobilePower = enableMobile;
	}

	public void mergeProperties(DEProperties props) throws IOException {
		if (props == null) {
			return;
		}
		Packet250CustomPayload packet = props.createPropertiesPacket(false);
		this.readInData(packet);
	}

	public void mergeProperties(TileEntityElevator otherTile) throws IOException {
		if (otherTile == null) {
			return;
		}
		Packet250CustomPayload packet = otherTile.createPropertiesPacket(false);
		this.readInData(packet);
	}

	public boolean readInData(Packet250CustomPayload packet) throws IOException {
		if (packet == null) {
			return false;
		}
		DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(packet.data));

		DECore.say("Loading properties from packet...");

		command = dataStream.readInt();
		int numFloors = dataStream.readInt();

		canProvidePower = dataStream.readBoolean();
		canBeHalted = dataStream.readBoolean();
		enableMobilePower = dataStream.readBoolean();

		DECore.say("power: " + canProvidePower + ", halt: " + canBeHalted
					+ ", mobile:" + enableMobilePower);

		elevatorName = dataStream.readUTF();
		firstFloorYValue = dataStream.readInt();
		colorData = dataStream.readInt();

		DECore.say("name: " + elevatorName + ", first floor Y: "
					+ firstFloorYValue + ", color: " + colorData);

		for (int i = 0; i < numFloors; i++) {
			int curYValue = dataStream.readInt();
			String curName = dataStream.readUTF();
			floorNames.put(	curYValue,
							curName);
			DECore.say("yCoord: " + curYValue + " : " + curName);
		}
		return true;
	}

	public Packet250CustomPayload createPropertiesPacket(boolean GUI_Request) throws IOException {
		int numFloors = this.floorNames.keySet().size();
		return createPropertiesPacket(	0,
										numFloors,
										GUI_Request);
	}

	public Packet250CustomPayload createPropertiesPacket(int curFloor, int numFloors, boolean GUI_Request) throws IOException {
		return createPropertiesPacket(	curFloor,
										numFloors,
										this.getSortedYCoordList(),
										GUI_Request);
	}

	public Packet250CustomPayload createPropertiesPacket(int curFloor, int numFloors, List<Integer> floors, boolean GUI_Request) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		data.writeInt(curFloor);
		data.writeInt(numFloors);

		data.writeBoolean(canProvidePower);
		data.writeBoolean(canBeHalted);
		data.writeBoolean(enableMobilePower);

		data.writeUTF(elevatorName);
		data.writeInt(firstFloorYValue);
		data.writeInt(colorData);

		Iterator<Integer> iter = floors.iterator();
		while (iter.hasNext()) {
			int curYValue = iter.next();
			String curName = floorNames.containsKey(curYValue) ? floorNames.get(curYValue) : "";
			data.writeInt(curYValue);
			data.writeUTF(curName);
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		if (GUI_Request) {
			packet.channel = ElevatorPacketHandler.CHANNELS[ElevatorPacketHandler.GUI_REQUEST];
		} else {
			packet.channel = ElevatorPacketHandler.CHANNELS[ElevatorPacketHandler.GUI_DATA];
		}
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;
		return packet;
	}

	public List<Integer> getSortedYCoordList() {
		List<Integer> knownFloors = new ArrayList<Integer>();
		knownFloors.addAll(this.floorNames.keySet());
		Collections.sort(knownFloors);
		return knownFloors;
	}

	public int getYCoordFromFloor(int floor) {
		List<Integer> knownFloors = getSortedYCoordList();
		if (floor > 0 && floor <= knownFloors.size()) {
			return knownFloors.get(floor - 1);
		}
		if (floor < 1) {
			return 0;
		}
		return DECore.max_elevator_Y;
	}

	// should only be used by the GUI for getting floor "one"
	public int getFloorOne() {
		List<Integer> knownFloors = getSortedYCoordList();
		for (int i = 0; i < knownFloors.size(); i++) {
			if (knownFloors.get(i) >= this.firstFloorYValue) {
				return (i + 1);
			}
		}
		return knownFloors.size() + 1;
	}

	public boolean nameFloor(int floor, String name) {
		int yCoord = getYCoordFromFloor(floor);
		return this.nameYCoord(	yCoord,
								name);
	}

	public boolean nameYCoord(int yCoord, String name) {
		if (name == null) {
			return false;
		}
		if (yCoord > 0 && yCoord < DECore.max_elevator_Y) {
			floorNames.put(	yCoord,
							name);
			return true;
		}
		return false;
	}

	// Get floor name from y value
	// (Returns null if the floor is unnamed)
	public String getFloorName(int yCoord) {
		return floorNames.get(yCoord);
	}

	public boolean isFloorNamed(int floor) {
		String name = floorNames.get(getYCoordFromFloor(floor));
		return (name != null && !name.isEmpty());
	}

	public boolean isYCoordNamed(int yCoord) {
		String name = floorNames.get(yCoord);
		return (name != null && !name.isEmpty());
	}

	public String getExtendedFloorName(int curFloor, int floorOne) {
		if (isFloorNamed(curFloor)) {
			return floorNames.get(getYCoordFromFloor(curFloor));
		}
		String name = "";
		int actualFloorNum = (floorOne < 1) ? curFloor : curFloor - floorOne
															+ 1;
		if (curFloor >= floorOne) {
			name = DECore.floorName + " " + actualFloorNum;
		} else {
			name = DECore.basementName + " "
					+ String.valueOf((int) MathHelper.abs(actualFloorNum - 1));
		}
		return name;
	}

	public String getAbbreviatedFloorName(int curF, int floorOne) {
		String name = "";
		int actualFloorNum = (floorOne < 1) ? curF : curF - floorOne + 1;
		if (actualFloorNum > 0) {
			name = String.valueOf(actualFloorNum);
		} else {
			name = "B"
					+ String.valueOf((int) MathHelper.abs(actualFloorNum - 1));
		}
		return name;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		Iterator<Integer> iter = floorNames.keySet().iterator();
		while (iter.hasNext()) {
			int curY = iter.next();
			String curName = floorNames.get(curY);
			if (curName != null && !curName.isEmpty()) {
				nbt.setString(	(new StringBuilder()).append("x").append(String.valueOf(curY)).toString(),
								curName);
			}
		}
		nbt.setBoolean(	"canProvide",
						canProvidePower);
		nbt.setBoolean(	"canHalt",
						canBeHalted);
		nbt.setBoolean(	"mobilePower",
						enableMobilePower);

		nbt.setInteger(	"floorOne",
						this.firstFloorYValue);
		nbt.setInteger(	"colorData",
						this.colorData);
		nbt.setString(	"elevatorName",
						this.elevatorName);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		for (int curY = 0; curY < (DECore.max_elevator_Y + 2); curY++) {
			String curFloorName = "";
			try {
				curFloorName = nbt.getString((new StringBuilder()).append("x").append(String.valueOf(curY)).toString());
			} finally {
			}
			if (curFloorName != null && !curFloorName.isEmpty()) {
				floorNames.put(	curY,
								curFloorName);
			}
		}
		canProvidePower = nbt.getBoolean("canProvide");
		canBeHalted = nbt.getBoolean("canHalt");
		enableMobilePower = nbt.getBoolean("mobilePower");

		firstFloorYValue = nbt.getInteger("floorOne");
		colorData = nbt.getInteger("colorData");
		elevatorName = nbt.getString("elevatorName");
	}
}
