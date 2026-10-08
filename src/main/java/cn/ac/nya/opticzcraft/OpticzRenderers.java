package cn.ac.nya.opticzcraft;

import cn.ac.nya.opticzcraft.entity.EntityArrow;
import cn.ac.nya.opticzcraft.entity.EntityThrow;
import cn.ac.nya.opticzcraft.render.ArrowRenderer;
import cn.ac.nya.opticzcraft.render.LaserRenderer;
import cn.ac.nya.opticzcraft.render.ThrowRenderer;
import cn.ac.nya.opticzcraft.tileentity.AbsOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserDet;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserJet;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserPbs;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRec;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRef;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-side registration for the opticzcraft renderers.
 *
 * <p>In 1.12.2 this was three call sites in {@code proxy/ClientProxy}:
 * {@code ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserJet.class, new LaserRenderer())}
 * and the same for the other four device classes, plus
 * {@code RenderingRegistry.registerEntityRenderingHandler(EntityArrow.class, ArrowRenderer.FACTORY)}
 * and the throw variant. 1.21.1 replaces all of them with one event on the <b>mod bus</b>:
 * {@link EntityRenderersEvent.RegisterRenderers}. Each binding below is the direct successor of one of
 * those lines -- the five device types each get a {@link LaserRenderer} (the 1.12.2 code created a
 * separate instance per class; the renderer is stateless, so one instance is bound per type here),
 * and the two entity types get their own renderers.</p>
 *
 * <p>Why the entities need this at all: in 1.12.2 they were bound by hand, and in 1.21.1 the
 * {@code EntityRenderers} map is populated <b>only</b> from this event -- there is no reflection
 * fallback. {@code EntityArrow} extends {@code AbstractArrow} but is not the vanilla
 * {@code Arrow} class, so it inherits nothing; {@code EntityThrow} extends
 * {@code ThrowableItemProjectile} and would fall back to {@code ThrownItemRenderer} only if it were
 * registered as a {@code ThrowableItemProjectile}. Both are named explicitly here, so neither depends
 * on a parent-class entry.</p>
 *
 * <p>The class is {@code Dist.CLIENT} throughout: it references {@code BlockEntityRenderer},
 * {@code EntityRenderer} and the render types, which do not exist on a dedicated server. FML only
 * loads a {@code Dist.CLIENT} subscriber on the physical client, so nothing here can be class-loaded
 * server-side -- the same arrangement {@code nyasamaoptics}' {@code OpticsRenderers} uses.</p>
 */
@EventBusSubscriber(modid = opticzCraft.MODID, value = Dist.CLIENT)
public final class OpticzRenderers {

    private OpticzRenderers() {
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        bindBlockEntity(event, OpticzBlockEntities.LASER_JET.get(), TileEntityLaserJet.class,
                LaserRenderer::new);
        bindBlockEntity(event, OpticzBlockEntities.LASER_DET.get(), TileEntityLaserDet.class,
                LaserRenderer::new);
        bindBlockEntity(event, OpticzBlockEntities.LASER_PBS.get(), TileEntityLaserPbs.class,
                LaserRenderer::new);
        bindBlockEntity(event, OpticzBlockEntities.LASER_REC.get(), TileEntityLaserRec.class,
                LaserRenderer::new);
        bindBlockEntity(event, OpticzBlockEntities.LASER_REF.get(), TileEntityLaserRef.class,
                LaserRenderer::new);

        event.registerEntityRenderer(OpticzEntities.THROW.get(), ThrowRenderer::new);
        event.registerEntityRenderer(OpticzEntities.ARROW.get(), ArrowRenderer::new);
    }

    /**
     * Binds a block entity type to a renderer despite the two independent wildcards.
     *
     * <p>{@code OpticzBlockEntities} exposes its types as {@code DeferredHolder<BlockEntityType<?>,
     * BlockEntityType<?>>} -- the element type is erased, so {@code registerBlockEntityRenderer}
     * cannot infer {@code T} from the type argument alone. The {@code Class<? extends T>} parameter
     * pins {@code T} to the concrete block entity class, which makes the cast below the single
     * unavoidable unchecked one and puts it in one place instead of five. The cast is sound by
     * construction: the class passed in is exactly the class the corresponding
     * {@code BlockEntityType.Builder} constructs in {@code OpticzBlockEntities}, and every device
     * renderer here only uses the {@link AbsOpticzDev} half of the hierarchy.</p>
     */
    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> void bindBlockEntity(
            EntityRenderersEvent.RegisterRenderers event, BlockEntityType<?> type,
            Class<? extends T> blockEntityClass, BlockEntityRendererProvider<T> provider) {
        event.registerBlockEntityRenderer((BlockEntityType<T>) type, provider);
    }
}
