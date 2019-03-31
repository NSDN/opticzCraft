package cn.ac.nya.opticzcraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TileEntityLaserRec extends AbsOpticzDev {

    @Override
    public void input(float wave, float power, float phase, EnumFacing facing) {
        this.pushInput(facing, wave, power, phase);
    }

    @Override
    public void tick(EnumFacing facing) {

    }

    public float powerLimit = LASER_DEFAULT_POWER;

    @Override
    protected float powerLimit() {
        return powerLimit;
    }

    @Override
    public void fromNBT(NBTTagCompound tagCompound) {
        super.fromNBT(tagCompound);

        powerLimit = tagCompound.getFloat("powerLimit");
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
        tagCompound.setFloat("powerLimit", powerLimit);

        return super.toNBT(tagCompound);
    }

}
