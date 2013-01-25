package slimevoid.elevators.tileentities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import slimevoid.elevators.core.DECore;
import slimevoid.elevators.core.DEProperties;

public class TileEntityElevator extends TileEntity {

	// Maps Y coordinates to names
	// If the y coordinate is not present in this list, that floor has yet to be
	// named

	private List<Integer> floors = new ArrayList<Integer>();

	public DEProperties props = new DEProperties();

	public static final int NO_ACTION = 0;
	public static final int REQUEST_NEW_FLOOR = 1;
	public static final int DEMAND_NEW_FLOOR = 2;
	public static final int POWER_ON = 3;

	public static final int NO_FLOOR = 0;

	private boolean providesPower = false;

	private int destination_Y;

	private int state;

	public TileEntityElevator() {
		super();
		state = NO_ACTION;
		destination_Y = NO_FLOOR;
	}

	public void setFloors(Collection<Integer> newSetFloors) {
		floors.clear();
		floors.addAll(newSetFloors);
		Collections.sort(floors);
		this.onInventoryChanged();
	}

	public int getDestination() {
		return destination_Y;
	}

	public void setPower(boolean providing) {
		if (!props.getCanProvidePower()) {
			providesPower = false;
			return;
		}
		providesPower = providing;
		this.onInventoryChanged();
	}

	public boolean getProvidesPower() {
		return providesPower;
	}

	public int getCurrentState() {
		return state;
	}

	public void clearState() {
		state = NO_ACTION;
		this.onInventoryChanged();
	}

	public void setFirstRefresh() {
		state = POWER_ON;
		this.onInventoryChanged();
	}

	public boolean reset() {
		state = NO_ACTION;
		if (hasNoFloors() || !betweenFloors()) {
			this.onInventoryChanged();
			return false;
		}

		requestFloor(getClosestFloor(yCoord));
		state = REQUEST_NEW_FLOOR;
		this.onInventoryChanged();
		return true;
	}

	public boolean demandY(int dest_Y) {
		if (dest_Y > 0 && dest_Y < DECore.max_elevator_Y) {
			destination_Y = dest_Y;
			state = DEMAND_NEW_FLOOR;
			this.onInventoryChanged();
			return true;
		}
		return false;
	}

	public boolean requestFloor(int floor) {
		int dest_Y = getYFromFloor(floor);
		if (hasFloorAt(dest_Y)) {
			destination_Y = dest_Y;
			state = REQUEST_NEW_FLOOR;
			this.onInventoryChanged();
			return true;
		}
		return false;
	}

	public boolean hasFloorAt(int y) {
		if (hasNoFloors()) {
			return false;
		}
		if (y <= 0 || y >= DECore.max_elevator_Y) {
			return false;
		}
		return floors.contains(y);
	}

	public boolean betweenFloors() {
		return (hasNoFloors() || !floors.contains(yCoord));
	}

	public boolean hasNoFloors() {
		return (floors == null || floors.isEmpty());
	}

	public int curFloor() {
		if (betweenFloors()) {
			return NO_FLOOR;
		}
		return getClosestFloorFromYCoor_AlwaysDown(yCoord);
	}

	public int floorsBelow() {
		if (hasNoFloors()) {
			return NO_FLOOR;
		}
		if (betweenFloors()) {
			return getClosestFloorFromYCoor_AlwaysDown(yCoord);
		}
		return (curFloor() - 1);
	}

	public int floorsAbove() {
		if (hasNoFloors()) {
			return NO_FLOOR;
		}
		if (betweenFloors()) {
			return floors.size() - floorsBelow();
		}
		return (floors.size() - curFloor());
	}

	public int numFloors() {
		if (hasNoFloors()) {
			return 0;
		}
		return floors.size();
	}

	public int getFloorFromY(int y) {
		if (hasNoFloors()) {
			return NO_FLOOR;
		}
		if (floors.contains(y)) {
			for (int i = 0; i < floors.size(); i++) {
				if (floors.get(i) == y) {
					return i;
				}
			}
		}
		return NO_FLOOR;
	}

	public int getYFromFloor(int floor) {
		if (hasNoFloors()) {
			return NO_FLOOR;
		}
		if (floor > 0 && floor <= floors.size()) {
			return floors.get(floor - 1);
		}
		return NO_FLOOR;
	}

	public int getClosestFloor(int startY) {
		if (hasNoFloors()) {
			return NO_FLOOR;
		}
		int dist = DECore.max_elevator_Y + 1;
		int chosenFloor = NO_FLOOR;
		for (int i = 0; i < floors.size(); i++) {
			int curY = floors.get(i);
			int distance = curY - startY;
			if (distance < 0) {
				distance *= -1;
			}
			if (distance == 0) {
				return (i + 1);
			}
			if (distance < dist) {
				chosenFloor = i + 1;
				dist = distance;
			}
		}
		return chosenFloor;
	}

	public int getClosestYFromYCoor(int y) {
		int floor = getClosestFloor(y);
		return getYFromFloor(floor);
	}

	public int getClosestFloorFromYCoor_AlwaysDown(int y) {
		if (hasNoFloors()) {
			return NO_FLOOR;
		}
		int curFloor = 0;
		Iterator<Integer> iter = floors.iterator();
		while (iter.hasNext()) {
			int curTestY = iter.next();
			if (curTestY <= y) {
				curFloor++;
			}
		}
		return curFloor;
	}

	public boolean checkoutData() throws IOException {
		ChunkPosition curPos = new ChunkPosition(xCoord, yCoord, zCoord);
		Packet250CustomPayload packet = DECore.checkedProperties.get(curPos);
		if (props.readInData(packet)) {
			DECore.checkedProperties.remove(curPos);
			return true;
		}

		return false;
	}

	public Packet250CustomPayload createPropertiesPacket(boolean GUI_Request) throws IOException {
		return props.createPropertiesPacket(
				this.curFloor(),
				this.numFloors(),
				floors,
				GUI_Request);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("provides", providesPower);
		props.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		providesPower = nbt.getBoolean("provides");
		props.readFromNBT(nbt);
	}

}
