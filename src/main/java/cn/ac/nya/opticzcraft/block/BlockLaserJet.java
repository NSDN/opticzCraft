package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLaserJet;
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
public class BlockLaserJet extends AbsOpticzBlock {

    public BlockLaserJet() {
        super("LaserJet", "laser_jet");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityLaserJet();
    }

    @Override
    public void tick(World world, BlockPos pos, IOpticzDev dev, EnumFacing facing) {
        int redPower = world.isBlockIndirectlyGettingPowered(pos);
        if (redPower > 0) {
            if (dev instanceof TileEntityLaserJet) {
                TileEntityLaserJet jet = (TileEntityLaserJet) dev;
                jet.actualPower = jet.laserPower * redPower / 15.0F;
            }
            dev.tick(facing);
        }
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.withProperty(LIGHTED, redPower > 0));
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing facing) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityLaserJet) {
            TileEntityLaserJet jet = (TileEntityLaserJet) tileEntity;

            ItemStack stack = player.getHeldItem(hand);
            if (!stack.isEmpty()) {
                String str = getLineFromBook(stack);
                if (str == null) return false;

                if (!world.isRemote) {
                    str = str.split("\n")[0];

                    if (str.split(",").length > 1) {
                        String pwr = str.split(",")[1];
                        float power = -1;
                        try {
                            power = Float.parseFloat(pwr);
                        } catch (Exception e) {
                            player.sendMessage(new TextComponentString(TextFormatting.RED + "PARSE ERR AT POWER"));
                        }
                        if (power < 0) {
                            player.sendMessage(new TextComponentString(TextFormatting.RED + "POWER ERR"));
                        } else {
                            jet.laserPower = power;
                            player.sendMessage(new TextComponentString(String.format("Power: %1.1f mW", power)));
                        }
                    }
                    str = str.split(",")[0];

                    float waveLen = -1;
                    try {
                        waveLen = Float.parseFloat(str);
                    } catch (Exception e) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "PARSE ERR AT WAVELENGTH"));
                    }
                    if (waveLen < 0) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "WAVELENGTH ERR"));
                    } else {
                        jet.waveLength = waveLen;
                        player.sendMessage(new TextComponentString(String.format("Wavelength: %1.1f nm", waveLen)));
                    }
                }

                return true;
            }
        }
        return false;
    }

}
