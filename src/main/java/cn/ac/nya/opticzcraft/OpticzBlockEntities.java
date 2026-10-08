package cn.ac.nya.opticzcraft;

import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserDet;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserJet;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserPbs;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRec;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRef;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block entity types for opticzcraft.
 *
 * <p>1.12.2 needed none of this: {@code TileEntityLoader} called
 * {@code GameRegistry.registerTileEntity} once per class and derived the name by stripping
 * {@code "TileEntity"} from the simple name ({@code TileEntityLaserJet} -> {@code tileLaserJet}).
 * 1.21.1 requires a {@code BlockEntityType} to carry the valid blocks, and the registry name is
 * spelled out explicitly here -- so the names below are the deliberate modern successors of that
 * derivation: {@code laser_jet}, {@code laser_det}, {@code laser_pbs}, {@code laser_rec},
 * {@code laser_ref}.</p>
 *
 * <p>The splitter and reflector each have a second, breakable variant. 1.12.2 registered one tile
 * entity class per device and listed both blocks as valid because {@code createNewTileEntity} was
 * reached through the block; in 1.21.1 the type itself must name every block it is valid for, so they
 * are listed together here.</p>
 *
 * <p>Signature note, verified with javap against neoforge-21.1.256-merged.jar:
 * {@code BlockEntityType$BlockEntitySupplier<T>} is {@code T create(BlockPos, BlockState)} -- the
 * entity type is <b>not</b> passed in, unlike the {@code BlockEntity(BlockEntityType, BlockPos,
 * BlockState)} constructor. The suppliers below therefore resolve their own type lazily through the
 * static accessors, which are only evaluated when a block entity is actually created, long after
 * registration.</p>
 */
public final class OpticzBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, opticzCraft.MODID);

    private OpticzBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> LASER_JET =
            BLOCK_ENTITIES.register("laser_jet", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TileEntityLaserJet(laserJetType(), pos, state),
                            OpticzBlocks.LASER_JET.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> LASER_DET =
            BLOCK_ENTITIES.register("laser_det", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TileEntityLaserDet(laserDetType(), pos, state),
                            OpticzBlocks.LASER_DET.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> LASER_PBS =
            BLOCK_ENTITIES.register("laser_pbs", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TileEntityLaserPbs(laserPbsType(), pos, state),
                            OpticzBlocks.LASER_PBS.get(), OpticzBlocks.LASER_PBS_SOFT.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> LASER_REC =
            BLOCK_ENTITIES.register("laser_rec", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TileEntityLaserRec(laserRecType(), pos, state),
                            OpticzBlocks.LASER_REC.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> LASER_REF =
            BLOCK_ENTITIES.register("laser_ref", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TileEntityLaserRef(laserRefType(), pos, state),
                            OpticzBlocks.LASER_REF.get(), OpticzBlocks.LASER_REF_SOFT.get())
                    .build(null));

    /** Convenience accessors for the block classes, resolved lazily after registration. */
    public static BlockEntityType<?> laserJetType() { return LASER_JET.get(); }

    public static BlockEntityType<?> laserDetType() { return LASER_DET.get(); }

    public static BlockEntityType<?> laserPbsType() { return LASER_PBS.get(); }

    public static BlockEntityType<?> laserRecType() { return LASER_REC.get(); }

    public static BlockEntityType<?> laserRefType() { return LASER_REF.get(); }
}
