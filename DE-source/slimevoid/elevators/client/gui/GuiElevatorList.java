package slimevoid.elevators.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import slimevoid.elevators.core.lib.ResourceLib;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElevatorList {
	private final Minecraft mc;

	/**
	 * The width of the GuiScreen. Affects the container rendering, but not the
	 * overlays.
	 */
	private final int width;

	protected final int fullHeight;
	protected final int minHeight;

	/**
	 * The height of the GuiScreen. Affects the container rendering, but not the
	 * overlays or the scrolling.
	 */
	private int height;

	/** The top of the slot container. Affects the overlays and scrolling. */
	protected final int top;

	/** The bottom of the slot container. Affects the overlays and scrolling. */
	protected int bottom;
	private final int right;
	private final int left;

	/** The height of a slot. */
	protected final int slotHeight;

	/** button id of the button used to scroll up */
	private int scrollUpButtonID;

	/** the buttonID of the button used to scroll down */
	private int scrollDownButtonID;
	protected int mouseX;
	protected int mouseY;

	/** where the mouse was in the window when you first clicked to scroll */
	private float initialClickY = -2.0F;

	/**
	 * what to multiply the amount you moved your mouse by(used for slowing down
	 * scrolling when over the items and no on scroll bar)
	 */
	private float scrollMultiplier;

	/** how far down this slot has been scrolled */
	private float amountScrolled;

	/** the element in the list that was selected */
	public int selectedElement = -1;

	/** the time when this button was last clicked. */
	private long lastClicked = 0L;

	private final GuiElevator parentScreen;

	public final List<Integer> itemList = new ArrayList<Integer>();

	public boolean extended = false;

	public final int guiID;

	private final FontRenderer fontrender;

	public GuiElevatorList(GuiElevator parent, int ID, int LEFT, int TOP, int WIDTH, int HEIGHT, int SLOTHEIGHT, int MAXHEIGHT, Set itemlist, Minecraft mc, FontRenderer fr) {
		parentScreen = parent;
		this.mc = mc;
		this.width = WIDTH;
		this.height = HEIGHT;
		this.fullHeight = MAXHEIGHT;
		this.minHeight = HEIGHT;
		this.top = TOP;
		this.bottom = TOP + HEIGHT;
		this.slotHeight = SLOTHEIGHT;
		this.left = LEFT;
		this.right = LEFT + WIDTH;
		if (itemlist != null) {
			itemList.addAll(itemlist);
		}
		guiID = ID;
		fontrender = fr;
	}

	/**
	 * Gets the size of the current slot list.
	 */
	protected int getSize() {
		return itemList.size();
	}

	/**
	 * the element in the slot that was clicked, boolean for whether it was
	 * double clicked or not
	 */
	protected void elementClicked(int slot, boolean doubleClicked) {
		selectedElement = slot;

		if (doubleClicked) {
			// parentScreen.doubleClickedListItem(guiID, slot);
		}
	}

	/**
	 * returns true if the element passed in is currently selected
	 */
	protected boolean isSelected(int slot) {
		return selectedElement == slot;
	}

	protected void drawBackground() {
	}

	protected void drawSlot(int slotID, int left, int top, int unknown, Tessellator tessellator) {
		int curFloor = slotID + 1;
		String curFloorName = parentScreen.props.getExtendedFloorName(
				curFloor,
				parentScreen.floorOne);
		if (!parentScreen.props.isFloorNamed(curFloor)) {
			curFloorName = "[Unnamed]";
		}
		if (curFloorName.length() > 20) {
			curFloorName = curFloorName.substring(0, 20) + "...";
		}
		parentScreen.drawString(
				fontrender,
				String.valueOf(curFloor) + ": " + curFloorName,
				left + 2,
				top + 1,
				16777215);
	}

	/**
	 * return the height of the content being scrolled
	 */
	protected int getContentHeight() {
		return this.getSize() * this.slotHeight; // + this.field_27261_r;
	}

	/**
	 * Registers the IDs that can be used for the scrollbar's buttons.
	 */
	public void registerScrollButtons(List par1List, int par2, int par3) {
		this.scrollUpButtonID = par2;
		this.scrollDownButtonID = par3;
	}

	/**
	 * stop the thing from scrolling out of bounds
	 */
	private void bindAmountScrolled() {
		int var1 = this.getContentHeight() - (this.bottom - this.top - 4);

		if (var1 < 0) {
			var1 /= 2;
		}

		if (this.amountScrolled < 0.0F) {
			this.amountScrolled = 0.0F;
		}

		if (this.amountScrolled > var1) {
			this.amountScrolled = var1;
		}
	}

	public void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.enabled) {
			if (par1GuiButton.id == this.scrollUpButtonID) {
				this.amountScrolled -= (this.slotHeight);
				this.initialClickY = -2.0F;
				this.bindAmountScrolled();
			} else if (par1GuiButton.id == this.scrollDownButtonID) {
				this.amountScrolled += (this.slotHeight);
				this.initialClickY = -2.0F;
				this.bindAmountScrolled();
			}
		}
	}

	public void minimize() {
		extended = false;
		this.height = this.minHeight;
		this.bottom = this.top + this.height;
	}

	public void maximize() {
		extended = true;
		this.height = this.fullHeight;
		this.bottom = this.top + this.height;
	}

	public boolean mousePressed(int x, int y) {
		return x >= this.left && y >= this.top && x < (this.right + 10) && y < this.bottom;
	}

	public void setAmountScrolled() {
		amountScrolled = slotHeight * selectedElement;
		bindAmountScrolled();
	}

	/**
	 * draws the slot to the screen, pass in mouse's current x and y and partial
	 * ticks
	 */
	public void drawScreen(int x, int y, float f) {
		this.mouseX = x;
		this.mouseY = y;
		this.drawBackground();

		int numItems = this.getSize();
		int scrollLeft = this.right; // this.width / 2 + 124;
		int scrollRight = scrollLeft + 10;

		int slotLeft = this.left;// + this.width / 2 - 110;
		int slotRight = this.right;// this.width / 2 + 110;

		int var9;
		int relativeY;
		int hoverSlot;
		int var13;
		int var19;

		// Click
		if (Mouse.isButtonDown(0)) {
			if (this.initialClickY == -1.0F) {
				boolean elementWasSelected = true;

				if (mousePressed(x, y)) {
					relativeY = y - this.top + (int) this.amountScrolled - 4;
					hoverSlot = relativeY / this.slotHeight;
					if (!extended) {
						maximize();
					}

					// If mouse is on an element
					if (x >= slotLeft && x <= slotRight && hoverSlot >= 0 && relativeY >= 0 && hoverSlot < numItems) {
						boolean doubleClicked = System.currentTimeMillis() - this.lastClicked < 500L;
						if (doubleClicked) {
							minimize();
							// this.elementClicked(hoverSlot, doubleClicked);
							this.selectedElement = hoverSlot;
							this.amountScrolled = slotHeight * selectedElement;
						}
						this.lastClicked = System.currentTimeMillis();
					}
					// if mouse is above all elements (not actually on the list)
					else if (x >= slotLeft && x <= slotRight && relativeY < 0) {
						elementWasSelected = false;
					}

					// if mouse is over the scroll bar
					if (x >= scrollLeft && x <= scrollRight) {
						this.scrollMultiplier = -1.0F;
						var19 = this.getContentHeight() - (this.bottom - this.top - 4);

						if (var19 < 1) {
							var19 = 1;
						}

						var13 = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this
								.getContentHeight());

						if (var13 < 32) {
							var13 = 32;
						}

						if (var13 > this.bottom - this.top - 8) {
							var13 = this.bottom - this.top - 8;
						}

						this.scrollMultiplier /= (float) (this.bottom - this.top - var13) / (float) var19;
						if (extended) {
							scrollMultiplier *= 2;
						}
					} else {
						this.scrollMultiplier = 1.0F;
					}

					if (elementWasSelected) {
						this.initialClickY = y;
					} else {
						this.initialClickY = -2.0F;
					}
				} else {
					this.initialClickY = -2.0F;
				}
			} else if (this.initialClickY >= 0.0F) {
				this.amountScrolled -= MathHelper
						.floor_float(((y - this.initialClickY) * this.scrollMultiplier) / this.slotHeight) * this.slotHeight;
				this.initialClickY = y;
			}
		}
		// Scroll
		else {
			while (Mouse.next()) {
				int mouseWheelEvent = Mouse.getEventDWheel();

				if (mouseWheelEvent != 0) {
					if (mouseWheelEvent > 0) {
						mouseWheelEvent = -1;
					} else if (mouseWheelEvent < 0) {
						mouseWheelEvent = 1;
					}

					this.amountScrolled += mouseWheelEvent * this.slotHeight;
				}
			}

			this.initialClickY = -1.0F;
		}
		this.bindAmountScrolled();
		if (!extended) {
			selectedElement = MathHelper
					.floor_float(amountScrolled / slotHeight + 0.5F);
		}

		Tessellator tess = Tessellator.instance;
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		/**GL11.glBindTexture(
				GL11.GL_TEXTURE_2D,
				this.mc.renderEngine.getTexture("/gui/elevatorbg.png"));**/
		mc.renderEngine.bindTexture(ResourceLib.BG_ELEVATOR);

		float var17 = 32.0F;
		tess.startDrawingQuads();
		tess.setColorOpaque_I(2105376);
		tess.addVertexWithUV(
				this.left,
				this.bottom,
				0.0D,
				this.left / var17,
				(this.bottom + (int) this.amountScrolled) / var17);
		tess.addVertexWithUV(
				this.right,
				this.bottom,
				0.0D,
				this.right / var17,
				(this.bottom + (int) this.amountScrolled) / var17);
		tess.addVertexWithUV(
				this.right,
				this.top,
				0.0D,
				this.right / var17,
				(this.top + (int) this.amountScrolled) / var17);
		tess.addVertexWithUV(
				this.left,
				this.top,
				0.0D,
				this.left / var17,
				(this.top + (int) this.amountScrolled) / var17);
		tess.draw();
		var9 = this.width / 2 - 92 - 16;
		relativeY = this.top + 4 - (int) this.amountScrolled;

		int var14;

		for (int curItem = 0; curItem < numItems; ++curItem) {
			int currentY = relativeY + curItem * this.slotHeight;
			int curSlotHeight = this.slotHeight - 4;

			int curSlotLeft = slotLeft;
			int curSlotRight = slotRight;

			if (currentY <= (this.bottom - 8) && currentY + curSlotHeight >= (this.top + 8)) {
				// Draw outline and background for selected item
				if (this.isSelected(curItem)) {
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					tess.startDrawingQuads();
					tess.setColorOpaque_I(8421504);
					tess.addVertexWithUV(
							slotLeft,
							currentY + curSlotHeight + 2,
							0.0D,
							0.0D,
							1.0D);
					tess.addVertexWithUV(
							slotRight,
							currentY + curSlotHeight + 2,
							0.0D,
							1.0D,
							1.0D);
					tess.addVertexWithUV(
							slotRight,
							currentY - 2,
							0.0D,
							1.0D,
							0.0D);
					tess.addVertexWithUV(
							slotLeft,
							currentY - 2,
							0.0D,
							0.0D,
							0.0D);
					tess.setColorOpaque_I(0);
					tess.addVertexWithUV(
							slotLeft + 1,
							currentY + curSlotHeight + 1,
							0.0D,
							0.0D,
							1.0D);
					tess.addVertexWithUV(
							slotRight - 1,
							currentY + curSlotHeight + 1,
							0.0D,
							1.0D,
							1.0D);
					tess.addVertexWithUV(
							slotRight - 1,
							currentY - 1,
							0.0D,
							1.0D,
							0.0D);
					tess.addVertexWithUV(
							slotLeft + 1,
							currentY - 1,
							0.0D,
							0.0D,
							0.0D);
					tess.draw();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}
				// draw slot contents
				this.drawSlot(
						curItem,
						curSlotLeft,
						currentY,
						curSlotHeight,
						tess);
			}
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		byte var20 = 4;
		// this.overlayBackground(0, this.top, 255, 255);
		// this.overlayBackground(this.bottom, this.height, 255, 255);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tess.startDrawingQuads();
		tess.setColorRGBA_I(0, 0);
		tess.addVertexWithUV(this.left, this.top + var20, 0.0D, 0.0D, 1.0D);
		tess.addVertexWithUV(this.right, this.top + var20, 0.0D, 1.0D, 1.0D);
		tess.setColorRGBA_I(0, 255);
		tess.addVertexWithUV(this.right, this.top, 0.0D, 1.0D, 0.0D);
		tess.addVertexWithUV(this.left, this.top, 0.0D, 0.0D, 0.0D);
		tess.draw();
		tess.startDrawingQuads();
		tess.setColorRGBA_I(0, 255);
		tess.addVertexWithUV(this.left, this.bottom, 0.0D, 0.0D, 1.0D);
		tess.addVertexWithUV(this.right, this.bottom, 0.0D, 1.0D, 1.0D);
		tess.setColorRGBA_I(0, 0);
		tess.addVertexWithUV(this.right, this.bottom - var20, 0.0D, 1.0D, 0.0D);
		tess.addVertexWithUV(this.left, this.bottom - var20, 0.0D, 0.0D, 0.0D);
		tess.draw();
		var19 = this.getContentHeight() - (this.bottom - this.top - 4);

		if (var19 > 0) {
			var13 = (this.bottom - this.top) * (this.bottom - this.top) / this
					.getContentHeight();

			if (var13 < 32) {
				var13 = 32;
			}

			if (var13 > this.bottom - this.top - 8) {
				var13 = this.bottom - this.top - 8;
			}

			var14 = (int) this.amountScrolled * (this.bottom - this.top - var13) / var19 + this.top;

			if (var14 < this.top) {
				var14 = this.top;
			}

			tess.startDrawingQuads();
			tess.setColorRGBA_I(0, 255);
			tess.addVertexWithUV(scrollLeft, this.bottom, 0.0D, 0.0D, 1.0D);
			tess.addVertexWithUV(scrollRight, this.bottom, 0.0D, 1.0D, 1.0D);
			tess.addVertexWithUV(scrollRight, this.top, 0.0D, 1.0D, 0.0D);
			tess.addVertexWithUV(scrollLeft, this.top, 0.0D, 0.0D, 0.0D);
			tess.draw();
			tess.startDrawingQuads();
			tess.setColorRGBA_I(8421504, 255);
			tess.addVertexWithUV(scrollLeft, var14 + var13, 0.0D, 0.0D, 1.0D);
			tess.addVertexWithUV(scrollRight, var14 + var13, 0.0D, 1.0D, 1.0D);
			tess.addVertexWithUV(scrollRight, var14, 0.0D, 1.0D, 0.0D);
			tess.addVertexWithUV(scrollLeft, var14, 0.0D, 0.0D, 0.0D);
			tess.draw();
			tess.startDrawingQuads();
			tess.setColorRGBA_I(12632256, 255);
			tess.addVertexWithUV(
					scrollLeft,
					var14 + var13 - 1,
					0.0D,
					0.0D,
					1.0D);
			tess.addVertexWithUV(
					scrollRight - 1,
					var14 + var13 - 1,
					0.0D,
					1.0D,
					1.0D);
			tess.addVertexWithUV(scrollRight - 1, var14, 0.0D, 1.0D, 0.0D);
			tess.addVertexWithUV(scrollLeft, var14, 0.0D, 0.0D, 0.0D);
			tess.draw();
		}

		// this.func_27257_b(x, y);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	/**
	 * Overlays the background to hide scrolled items
	 */
	private void overlayBackground(int par1, int par2, int par3, int par4) {
		Tessellator var5 = Tessellator.instance;
		/**GL11.glBindTexture(
				GL11.GL_TEXTURE_2D,
				this.mc.renderEngine.getTexture("/gui/background.png"));**/
		mc.renderEngine.bindTexture(ResourceLib.BACKGROUND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float var6 = 32.0F;
		var5.startDrawingQuads();
		var5.setColorRGBA_I(4210752, par4);
		var5.addVertexWithUV(0.0D, par2, 0.0D, 0.0D, par2 / var6);
		var5.addVertexWithUV(
				this.width,
				par2,
				0.0D,
				this.width / var6,
				par2 / var6);
		var5.setColorRGBA_I(4210752, par3);
		var5.addVertexWithUV(
				this.width,
				par1,
				0.0D,
				this.width / var6,
				par1 / var6);
		var5.addVertexWithUV(0.0D, par1, 0.0D, 0.0D, par1 / var6);
		var5.draw();
	}
}
