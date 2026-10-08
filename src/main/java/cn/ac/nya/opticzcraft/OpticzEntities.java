package cn.ac.nya.opticzcraft;

import cn.ac.nya.opticzcraft.entity.EntityArrow;
import cn.ac.nya.opticzcraft.entity.EntityThrow;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity types for opticzcraft.
 *
 * <p>Replaces the 1.12.2 {@code entity.EntityLoader}. That class kept a
 * {@code LinkedList<Class<? extends Entity>>}, derived each registry name by stripping the package off
 * the class name ({@code cn.ac.nya.opticzcraft.entity.EntityThrow} -&gt; {@code entity_entitythrow} -
 * &gt; {@code throw}), and pushed the list through {@code RegistryEvent.Register<EntityEntry>} using
 * {@code EntityRegistry.registerModEntity(...)} with the tracking constants {@code 256, 3, true}.</p>
 *
 * <p>1.21.1 has no {@code EntityEntry} and no registry event for entities: a {@code DeferredRegister}
 * table replaces both, the registry name is spelled out explicitly, and the tracking constants move
 * onto the {@code EntityType.Builder}.</p>
 *
 * <p>Signature note, verified with javap against neoforge-21.1.256-merged.jar:
 * {@code EntityType$EntityFactory<T>} is <b>{@code T create(EntityType<T>, Level)}</b> -- the entity
 * type <i>is</i> passed in, the exact opposite of {@code BlockEntityType$BlockEntitySupplier}. No lazy
 * holder trick is needed here (unlike {@link OpticzBlockEntities}): the factory simply forwards the
 * type it is handed, exactly as vanilla's {@code Snowball}/{@code Arrow} do.</p>
 *
 * <p>The 1.12.2 loader derived its ids from the simple class names, landing on {@code throw} and
 * {@code arrow} (the {@code entity_} prefix it also produced was discarded by
 * {@code EntityRegistry#registerModEntity}, which prefixes the mod id itself). Those two ids are kept
 * verbatim so existing worlds and the {@code opticzcraft:throw} / {@code opticzcraft:arrow} item
 * translations line up.</p>
 *
 * <p>Not ported from {@code EntityLoader}: the mod-instance argument and
 * {@code opticzCraft.getInstance()} (the mod instance no longer exists).</p>
 */
public final class OpticzEntities {

    /**
     * Entity tracking range in <b>chunks</b>.
     *
     * <p>1.12.2 expressed this in blocks (256). 1.21.1 measures it in chunks and the chunk map
     * multiplies by 16 again, so copying 256 across would give a 4096-block radius. 256 blocks / 16
     * = 16 chunks restores the original intent. Found by the railway W4 agent, who hit the same
     * unit change in RailwayEntities.</p>
     */
    public static final int TRACKING_RANGE_CHUNKS = 16;


    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, opticzCraft.MODID);

    private OpticzEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    /**
     * 1.12.2 {@code EntityLoader}: {@code entities.add(EntityThrow.class)} -- the NSF Pressure Bomb.
     * Its hitbox is the vanilla snowball's, and {@code (256, 3, true)} becomes
     * {@code clientTrackingRange(256).updateInterval(3).sized(0.25F, 0.25F)}.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityThrow>> THROW =
            ENTITIES.register("throw", () -> EntityType.Builder
                    .<EntityThrow>of(EntityThrow::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(TRACKING_RANGE_CHUNKS)
                    .updateInterval(3)
                    .build(opticzCraft.MODID + ":throw"));

    /**
     * 1.12.2 {@code EntityLoader}: {@code entities.add(EntityArrow.class)} -- the cluster missile. The
     * hitbox is the vanilla arrow's (0.5 &times; 0.5).
     */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityArrow>> ARROW =
            ENTITIES.register("arrow", () -> EntityType.Builder
                    .<EntityArrow>of(EntityArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(TRACKING_RANGE_CHUNKS)
                    .updateInterval(3)
                    .build(opticzCraft.MODID + ":arrow"));

    /** Convenience accessors, mirroring the {@code OpticzBlockEntities.laserJetType()} style. */
    public static EntityType<EntityThrow> throwType() { return THROW.get(); }

    public static EntityType<EntityArrow> arrowType() { return ARROW.get(); }
}
