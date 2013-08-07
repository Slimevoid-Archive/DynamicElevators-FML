package slimevoid.elevators.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

import slimevoid.elevators.core.lib.ResourceLib;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElevatorOptionsButton extends GuiButton {

	public GuiElevatorOptionsButton(int i, int j, int k) {
		super(i, j, k, 16, 16, "");
	}

	@Override
	public void drawButton(Minecraft minecraft, int i, int j) {
		FontRenderer fontrenderer = minecraft.fontRenderer;
		/**GL11.glBindTexture(
				GL11.GL_TEXTURE_2D,
				minecraft.renderEngine.getTexture("/gui/elevatorgui.png"));**/
		minecraft.renderEngine.func_110577_a(ResourceLib.GUI_ELEVATOR);
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		boolean overButton = i >= this.xPosition && j >= this.yPosition && i < this.xPosition + this.width && j < this.yPosition + this.height;
		int selected = this.getHoverState(overButton) - 1;
		if (selected < 0 || selected > 1) {
			selected = 0;
		}

		drawTexturedModalRect(xPosition, yPosition, 215, 21 * selected, 21, 21);

		drawString(
				fontrenderer,
				displayString,
				xPosition + 16 + 1,
				yPosition + height / 2 - 2,
				0xffffa0);
	}
}
