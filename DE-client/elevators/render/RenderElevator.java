package elevators.render;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Render;
import net.minecraft.src.World;

import org.lwjgl.opengl.GL11;

import elevators.core.DECore;
import elevators.entities.EntityElevator;

public class RenderElevator extends Render {

	public RenderElevator() {
		shadowSize = 0.5F;
	}

	public void renderElevatorEntity(Block elevator, World world, int x, int y, int z, int textureData[]) {
		this.renderBlocks.func_83018_a(elevator);
		this.renderBlocks.func_78588_a(elevator, world, x, y, z, 0);
	}

	public void doRenderElevator(EntityElevator elevator, double d, double d1, double d2, float f, float f1) {
		GL11.glPushMatrix();
		Block block = DECore.Elevator;
		// World world = elevator.getWorld();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		GL11.glTranslatef((float) d, (float) d1, (float) d2);
		// GL11.glScalef(-1F, -1F, 1.0F); - ceilings?
		loadTexture("/terrain.png");

		// int textureData[] = elevator.getTextureData();
		int textureData[] = {
				DECore.sideTexture,
				DECore.sideTexture,
				DECore.sideTexture };

		// Bottom
		textureData[0] = elevator.isCeiling() ? DECore.topTexture : DECore.sideTexture;
		// Top
		textureData[1] = elevator.isCeiling() ? DECore.sideTexture : DECore.topTexture;
		// Sides
		textureData[2] = DECore.sideTexture;

		renderElevatorEntity(
				block,
				elevator.getWorld(),
				MathHelper.floor_double(elevator.posX),
				MathHelper.floor_double(elevator.posY),
				MathHelper.floor_double(elevator.posZ),
				textureData);
		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		doRenderElevator((EntityElevator) entity, d, d1, d2, f, f1);
	}
}
