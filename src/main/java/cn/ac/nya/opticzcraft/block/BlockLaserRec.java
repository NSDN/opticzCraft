package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.OpticzBlockEntities;
import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * <p>Ported to Minecraft 1.21.1. The receiver accumulates the optical power arriving on its facing side
 * and exposes it as a redstone signal, like the detector but without the per-face distribution.</p>
 *
 * <p>Mapping notes: {@code canProvidePower} -> {@code isSignalSource}, {@code getWeakPower} ->
 * {@code getSignal}, {@code getStrongPower} -> {@code getDirectSignal} (both read
 * {@code dev.getInput(state.getValue(FACING))}, unchanged), and {@code onBlockActivated} ->
 * {@link #useItemOn} for the power-limit configuration.</p>
 */
public class BlockLaserRec extends AbsOpticzBlock {

    public static final MapCodec<BlockLaserRec> CODEC = simpleCodec(BlockLaserRec::new);

    public BlockLaserRec(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Supplier<BlockEntityType<?>> blockEntityTypeSupplier() {
        return OpticzBlockEntities::laserRecType;
    }

    @Override
    protected BlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityLaserRec(type, pos, state);
    }

    @Override
    public void tick(Level world, BlockPos pos, IOpticzDev dev, Direction facing) {
        dev.tick(facing);
        BlockState state = world.getBlockState(pos);
        world.setBlockAndUpdate(pos, state.setValue(LIGHTED, dev.getInput(facing) > 0));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityLaserRec rec) {

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
                        rec.powerLimit = powerLimit;
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

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction facing) {
        state = world.getBlockState(pos);
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof IOpticzDev dev) {
            return dev.getInput(state.getValue(FACING));
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction facing) {
        state = world.getBlockState(pos);
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof IOpticzDev dev) {
            return dev.getInput(state.getValue(FACING));
        }
        return 0;
    }

}
