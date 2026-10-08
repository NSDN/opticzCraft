package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.OpticzBlockEntities;
import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRef;
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
 * <p>Ported to Minecraft 1.21.1. Like the splitter, the reflector has two registered variants that
 * differ only in hardness: {@code laser_ref} (unbreakable) and {@code laser_ref_soft} (hardness 0.5),
 * which 1.12.2 produced through the static {@code getSoft()} factory. With {@code DeferredRegister}
 * both are plain entries in the block table, so the factory is not carried over and the class keeps a
 * one-argument constructor (and therefore {@code simpleCodec}).</p>
 */
public class BlockLaserRef extends AbsOpticzBlock {

    public static final MapCodec<BlockLaserRef> CODEC = simpleCodec(BlockLaserRef::new);

    public BlockLaserRef(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Supplier<BlockEntityType<?>> blockEntityTypeSupplier() {
        return OpticzBlockEntities::laserRefType;
    }

    @Override
    protected BlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityLaserRef(type, pos, state);
    }

    @Override
    public void tick(Level world, BlockPos pos, IOpticzDev dev, Direction facing) {
        dev.tick(facing);
        BlockState state = world.getBlockState(pos);
        world.setBlockAndUpdate(pos, state.setValue(LIGHTED, dev.hasInput()));
    }

}
