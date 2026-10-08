package cn.ac.nya.opticzcraft;

import cn.ac.nya.opticzcraft.block.AbsOpticzBlock;
import cn.ac.nya.opticzcraft.block.BlockLaserDet;
import cn.ac.nya.opticzcraft.block.BlockLaserJet;
import cn.ac.nya.opticzcraft.block.BlockLaserPbs;
import cn.ac.nya.opticzcraft.block.BlockLaserRec;
import cn.ac.nya.opticzcraft.block.BlockLaserRef;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration for {@code opticzcraft} blocks and their items.
 *
 * <p>Replaces the 1.12.2 {@code block.BlockLoader}, which built a {@code LinkedList<Block>} plus a
 * {@code LinkedHashMap<Block, Item>}, registered them through
 * {@code RegistryEvent.Register<Block>}/{@code <Item>}, and bound item icons through a client-only
 * {@code ModelRegistryEvent} handler. All three of those hooks are gone in 1.21.1: registration is a
 * {@code DeferredRegister} table walk, and item models are plain JSON under
 * {@code assets/opticzcraft/models/item/} (no {@code ModelLoader.setCustomModelResourceLocation}).</p>
 *
 * <p>The 1.12.2 loader registered exactly these seven blocks, in this order: the five devices, then
 * the breakable splitter and reflector produced by {@code BlockLaserPbs.getSoft()} /
 * {@code BlockLaserRef.getSoft()}. Here the two "soft" variants are ordinary table entries with their
 * own properties, which removes the static factory without changing what is registered.</p>
 *
 * <p>Properties note: the 1.12.2 base class set {@code Material.IRON}, hardness -1 (unbreakable),
 * resistance 10, {@code SoundType.METAL}, no opacity and a light value of 15 while {@code LIGHTED} was
 * set. 1.21.1 has no {@code Material} and no {@code getLightValue} override point (verified with
 * javap: {@code BlockBehaviour} exposes neither {@code getLightEmission} nor {@code Material}), so the
 * light function is declared here as {@code lightLevel(state -> state.getValue(LIGHTED) ? 15 : 0)} and
 * the material becomes {@code MapColor.METAL}.</p>
 */
public final class OpticzBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(opticzCraft.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(opticzCraft.MODID);

    /** Every custom {@link Block} subclass needs its codec in {@code BuiltInRegistries.BLOCK_TYPE}. */
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, opticzCraft.MODID);

    private OpticzBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_TYPES.register(modBus);
    }

    static {
        BLOCK_TYPES.register("laser_jet", () -> BlockLaserJet.CODEC);
        BLOCK_TYPES.register("laser_det", () -> BlockLaserDet.CODEC);
        BLOCK_TYPES.register("laser_pbs", () -> BlockLaserPbs.CODEC);
        BLOCK_TYPES.register("laser_rec", () -> BlockLaserRec.CODEC);
        BLOCK_TYPES.register("laser_ref", () -> BlockLaserRef.CODEC);
    }

    public static final DeferredBlock<BlockLaserJet> LASER_JET =
            register("laser_jet", BlockLaserJet::new, deviceProperties(-1.0F));

    public static final DeferredBlock<BlockLaserDet> LASER_DET =
            register("laser_det", BlockLaserDet::new, deviceProperties(-1.0F));

    public static final DeferredBlock<BlockLaserPbs> LASER_PBS =
            register("laser_pbs", BlockLaserPbs::new, deviceProperties(-1.0F));

    public static final DeferredBlock<BlockLaserRec> LASER_REC =
            register("laser_rec", BlockLaserRec::new, deviceProperties(-1.0F));

    public static final DeferredBlock<BlockLaserRef> LASER_REF =
            register("laser_ref", BlockLaserRef::new, deviceProperties(-1.0F));

    public static final DeferredBlock<BlockLaserPbs> LASER_PBS_SOFT =
            register("laser_pbs_soft", BlockLaserPbs::new, deviceProperties(0.5F));

    public static final DeferredBlock<BlockLaserRef> LASER_REF_SOFT =
            register("laser_ref_soft", BlockLaserRef::new, deviceProperties(0.5F));

    /**
     * A fresh property set per block on purpose: {@code Properties} is mutable, so sharing one
     * instance across the table would leak later tweaks into every sibling.
     *
     * @param hardness -1 for the unbreakable devices, 0.5 for the two breakable variants
     */
    private static BlockBehaviour.Properties deviceProperties(float hardness) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(hardness, 10.0F)
                .noOcclusion()
                .lightLevel(state -> state.getValue(AbsOpticzBlock.LIGHTED) ? 15 : 0);
    }

    private static <T extends Block> DeferredBlock<T> register(String name,
                                                              java.util.function.Function<BlockBehaviour.Properties, T> factory,
                                                              BlockBehaviour.Properties properties) {
        DeferredBlock<T> block = BLOCKS.register(name, () -> factory.apply(properties));
        // The 1.12.2 loader registered one ItemBlock per block under the block's registry path; this
        // is the modern equivalent and keeps the item name identical to the block name.
        ITEMS.registerSimpleBlockItem(block);
        return block;
    }
}
