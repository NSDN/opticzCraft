package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserDet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
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
public class BlockLaserDet extends AbsOpticzBlock {

    public BlockLaserDet() {
        super("LaserDet", "laser_det");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityLaserDet();
    }

    @Override
    public void tick(World world, BlockPos pos, IOpticzDev dev, EnumFacing facing) {
        dev.tick(facing);
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.withProperty(LIGHTED, dev.hasInput()));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase player) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, player);
        return state.withProperty(FACING, facing);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityLaserDet) {
            TileEntityLaserDet det = (TileEntityLaserDet) tileEntity;

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
                        det.powerLimit = powerLimit;
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

    public int getInput(IOpticzDev dev, EnumFacing exclude) {
        int result = 0;
        for (EnumFacing f : EnumFacing.VALUES) {
            if (f != exclude)
                result += dev.getInput(f);
        }
        return result & 0xF;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        state = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IOpticzDev) {
            return getInput((IOpticzDev) tileEntity, state.getValue(FACING).getOpposite());
        }
        return 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        state = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IOpticzDev) {
            int output = getInput((IOpticzDev) tileEntity, state.getValue(FACING).getOpposite());
            return facing == state.getValue(FACING).getOpposite() ? output : output / 2;
        }
        return 0;
    }

}
