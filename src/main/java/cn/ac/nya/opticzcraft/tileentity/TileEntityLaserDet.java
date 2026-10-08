package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1: {@code EnumFacing} to {@link Direction}, {@code this.world}/
 * {@code this.pos} to {@code this.level}/{@code this.worldPosition}, {@code pos.add(vec)} to
 * {@code worldPosition.offset(...)}.</p>
 */
public class TileEntityLaserDet extends AbsOpticzDev {

    public float powerLimit = LASER_DEFAULT_POWER;

    public TileEntityLaserDet(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void input(float wave, float power, float phase, Direction facing) {
        this.pushInput(facing, wave, power, phase);
    }

    @Override
    public void tick(Direction facing) {
        for (Direction f : Direction.values()) {
            if (f.getAxis() != facing.getAxis()) {
                Face face = this.getInputFace(f);
                if (!face.isEmpty()) {
                    Direction target = f.getOpposite();
                    Vec3i vec = IOpticzDev.transform(level, worldPosition, target);
                    this.putOutpoint(target, vec);
                    for (Wave wave : face.values()) {
                        this.pushOutput(target, wave.waveLen(), wave.power(), wave.phase());
                        BlockEntity tileEntity = this.level.getBlockEntity(
                                this.worldPosition.offset(vec.getX(), vec.getY(), vec.getZ()));
                        if (tileEntity instanceof IOpticzDev dev) {
                            dev.input(wave.waveLen(), wave.power(), wave.phase(), target.getOpposite());
                        }
                    }
                    IOpticzDev.perform(level, worldPosition, target, this.getOutput(target));
                } else {
                    this.calcInput(f);
                }
            }
        }
    }

    @Override
    protected float powerLimit() {
        return powerLimit;
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);

        powerLimit = tagCompound.getFloat("powerLimit");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putFloat("powerLimit", powerLimit);

        return super.toNBT(tagCompound);
    }

}
