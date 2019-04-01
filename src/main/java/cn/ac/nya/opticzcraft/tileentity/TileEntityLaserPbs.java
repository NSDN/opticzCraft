package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TileEntityLaserPbs extends AbsOpticzDev {

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
                    shift = $PI4;

                if (f.getAxis() != facing.getAxis()) {
                    Vec3i vec;
                    boolean powered = this.world.isBlockIndirectlyGettingPowered(this.pos) > 0;

                    EnumFacing target = f.getOpposite();
                    vec = IOpticzDev.transform(world, pos, target);
                    this.putOutpoint(target, vec);
                    for (Wave wave : face.values()) {
                        this.pushOutput(target, wave.waveLen(), powered ? wave.power() : wave.power() / 2, wave.phase());
                        TileEntity tileEntity = this.world.getTileEntity(this.pos.add(vec));
                        if (tileEntity instanceof IOpticzDev) {
                            IOpticzDev dev = (IOpticzDev) tileEntity;
                            dev.input(wave.waveLen(), powered ? wave.power() : wave.power() / 2, wave.phase(), target.getOpposite());
                        }
                    }
                    IOpticzDev.perform(world, pos, target, this.getOutput(target));

                    if (!powered) {
                        vec = IOpticzDev.transform(world, pos, facing);
                        this.putOutpoint(facing, vec);
                        for (Wave wave : face.values()) {
                            this.pushOutput(facing, wave.waveLen(), wave.power() / 2, wave.phase());
                            TileEntity tileEntity = this.world.getTileEntity(this.pos.add(vec));
                            if (tileEntity instanceof IOpticzDev) {
                                IOpticzDev dev = (IOpticzDev) tileEntity;
                                dev.input(wave.waveLen(), wave.power() / 2, wave.phase(), facing.getOpposite());
                            }
                        }
                    }
                } else {
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
                }
            } else {
                this.calcInput(f);
            }
        }
        IOpticzDev.perform(world, pos, facing, this.getOutput(facing));
    }

}
