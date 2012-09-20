package ironclad49er.elevators.client;

import ironclad49er.elevators.common.EntityElevator;
import ironclad49er.elevators.common.mod_Elevator;
import net.minecraft.src.*;

import org.lwjgl.opengl.GL11;

public class RenderElevator extends Render {
    private RenderBlocks renderBlocks;

    public RenderElevator() {
        renderBlocks = new RenderBlocks();
        shadowSize = 0.5F;
    }
    
    public void renderElevatorEntity(Block elevator, World world, int i, int j, int k, int textureData[]) {
        float f = 0.5F;
        float f1 = 1.0F;
        float f2 = 0.8F;
        float f3 = 0.6F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(elevator.getMixedBrightnessForBlock(world, i, j, k));
        float f4 = 1.0F;
        float f5 = 1.0F;

        if (f5 < f4) { f5 = f4; }

        tessellator.setColorOpaque_F(f * f5, f * f5, f * f5);
        renderBlocks.renderBottomFace(elevator, -0.5D, -0.5D, -0.5D, textureData[0]);
        f5 = 1.0F;

        if (f5 < f4) { f5 = f4; }

        tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
        renderBlocks.renderTopFace(elevator, -0.5D, -0.5D, -0.5D, textureData[1]);
        f5 = 1.0F;

        if (f5 < f4) { f5 = f4; }

        tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
        renderBlocks.renderEastFace(elevator, -0.5D, -0.5D, -0.5D, textureData[2]);
        f5 = 1.0F;

        if (f5 < f4) { f5 = f4; }

        tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
        renderBlocks.renderWestFace(elevator, -0.5D, -0.5D, -0.5D, textureData[2]);
        f5 = 1.0F;

        if (f5 < f4) { f5 = f4; }

        tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
        renderBlocks.renderNorthFace(elevator, -0.5D, -0.5D, -0.5D, textureData[2]);
        f5 = 1.0F;

        if (f5 < f4) { f5 = f4; }

        tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
        renderBlocks.renderSouthFace(elevator, -0.5D, -0.5D, -0.5D, textureData[2]);
        tessellator.draw();
    }
    

    public void doRenderElevator(EntityElevator elevator, double d, double d1, double d2, float f, float f1) {
    	GL11.glPushMatrix();
        Block block = mod_Elevator.Elevator;
        //World world = elevator.getWorld();
        GL11.glDisable(2896 /*GL_LIGHTING*/);
        GL11.glTranslatef((float)d, (float)d1, (float)d2);
        //GL11.glScalef(-1F, -1F, 1.0F); - ceilings?
        loadTexture("/terrain.png");
        
        // int textureData[] = elevator.getTextureData();
    	int textureData[] = {mod_Elevator.sideTexture, mod_Elevator.sideTexture, mod_Elevator.sideTexture};
    	
    	//Bottom
    	textureData[0] = elevator.isCeiling() ? mod_Elevator.topTexture : mod_Elevator.sideTexture;
    	//Top
    	textureData[1] = elevator.isCeiling() ? mod_Elevator.sideTexture : mod_Elevator.topTexture;
    	//Sides
    	textureData[2] = mod_Elevator.sideTexture;
        
        renderElevatorEntity(block, elevator.getWorld(), MathHelper.floor_double(elevator.posX), MathHelper.floor_double(elevator.posY), MathHelper.floor_double(elevator.posZ), textureData);
        GL11.glEnable(2896 /*GL_LIGHTING*/);
        GL11.glPopMatrix();
    }
    
    @Override
    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
        doRenderElevator((EntityElevator)entity, d, d1, d2, f, f1);
    }
}
