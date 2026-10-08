package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1. The polarising beam splitter splits each incoming beam in two, and
 * the phase shift is applied through {@code IOpticzDev.$PI4}. One 1.12.2 call had no modern
 * counterpart:</p>
 * <ul>
 *   <li>{@code world.isBlockIndirectlyGettingPowered(pos) > 0} becomes
 *       {@code level.hasNeighborSignal(worldPosition)} -- the old method returned the strength 0..15
 *       and only the {@code > 0} test was used here, so the boolean form is exact.</li>
 * </ul>
 */
public class TileEntityLaserPbs extends AbsOpticzDev {

    public TileEntityLaserPbs(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void input(float wave, float power, float phase, Direction facing) {
        this.pushInput(facing, wave, power, phase);
    }

    @Override
    public void tick(Direction facing) {
        for (Direction f : Direction.values()) {
            Face face = this.getInputFace(f);
            if (!face.isEmpty()) {
                float shift = 0;
                if (f == facing.getOpposite())
                    shift = $PI4;

                if (f.getAxis() != facing.getAxis()) {
                    Vec3i vec;
                    boolean powered = this.level.hasNeighborSignal(this.worldPosition);

                    Direction target = f.getOpposite();
                    vec = IOpticzDev.transform(level, worldPosition, target);
                    this.putOutpoint(target, vec);
                    for (Wave wave : face.values()) {
                        this.pushOutput(target, wave.waveLen(), powered ? wave.power() : wave.power() / 2, wave.phase());
                        BlockEntity tileEntity = this.level.getBlockEntity(
                                this.worldPosition.offset(vec.getX(), vec.getY(), vec.getZ()));
                        if (tileEntity instanceof IOpticzDev dev) {
                            dev.input(wave.waveLen(), powered ? wave.power() : wave.power() / 2, wave.phase(), target.getOpposite());
                        }
                    }
                    IOpticzDev.perform(level, worldPosition, target, this.getOutput(target));

                    if (!powered) {
                        vec = IOpticzDev.transform(level, worldPosition, facing);
                        this.putOutpoint(facing, vec);
                        for (Wave wave : face.values()) {
                            this.pushOutput(facing, wave.waveLen(), wave.power() / 2, wave.phase());
                            BlockEntity tileEntity = this.level.getBlockEntity(
                                    this.worldPosition.offset(vec.getX(), vec.getY(), vec.getZ()));
                            if (tileEntity instanceof IOpticzDev dev) {
                                dev.input(wave.waveLen(), wave.power() / 2, wave.phase(), facing.getOpposite());
                            }
                        }
                    }
                } else {
                    Vec3i vec = IOpticzDev.transform(level, worldPosition, facing);
                    this.putOutpoint(facing, vec);
                    for (Wave wave : face.values()) {
                        this.pushOutput(facing, wave.waveLen(), wave.power(), wave.phase() - shift);
                        BlockEntity tileEntity = this.level.getBlockEntity(
                                this.worldPosition.offset(vec.getX(), vec.getY(), vec.getZ()));
                        if (tileEntity instanceof IOpticzDev dev) {
                            dev.input(wave.waveLen(), wave.power(), wave.phase() - shift, facing.getOpposite());
                        }
                    }
                }
            } else {
                this.calcInput(f);
            }
        }
        IOpticzDev.perform(level, worldPosition, facing, this.getOutput(facing));
    }

}
