package elevators.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElevatorRadialButton extends GuiButton {

	// ID, left, top, display string
	public GuiElevatorRadialButton(int i, int j, int k, String s) {
		super(i, j, k, 35, 16, s);
	}

	public void drawButton(Minecraft minecraft, int i, int j) {
		if (!this.drawButton) {
			return;
		}
		FontRenderer fontrenderer = minecraft.fontRenderer;
		GL11.glBindTexture(3553 /* GL_TEXTURE_2D */,
				minecraft.renderEngine.getTexture("/gui/elevatorgui.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int k = (enabled) ? 0 : 1;
		drawTexturedModalRect(xPosition, yPosition, 215, 42 + k * 13, 13, 13);

		int color = enabled ? 0x000000 : 0x3D3D3D;
		fontrenderer.drawString(displayString, xPosition + 13 + 1, yPosition
				+ height / 2 - 4, color);
	}

	protected void mouseDragged(Minecraft minecraft, int i, int j) {
	}

	public void mouseReleased(int i, int j) {
	}

	public boolean mousePressed(Minecraft minecraft, int i, int j) {
		if (!drawButton) {
			return false;
		}
		// mod_ExpandedArt.say((new
		// StringBuilder()).append(i).append(", ").append(j).toString());
		// mod_ExpandedArt.say((new
		// StringBuilder()).append(xPosition).append(", ").append(yPosition).append(": ").append(width).append(", ").append(height).toString());
		if (i >= xPosition && j >= yPosition && i < xPosition + width
				&& j < yPosition + height) {
			enabled = !enabled;
			return true;
		}
		return false;
	}
}
