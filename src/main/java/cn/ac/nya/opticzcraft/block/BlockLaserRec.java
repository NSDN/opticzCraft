package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserRec;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class BlockLaserRec extends AbsOpticzBlock {

    public BlockLaserRec() {
        super("LaserRec", "laser_rec");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityLaserRec();
    }

    @Override
    public void tick(World world, BlockPos pos, IOpticzDev dev, EnumFacing facing) {
        dev.tick(facing);
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.withProperty(LIGHTED, dev.getInput(facing) > 0));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityLaserRec) {
            TileEntityLaserRec rec = (TileEntityLaserRec) tileEntity;

            ItemStack stack = player.getHeldItem(hand);
            if (!stack.isEmpty()) {
                String str = getLineFromBook(stack);
                if (str == null) return false;

                if (!world.isRemote) {
                    str = str.split("\n")[0];

                    float powerLimit = -1;
                    try {
                        powerLimit = Float.parseFloat(str);
                    } catch (Exception e) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "PARSE ERR AT POWER LIMIT"));
                    }
                    if (powerLimit < 0) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "POWER LIMIT ERR"));
                    } else {
                        rec.powerLimit = powerLimit;
                        player.sendMessage(new TextComponentString(String.format("Power limit: %1.1f mW", powerLimit)));
                    }
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing facing) {
        return true;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        state = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IOpticzDev) {
            return ((IOpticzDev) tileEntity).getInput(state.getValue(FACING));
        }
        return 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        state = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IOpticzDev) {
            return ((IOpticzDev) tileEntity).getInput(state.getValue(FACING));
        }
        return 0;
    }

}
