package slimevoid.elevators.client.gui;

// D.E. - 1.6
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElevatorSlider extends GuiButton {

	/** The value of this slider control. */
	public float	sliderValue		= 1.0F;

	private float	sliderPosition	= 1.0F;

	public float	maximumValue	= 1.0F;

	/** Is this slider control being dragged. */
	public boolean	dragging		= false;

	private boolean	discrete		= false;

	String			message			= "";

	public GuiElevatorSlider(int id, int left, int top, float defaultValue, float maximum, boolean discreteValues, String property) {
		super(id, left, top, 150, 20, "");
		this.sliderValue = defaultValue;
		this.maximumValue = maximum;
		this.sliderPosition = (sliderValue - 1) / maximumValue;
		this.discrete = discreteValues;
		message = property;
		nameString();
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over
	 * this button and 2 if it IS hovering over this button.
	 */
	@Override
	protected int getHoverState(boolean par1) {
		return 0;
	}

	private void nameString() {
		if (discrete) {
			this.displayString = message
									+ String.valueOf((int) this.sliderValue);
		} else {
			this.displayString = message + String.valueOf(this.sliderValue);
		}
		if (this.sliderValue == this.maximumValue + 1) {
			this.displayString = "All Below Ground";
		} else if (this.sliderValue == 1) {
			this.displayString = "All Above Ground";
		}
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of
	 * MouseListener.mouseDragged(MouseEvent e).
	 */
	@Override
	protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
		if (this.drawButton) {
			if (this.dragging) {
				this.sliderPosition = (float) (par2 - (this.xPosition + 4))
										/ (float) (this.width - 8);

				if (discrete) {
					sliderPosition = MathHelper.floor_float((sliderPosition
																* maximumValue + 0.5F))
										/ maximumValue;
				}

				if (this.sliderPosition < 0.0F) {
					this.sliderPosition = 0.0F;
				}

				if (this.sliderPosition > 1.0F) {
					this.sliderPosition = 1.0F;
				}

				sliderValue = 1 + sliderPosition * maximumValue;
				nameString();
			}

			GL11.glColor4f(	1.0F,
							1.0F,
							1.0F,
							1.0F);
			this.drawTexturedModalRect(	this.xPosition
												+ (int) (this.sliderPosition * (this.width - 8)),
										this.yPosition,
										0,
										66,
										4,
										20);
			this.drawTexturedModalRect(	this.xPosition
												+ (int) (this.sliderPosition * (this.width - 8))
												+ 4,
										this.yPosition,
										196,
										66,
										4,
										20);
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		if (super.mousePressed(	par1Minecraft,
								par2,
								par3)) {
			if (this.dragging) {
				this.dragging = false;
				return true;
			}
			this.sliderPosition = (float) (par2 - (this.xPosition + 4))
									/ (float) (this.width - 8);

			if (discrete) {
				sliderPosition = MathHelper.floor_float((sliderPosition
															* maximumValue + 0.5F))
									/ maximumValue;
			}

			if (this.sliderPosition < 0.0F) {
				this.sliderPosition = 0.0F;
			}

			if (this.sliderPosition > 1.0F) {
				this.sliderPosition = 1.0F;
			}
			sliderValue = 1 + sliderPosition * maximumValue;
			nameString();
			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Fired when the mouse button is released. Equivalent of
	 * MouseListener.mouseReleased(MouseEvent e).
	 */
	@Override
	public void mouseReleased(int par1, int par2) {
		this.dragging = false;
	}
}
