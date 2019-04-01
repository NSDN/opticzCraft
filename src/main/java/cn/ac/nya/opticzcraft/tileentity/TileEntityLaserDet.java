package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TileEntityLaserDet extends AbsOpticzDev {

    @Override
    public void input(float wave, float power, float phase, EnumFacing facing) {
        this.pushInput(facing, wave, power, phase);
    }

    @Override
    public void tick(EnumFacing facing) {
        for (EnumFacing f : EnumFacing.VALUES) {
            if (f.getAxis() != facing.getAxis()) {
                Face face = this.getInputFace(f);
                if (!face.isEmpty()) {
                    EnumFacing target = f.getOpposite();
                    Vec3i vec = IOpticzDev.transform(world, pos, target);
                    this.putOutpoint(target, vec);
                    for (Wave wave : face.values()) {
                        this.pushOutput(target, wave.waveLen(), wave.power(), wave.phase());
                        TileEntity tileEntity = this.world.getTileEntity(this.pos.add(vec));
                        if (tileEntity instanceof IOpticzDev) {
                            IOpticzDev dev = (IOpticzDev) tileEntity;
                            dev.input(wave.waveLen(), wave.power(), wave.phase(), target.getOpposite());
                        }
                    }
                    IOpticzDev.perform(world, pos, target, this.getOutput(target));
                } else {
                    this.calcInput(f);
                }
            }
        }
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
