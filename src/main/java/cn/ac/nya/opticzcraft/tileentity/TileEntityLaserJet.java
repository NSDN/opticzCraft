package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TileEntityLaserJet extends AbsOpticzDev {

    public float waveLength = 532.0F;
    public float laserPower = LASER_DEFAULT_POWER;
    public float actualPower = 0.0F;

    @Override
    protected float powerLimit() {
        return laserPower;
    }

    @Override
    public void input(float wave, float power, float phase, EnumFacing facing) {

    }

    @Override
    public void tick(EnumFacing facing) {
        Vec3i vec = IOpticzDev.transform(this.world, this.pos, facing);
        this.pushOutput(facing, this.waveLength, this.actualPower, 0.0F);
        IOpticzDev.perform(world, pos, facing, this.actualPower);
        this.putOutpoint(facing, vec);
        TileEntity tileEntity = this.world.getTileEntity(this.pos.add(vec));
        if (tileEntity instanceof IOpticzDev) {
            IOpticzDev dev = (IOpticzDev) tileEntity;
            dev.input(this.waveLength, this.actualPower, 0.0F, facing.getOpposite());
        }
    }

    @Override
    public void fromNBT(NBTTagCompound tagCompound) {
        super.fromNBT(tagCompound);

        this.waveLength = tagCompound.getFloat("waveLength");
        this.laserPower = tagCompound.getFloat("laserPower");
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
        tagCompound.setFloat("waveLength", this.waveLength);
        tagCompound.setFloat("laserPower", this.laserPower);

        return super.toNBT(tagCompound);
    }

}
