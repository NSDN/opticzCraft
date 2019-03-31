package cn.ac.nya.opticzcraft.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * Created by drzzm32 on 2019.3.31.
 */
public abstract class AbsTESR extends TileEntitySpecialRenderer<TileEntity> {

    @Override
    public void render(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y  + 0.5F, (float) z + 0.5F);

        RenderHelper.disableStandardItemLighting();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        render(te, x, y, z);

        RenderHelper.enableStandardItemLighting();

        GL11.glPopMatrix();
    }

    public abstract void render(@Nonnull TileEntity te, double x, double y, double z);

}
