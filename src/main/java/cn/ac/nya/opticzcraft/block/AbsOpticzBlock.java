package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Supplier;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1 / NeoForge. Every optic device is a metal shell with one emitting
 * face and a "lit" flag, and its behaviour is driven by a one-tick self-rescheduling block tick.
 * The structural mapping:</p>
 * <ul>
 *   <li>{@code extends BlockContainer} becomes {@code extends Block implements EntityBlock}: there is
 *       no {@code BlockContainer} any more, the block entity arrives through
 *       {@link #newBlockEntity(BlockPos, BlockState)}. The type itself is resolved lazily from the
 *       deferred register in {@link #blockEntityType()} -- a supplier passed into the constructor
 *       would make {@code simpleCodec} unusable for the concrete subclasses, so every laser block
 *       stays a one-argument {@code (Properties)} block.</li>
 *   <li>Hardness/resistance/sound/map colour/light level move into
 *       {@link BlockBehaviour.Properties}; {@code getLightValue(state, world, pos)} has <b>no</b>
 *       override point in 1.21.1 (verified with javap: {@code BlockBehaviour} has no
 *       {@code getLightEmission}), so the light level is declared by the registration code as
 *       {@code lightLevel(state -> state.getValue(LIGHTED) ? 15 : 0)}.</li>
 *   <li>{@code isOpaqueCube()}/{@code isFullCube()} false become {@code noOcclusion()} in the
 *       properties. {@code getBoundingBox} is dropped: the default shape is already the full block,
 *       which is what the old override returned.</li>
 *   <li>{@code getStateFromMeta}/{@code getMetaFromState} disappear with the 1.13 flattening -- the
 *       old meta packed {@code facing} in bits 0..2 and {@code lighted} in bit 3.
 *       {@code createBlockState()} becomes {@link #createBlockStateDefinition}.</li>
 *   <li>{@code onBlockAdded} becomes {@link #onPlace}; {@code updateTick} becomes
 *       {@link #tick(BlockState, ServerLevel, BlockPos, RandomSource)} (server-only by construction,
 *       which is what the old {@code !world.isRemote} guard tested). {@code tickRate} returned 20 but
 *       every schedule used delay 1, so it had no effect and is not carried over.
 *       {@code notifyNeighborsOfStateChange} becomes {@code updateNeighborsAt},
 *       {@code notifyBlockUpdate} becomes {@code sendBlockUpdated}, and
 *       {@code markBlockRangeForRenderUpdate} has no replacement (the renderer handles its own
 *       bounds in 1.21.1).</li>
 *   <li>{@code getStateForPlacement(...)} becomes {@link #getStateForPlacement(BlockPlaceContext)};
 *       {@code EnumFacing.getDirectionFromEntityLiving} is replaced by
 *       {@code context.getNearestLookingDirection().getOpposite()}, which yields the same direction
 *       for every placement case the old helper covered.</li>
 *   <li>{@code isSideSolid} is <b>not ported</b>: 1.21.1 has no per-face solidity hook (verified with
 *       javap). Per-face solidity is derived from the collision shape, and the old override existed
 *       only to make the emitting face pass-through for redstone dust -- reproducing it by inflating
 *       the shape would change how players walk on the block, so it is left out deliberately.</li>
 * </ul>
 */
public abstract class AbsOpticzBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIGHTED = BooleanProperty.create("lighted");

    protected AbsOpticzBlock(BlockBehaviour.Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(LIGHTED, false)
        );
    }

    /** The registered type of this block's block entity, resolved lazily after registration. */
    protected abstract Supplier<BlockEntityType<?>> blockEntityTypeSupplier();

    /** Modern replacement for the 1.12.2 {@code createNewTileEntity()} hook. */
    protected abstract BlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state);

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createBlockEntity(blockEntityTypeSupplier().get(), pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIGHTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) state = defaultBlockState();
        return state.setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    /**
     * Reads the first page of a written book held by the player.
     *
     * <p>1.12.2 read {@code ItemWritableBook} + {@code NBTTagCompound.getTagList("pages", 8)}.
     * 1.21.1 replaced that NBT layout with the {@code WRITABLE_BOOK_CONTENT} data component, so the
     * value is fetched through {@link DataComponents#WRITABLE_BOOK_CONTENT} and its first page is
     * taken with {@code getPages(false)} (unfiltered, i.e. exactly the raw text the old code read).</p>
     */
    public static String getLineFromBook(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        if (itemStack.getItem() instanceof WritableBookItem) {
            WritableBookContent content = itemStack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content == null) return null;
            return content.getPages(false).findFirst().orElse(null);
        }
        return null;
    }

    /** 1.12.2 {@code onBlockAdded}: schedule the first device tick. */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        state = level.getBlockState(pos);
        Direction facing = state.getValue(FACING);
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof IOpticzDev dev) {
            dev.clearOutputs();
            tick(level, pos, dev, facing);
            dev.clearInputs();

            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.relative(facing.getOpposite()), this);

            level.sendBlockUpdated(pos, state, state, 2);

            level.scheduleTick(pos, this, 1);
        }
    }

    /** Shared helper for the device blocks: the 1.12.2 redstone chat messages. */
    protected static void message(net.minecraft.world.entity.player.Player player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }

    public abstract void tick(Level world, BlockPos pos, IOpticzDev dev, Direction facing);

}
