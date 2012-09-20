package ironclad49er.elevators.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

public class GuiElevatorOptionsButton extends GuiButton {
	
	public GuiElevatorOptionsButton(int i, int j, int k) {
    	super(i, j, k, 16, 16, "");
    }

    public void drawButton(Minecraft minecraft, int i, int j) {
        FontRenderer fontrenderer = minecraft.fontRenderer;
        GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, minecraft.renderEngine.getTexture("/gui/elevatorgui.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        boolean overButton = i >= this.xPosition && j >= this.yPosition && i < this.xPosition + this.width && j < this.yPosition + this.height;
        int selected = this.getHoverState(overButton) - 1;
        if (selected < 0 || selected > 1) {selected = 0;}

        drawTexturedModalRect(xPosition, yPosition, 215, 21*selected, 21, 21);
 
        drawString(fontrenderer, displayString, xPosition + 16 + 1, yPosition + height/2 - 2, 0xffffa0);
    }
}
