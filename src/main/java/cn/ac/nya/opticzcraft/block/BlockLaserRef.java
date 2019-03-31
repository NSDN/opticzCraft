package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRef;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class BlockLaserRef extends AbsOpticzBlock {

    public BlockLaserRef() {
        super("LaserRef", "laser_ref");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityLaserRef();
    }

    @Override
    public void tick(World world, BlockPos pos, IOpticzDev dev, EnumFacing facing) {
        dev.tick(facing);
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.withProperty(LIGHTED, dev.hasInput()));
    }

}
