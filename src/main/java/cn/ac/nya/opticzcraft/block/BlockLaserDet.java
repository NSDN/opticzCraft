package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.OpticzBlockEntities;
import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserDet;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1. The detector measures the optical power it receives and mirrors it
 * onto redstone, so it is both a redstone consumer and a redstone source.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code canProvidePower} becomes {@code isSignalSource}; {@code getWeakPower} becomes
 *       {@code getSignal} and {@code getStrongPower} becomes {@code getDirectSignal} -- the same
 *       protected hooks, just renamed.</li>
 *   <li>{@code getStateForPlacement(World, BlockPos, EnumFacing, ...)} becomes
 *       {@link #getStateForPlacement(BlockPlaceContext)} and uses the clicked face, exactly like the
 *       old override did with its {@code facing} argument.</li>
 *   <li>{@code onBlockActivated} becomes {@link #useItemOn} (power-limit configuration from a written
 *       book).</li>
 * </ul>
 */
public class BlockLaserDet extends AbsOpticzBlock {

    public static final MapCodec<BlockLaserDet> CODEC = simpleCodec(BlockLaserDet::new);

    public BlockLaserDet(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Supplier<BlockEntityType<?>> blockEntityTypeSupplier() {
        return OpticzBlockEntities::laserDetType;
    }

    @Override
    protected BlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityLaserDet(type, pos, state);
    }

    @Override
    public void tick(Level world, BlockPos pos, IOpticzDev dev, Direction facing) {
        dev.tick(facing);
        BlockState state = world.getBlockState(pos);
        world.setBlockAndUpdate(pos, state.setValue(LIGHTED, dev.hasInput()));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.setValue(FACING, context.getClickedFace());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityLaserDet det) {

            if (!stack.isEmpty()) {
                String str = getLineFromBook(stack);
                if (str == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

                if (!world.isClientSide) {
                    str = str.split("\n")[0];

                    float powerLimit = -1;
                    try {
                        powerLimit = Float.parseFloat(str);
                    } catch (Exception e) {
                        message(player, net.minecraft.ChatFormatting.RED + "PARSE ERR AT POWER LIMIT");
                    }
                    if (powerLimit < 0) {
                        message(player, net.minecraft.ChatFormatting.RED + "POWER LIMIT ERR");
                    } else {
                        det.powerLimit = powerLimit;
                        message(player, String.format("Power limit: %1.1f mW", powerLimit));
                    }
                }

                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction facing) {
        return true;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    public int getInput(IOpticzDev dev, Direction exclude) {
        int result = 0;
        for (Direction f : Direction.values()) {
            if (f != exclude)
                result += dev.getInput(f);
        }
        return result & 0xF;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction facing) {
        state = world.getBlockState(pos);
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof IOpticzDev dev) {
            return getInput(dev, state.getValue(FACING).getOpposite());
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction facing) {
        state = world.getBlockState(pos);
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof IOpticzDev dev) {
            int output = getInput(dev, state.getValue(FACING).getOpposite());
            return facing == state.getValue(FACING).getOpposite() ? output : output / 2;
        }
        return 0;
    }

}
