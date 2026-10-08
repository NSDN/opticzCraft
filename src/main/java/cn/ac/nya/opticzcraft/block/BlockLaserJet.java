package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.OpticzBlockEntities;
import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserJet;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
 * <p>Ported to Minecraft 1.21.1. The laser source runs only while it receives a redstone signal, and
 * its power is that signal scaled by the configured laser power.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code world.isBlockIndirectlyGettingPowered(pos)} becomes
 *       {@link net.minecraft.world.level.SignalGetter#getBestNeighborSignal(BlockPos)}: the old method
 *       returned the 0..15 strength and this block actually uses the magnitude
 *       ({@code laserPower * redPower / 15}), so the boolean {@code hasNeighborSignal} would have lost
 *       information.</li>
 *   <li>{@code onBlockActivated} becomes {@link #useItemOn}. 1.12.2 returned {@code false} when the
 *       held stack was not a written book, which is {@code PASS} here.</li>
 *   <li>{@code TextComponentString} becomes {@link Component#literal}; {@code TextFormatting} becomes
 *       {@code ChatFormatting}, whose {@code toString()} still carries the section-sign prefix.</li>
 * </ul>
 */
public class BlockLaserJet extends AbsOpticzBlock {

    public static final MapCodec<BlockLaserJet> CODEC = simpleCodec(BlockLaserJet::new);

    public BlockLaserJet(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Supplier<BlockEntityType<?>> blockEntityTypeSupplier() {
        return OpticzBlockEntities::laserJetType;
    }

    @Override
    protected BlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityLaserJet(type, pos, state);
    }

    @Override
    public void tick(Level world, BlockPos pos, IOpticzDev dev, Direction facing) {
        int redPower = world.getBestNeighborSignal(pos);
        if (redPower > 0) {
            if (dev instanceof TileEntityLaserJet jet) {
                jet.actualPower = jet.laserPower * redPower / 15.0F;
            }
            dev.tick(facing);
        }
        BlockState state = world.getBlockState(pos);
        world.setBlockAndUpdate(pos, state.setValue(LIGHTED, redPower > 0));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction facing) {
        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityLaserJet jet) {

            if (!stack.isEmpty()) {
                String str = getLineFromBook(stack);
                if (str == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

                if (!world.isClientSide) {
                    str = str.split("\n")[0];

                    if (str.split(",").length > 1) {
                        String pwr = str.split(",")[1];
                        float power = -1;
                        try {
                            power = Float.parseFloat(pwr);
                        } catch (Exception e) {
                            message(player, net.minecraft.ChatFormatting.RED + "PARSE ERR AT POWER");
                        }
                        if (power < 0) {
                            message(player, net.minecraft.ChatFormatting.RED + "POWER ERR");
                        } else {
                            jet.laserPower = power;
                            message(player, String.format("Power: %1.1f mW", power));
                        }
                    }
                    str = str.split(",")[0];

                    float waveLen = -1;
                    try {
                        waveLen = Float.parseFloat(str);
                    } catch (Exception e) {
                        message(player, net.minecraft.ChatFormatting.RED + "PARSE ERR AT WAVELENGTH");
                    }
                    if (waveLen < 0) {
                        message(player, net.minecraft.ChatFormatting.RED + "WAVELENGTH ERR");
                    } else {
                        jet.waveLength = waveLen;
                        message(player, String.format("Wavelength: %1.1f nm", waveLen));
                    }
                }

                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

}
