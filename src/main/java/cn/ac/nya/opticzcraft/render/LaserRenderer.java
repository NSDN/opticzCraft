package cn.ac.nya.opticzcraft.render;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.AbsOpticzDev;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class LaserRenderer extends AbsTESR {

    @Override
    public boolean isGlobalRenderer(TileEntity te) {
        return true;
    }

    @Override
    public void render(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y  + 0.5F, (float) z + 0.5F);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //GL11.glLineWidth(2.0F);
        //GL11.glEnable(GL11.GL_LINE_SMOOTH);

        render(te, x, y, z);

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopAttrib();

        GL11.glPopMatrix();
    }

    @Override
    public void render(@Nonnull TileEntity te, double x, double y, double z) {
        if (te instanceof AbsOpticzDev) {
            AbsOpticzDev dev = (AbsOpticzDev) te;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            final double radius = 0.0625 / 2;
            for (EnumFacing facing : EnumFacing.VALUES) {
                Vec3i veci = dev.getOutpoint(facing);
                if (veci != null) {
                    IOpticzDev.Color4 color = dev.colorOf(facing);
                    putLight(veci, radius, buffer, color);
                }
            }

            tessellator.draw();
        }
    }

    private void putLight(Vec3i vec3i, double radius, BufferBuilder buffer, IOpticzDev.Color4 color) {
        int x = vec3i.getX(), y = vec3i.getY(), z = vec3i.getZ();
        if (x != 0 && y == 0 && z == 0) {
            buffer.pos(0, 0 + radius, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y + radius, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y + radius, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0, 0 + radius, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0, 0 + radius, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y + radius, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y - radius, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0, 0 - radius, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0, 0 - radius, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y - radius, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y - radius, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0, 0 - radius, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0, 0 - radius, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y - radius, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x, y + radius, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0, 0 + radius, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();
        } else if (x == 0 && y != 0 && z == 0) {
            buffer.pos(0 + radius, 0, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 + radius, 0, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0 + radius, 0, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 - radius, 0, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0 - radius, 0, 0 - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y, z - radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 - radius, 0, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0 - radius, 0, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y, z + radius).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 + radius, 0, 0 + radius).color(color.r, color.g, color.b, color.a).endVertex();
        } else if (x == 0 && y == 0 && z != 0) {
            buffer.pos(0 + radius, 0 + radius, 0).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y + radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y - radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 + radius, 0 - radius, 0).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0 + radius, 0 - radius, 0).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y - radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y - radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 - radius, 0 - radius, 0).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0 - radius, 0 - radius, 0).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y - radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y + radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 - radius, 0 + radius, 0).color(color.r, color.g, color.b, color.a).endVertex();

            buffer.pos(0 - radius, 0 + radius, 0).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x - radius, y + radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(x + radius, y + radius, z).color(color.r, color.g, color.b, color.a).endVertex();
            buffer.pos(0 + radius, 0 + radius, 0).color(color.r, color.g, color.b, color.a).endVertex();
        }
    }

}
