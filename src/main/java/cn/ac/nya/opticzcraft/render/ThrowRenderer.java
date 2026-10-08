package cn.ac.nya.opticzcraft.render;

import cn.ac.nya.opticzcraft.entity.EntityThrow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the {@link EntityThrow} pressure bomb: the item the projectile carries, drawn as a camera
 * billboard. Ported from the 1.12.2 {@code render.ThrowRenderer}.
 *
 * <p>Mapping from 1.12.2 {@code Render<EntityThrow>} to 1.21.1 {@link EntityRenderer}:</p>
 * <ul>
 *   <li>{@code Render<T>} becomes {@code EntityRenderer<T>} with the
 *       {@link EntityRendererProvider.Context} constructor.</li>
 *   <li>{@code getEntityTexture} becomes {@link #getTextureLocation(EntityThrow)}; the value is kept
 *       verbatim ({@code TextureMap.LOCATION_BLOCKS_TEXTURE} -&gt;
 *       {@link TextureAtlas#LOCATION_BLOCKS}). Note that 1.21.1 item rendering resolves its own
 *       texture through the item model, so this location is metadata for the dispatcher rather than a
 *       bound state -- the original called {@code bindTexture} on top of it and {@code renderItem}
 *       rebound the atlas anyway.</li>
 *   <li>{@code GlStateManager.translate((float)x, (float)y, (float)z)} disappears: the pose stack
 *       arrives at the entity. The billboard rotation is kept, but built the way vanilla's
 *       {@code ThrownItemRenderer} builds it in 1.21.1 -- the 1.12.2 code read the render manager's
 *       {@code playerViewY}/{@code playerViewX} (and flipped X for the reversed third-person view),
 *       which is exactly what {@link net.minecraft.client.renderer.entity.EntityRenderDispatcher}
 *       {@code cameraOrientation()} composes.</li>
 *   <li>{@code RenderItem.renderItem(stack, ItemCameraTransforms.TransformType.GROUND)} becomes
 *       {@code ItemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, ...)} with the packed light
 *       and {@link OverlayTexture#NO_OVERLAY}; {@code ItemCameraTransforms.TransformType} is
 *       {@link ItemDisplayContext} in 1.21.1 and {@code GROUND} survives under that name. The seed is
 *       the entity id, as vanilla uses it.</li>
 *   <li>The {@code GlStateManager.scale(1,1,1)} no-op and the {@code enableRescaleNormal} pair are
 *       dropped (no counterparts); the {@code renderOutlines}/{@code enableColorMaterial} branch is
 *       dropped for the same reason as in {@link ArrowRenderer} -- see the NOT PORTED note there.</li>
 * </ul>
 *
 * <pre>
 * NOT PORTED -- 1.12.2 {@code ItemStack(entityThrow.getItem())}:
 *   {@code getItem()} returned the {@code Item} the bomb renders as. In 1.21.1 that name is taken by
 *   {@code ThrowableItemProjectile#getItem()} returning an {@code ItemStack}, and
 *   {@link EntityThrow} supplies it through {@code getDefaultItem()}. The stack is therefore read with
 *   {@code entity.getItem()} instead of rebuilding it from an {@code Item}.
 * </pre>
 */
public class ThrowRenderer extends EntityRenderer<EntityThrow> {

    private final ItemRenderer itemRenderer;

    public ThrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    /** 1.12.2 {@code getEntityTexture}: {@code TextureMap.LOCATION_BLOCKS_TEXTURE}. */
    @Override
    public ResourceLocation getTextureLocation(EntityThrow entity) {
        // TextureAtlas.LOCATION_BLOCKS holds this exact value but is marked @Deprecated in 1.21.1
        // (vanilla's own ThrownItemRenderer still returns it, which is where the javac note came from).
        // InventoryMenu.BLOCK_ATLAS is the same ResourceLocation and is not deprecated.
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(EntityThrow entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // 1.12.2 renderItem.renderItem(stack, ItemCameraTransforms.TransformType.GROUND); this is the
        // same call vanilla's ThrownItemRenderer makes in 1.21.1, with the packed light and
        // OverlayTexture.NO_OVERLAY and the entity id as the deterministic seed.
        ItemStack stack = entity.getItem();
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
