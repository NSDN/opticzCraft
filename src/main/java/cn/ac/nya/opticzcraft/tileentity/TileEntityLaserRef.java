package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TileEntityLaserRef extends AbsOpticzDev {

    @Override
    public void input(float wave, float power, float phase, EnumFacing facing) {
        this.pushInput(facing, wave, power, phase);
    }

    @Override
    public void tick(EnumFacing facing) {
        for (EnumFacing f : EnumFacing.VALUES) {
            Face face = this.getInputFace(f);
            if (!face.isEmpty()) {
                float shift = 0;
                if (f == facing.getOpposite())
                    shift = -$PI4;

                Vec3i vec = IOpticzDev.transform(world, pos, facing);
                this.putOutpoint(facing, vec);
                for (Wave wave : face.values()) {
                    this.pushOutput(facing, wave.waveLen(), wave.power(), wave.phase() - shift);
                    TileEntity tileEntity = this.world.getTileEntity(this.pos.add(vec));
                    if (tileEntity instanceof IOpticzDev) {
                        IOpticzDev dev = (IOpticzDev) tileEntity;
                        dev.input(wave.waveLen(), wave.power(), wave.phase() - shift, facing.getOpposite());
                    }
                }
            } else {
                this.calcInput(f);
            }
        }
        IOpticzDev.perform(world, pos, facing, this.getOutput(facing));
    }

}
