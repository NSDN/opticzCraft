package cn.ac.nya.opticzcraft.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1: {@code EnumFacing} to {@link Direction}. The receiver only
 * accumulates its input face, so nothing else changed.</p>
 */
public class TileEntityLaserRec extends AbsOpticzDev {

    public float powerLimit = LASER_DEFAULT_POWER;

    public TileEntityLaserRec(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void input(float wave, float power, float phase, Direction facing) {
        this.pushInput(facing, wave, power, phase);
    }

    @Override
    public void tick(Direction facing) {
        Face face = this.getInputFace(facing);
        if (face.isEmpty())
            this.calcInput(facing);
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
