package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.OpticzBlockEntities;
import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserPbs;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1. The polarising beam splitter has two registered variants that differ
 * only in hardness: {@code laser_pbs} (hardness -1, i.e. unbreakable) and {@code laser_pbs_soft}
 * (hardness 0.5). 1.12.2 produced the soft one through the static {@code getSoft()} factory at
 * registration time; with {@code DeferredRegister} both variants are simply two entries in the block
 * table, so {@code getSoft()} has no successor and the class stays a plain one-argument block (which
 * is what lets it use {@code simpleCodec}).</p>
 */
public class BlockLaserPbs extends AbsOpticzBlock {

    public static final MapCodec<BlockLaserPbs> CODEC = simpleCodec(BlockLaserPbs::new);

    public BlockLaserPbs(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Supplier<BlockEntityType<?>> blockEntityTypeSupplier() {
        return OpticzBlockEntities::laserPbsType;
    }

    @Override
    protected BlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityLaserPbs(type, pos, state);
    }

    @Override
    public void tick(Level world, BlockPos pos, IOpticzDev dev, Direction facing) {
        dev.tick(facing);
        BlockState state = world.getBlockState(pos);
        world.setBlockAndUpdate(pos, state.setValue(LIGHTED, dev.hasInput()));
    }

}
