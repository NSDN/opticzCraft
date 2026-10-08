package cn.ac.nya.opticzcraft.render;

import cn.ac.nya.opticzcraft.tileentity.AbsOpticzDev;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

/**
 * Created by drzzm32 on 2019.3.31.
 *
 * <p>Ported to Minecraft 1.21.1 / NeoForge from the 1.12.2
 * {@code render.AbsTESR} ({@code extends TileEntitySpecialRenderer<TileEntity>}). It is the common
 * base of the opticzcraft block-entity renderers and keeps exactly one responsibility from the
 * original: move the pose to the <b>centre</b> of the block
 * ({@code glTranslatef(x + 0.5F, y + 0.5F, z + 0.5F)}) and let the subclass draw around that origin.
 * Everything that surrounded that translate was immediate-mode GL state and has no 1.21.1 counterpart;
 * the mapping is spelled out below so nothing looks silently dropped.</p>
 *
 * <ul>
 *   <li>{@code TileEntitySpecialRenderer<TileEntity>} becomes {@link BlockEntityRenderer}
 *       {@code <AbsOpticzDev>}. The generic narrows because 1.21.1 binds a renderer to one registered
 *       block entity type instead of dispatching by {@code instanceof} at render time.</li>
 *   <li>1.12.2 had two {@code render} overloads -- the six-argument
 *       {@code render(te, x, y, z, partialTicks, destroyStage, partial)} and an abstract
 *       {@code render(te, x, y, z)} that the subclass implemented. Java generics cannot express that
 *       overload pair any more, so the abstract half is renamed {@link #renderDevice}, the same split
 *       {@code nyasamatelecom}'s {@code api/render/AbsTileEntitySpecialRenderer} uses. Because the
 *       six-argument method is also where the pose was pushed, {@code LaserRenderer} (which overrode
 *       it to add its own blending) overrides {@link #render} as well and still funnels into the same
 *       {@link #renderDevice} slot -- the 1.12.2 call graph is preserved, not flattened.</li>
 *   <li>The coordinates arrive as a {@link PoseStack} already positioned at the block corner, so the
 *       old {@code glTranslatef(x + 0.5F, ...)} becomes {@link PoseStack#translate(float, float, float)}
 *       by {@code (0.5F, 0.5F, 0.5F)}. Drawing goes through {@link MultiBufferSource}.</li>
 * </ul>
 *
 * <pre>
 * NOT PORTED -- immediate-mode GL state that 1.21.1 does not expose to a renderer:
 *   RenderHelper.disableStandardItemLighting() / enableStandardItemLighting()
 *       The 1.21.1 block-entity pass renders with a lightmap-parameterised pipeline and a renderer no
 *       longer toggles the global standard item lighting; the devices draw self-lit geometry through
 *       their own render types instead (see LaserRenderer).
 *   GL11.glBlendFunc / glEnable(GL_BLEND) / glDisable(GL_CULL_FACE) / glShadeModel
 *       Blend and cull state now belong to the RenderType the vertices are written to, so there is no
 *       global state to push here.
 *   Minecraft.isAmbientOcclusionEnabled() -> glShadeModel(SMOOTH|FLAT)
 *       The smooth/flat shading choice is baked into the vertex normals and the render type; nothing in
 *       this base class can select it any more.
 * </pre>
 *
 * <p>What the block entity used to express through {@code getRenderBoundingBox()} /
 * {@code getMaxRenderDistanceSquared()} (both removed from {@code BlockEntity} in 1.21.1) is
 * re-expressed by the concrete renderers through {@code shouldRenderOffScreen} /
 * {@code getViewDistance} -- see {@link LaserRenderer}.</p>
 */
public abstract class AbsOpticzTESR implements BlockEntityRenderer<AbsOpticzDev> {

    @Override
    public void render(AbsOpticzDev blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        this.renderDevice(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();
    }

    /** The 1.12.2 abstract {@code render(TileEntity, double x, double y, double z)} slot. */
    public abstract void renderDevice(AbsOpticzDev blockEntity, float partialTick, PoseStack poseStack,
                                      MultiBufferSource bufferSource, int packedLight, int packedOverlay);
}
