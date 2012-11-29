package elevators.render;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Render;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;
import net.minecraft.src.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import elevators.core.DECore;
import elevators.entities.EntityElevator;

@SideOnly(Side.CLIENT)
public class RenderElevator extends Render {
	private RenderBlocks renderBlocks = new RenderBlocks();;

	public RenderElevator() {
		shadowSize = 0.5F;
	}

	public void renderElevatorEntity(Block elevator, World world, int i, int j,
			int k, int textureData[]) {
		System.out.println("renderElevatorEntity");
		float f = 0.5F;
		float f1 = 1.0F;
		float f2 = 0.8F;
		float f3 = 0.6F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setBrightness(elevator.getMixedBrightnessForBlock(world, i,
				j, k));
		float f4 = 1.0F;
		float f5 = 1.0F;

		if (f5 < f4) {
			f5 = f4;
		}

		tessellator.setColorOpaque_F(f * f5, f * f5, f * f5);
		renderBlocks.renderBlockByRenderType(elevator, i, j, k);//.renderBottomFace(elevator, -0.5D, -0.5D, -0.5D,
				//textureData[0]);
		f5 = 1.0F;

		if (f5 < f4) {
			f5 = f4;
		}

		tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
		System.out.println("topFace");
		renderBlocks.renderTopFace(elevator, -0.5D, -0.5D, -0.5D,
				textureData[1]);
		f5 = 1.0F;

		if (f5 < f4) {
			f5 = f4;
		}

		tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		System.out.println("eastFace");
		renderBlocks.renderEastFace(elevator, -0.5D, -0.5D, -0.5D,
				textureData[2]);
		f5 = 1.0F;

		if (f5 < f4) {
			f5 = f4;
		}

		tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		System.out.println("westFace");
		renderBlocks.renderWestFace(elevator, -0.5D, -0.5D, -0.5D,
				textureData[2]);
		f5 = 1.0F;

		if (f5 < f4) {
			f5 = f4;
		}

		tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		System.out.println("northFace");
		renderBlocks.renderNorthFace(elevator, -0.5D, -0.5D, -0.5D,
				textureData[2]);
		f5 = 1.0F;

		if (f5 < f4) {
			f5 = f4;
		}

		tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		System.out.println("southFace");
		renderBlocks.renderSouthFace(elevator, -0.5D, -0.5D, -0.5D,
				textureData[2]);
		tessellator.draw();
	}

	public void doRenderElevator(EntityElevator elevator, double d, double d1,
			double d2, float f, float f1) {
		GL11.glPushMatrix();
		Block block = Block.blocksList[DECore.Elevator.blockID];
		World world = elevator.getWorld();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		GL11.glTranslatef((float) d, (float) d1, (float) d2);
		// GL11.glScalef(-1F, -1F, 1.0F); - ceilings?
		this.loadTexture("/terrain.png");

		// int textureData[] = elevator.getTextureData();
		int textureData[] = { DECore.sideTexture, DECore.sideTexture,
				DECore.sideTexture };

		// Bottom
		textureData[0] = elevator.isCeiling() ? DECore.topTexture
				: DECore.sideTexture;
		// Top
		textureData[1] = elevator.isCeiling() ? DECore.sideTexture
				: DECore.topTexture;
		// Sides
		textureData[2] = DECore.sideTexture;

		renderElevatorEntity(block, world,
				MathHelper.floor_double(elevator.posX),
				MathHelper.floor_double(elevator.posY),
				MathHelper.floor_double(elevator.posZ), textureData);
		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity entity, double d, double d1, double d2,
			float f, float f1) {
		this.doRenderElevator((EntityElevator) entity, d, d1, d2, f, f1);
	}
}
