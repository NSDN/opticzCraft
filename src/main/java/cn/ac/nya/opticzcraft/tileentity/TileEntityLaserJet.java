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
 * <p>Ported to Minecraft 1.21.1. {@code this.world}/{@code this.pos} become {@code this.level}/
 * {@code this.worldPosition}; {@code pos.add(vec)} becomes {@code worldPosition.offset(x, y, z)}.</p>
 */
public class TileEntityLaserJet extends AbsOpticzDev {

    public float waveLength = 532.0F;
    public float laserPower = LASER_DEFAULT_POWER;
    public float actualPower = 0.0F;

    public TileEntityLaserJet(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected float powerLimit() {
        return laserPower;
    }

    @Override
    public void input(float wave, float power, float phase, Direction facing) {

    }

    @Override
    public void tick(Direction facing) {
        Vec3i vec = IOpticzDev.transform(this.level, this.worldPosition, facing);
        this.pushOutput(facing, this.waveLength, this.actualPower, 0.0F);
        IOpticzDev.perform(this.level, this.worldPosition, facing, this.actualPower);
        this.putOutpoint(facing, vec);
        BlockEntity tileEntity = this.level.getBlockEntity(
                this.worldPosition.offset(vec.getX(), vec.getY(), vec.getZ()));
        if (tileEntity instanceof IOpticzDev dev) {
            dev.input(this.waveLength, this.actualPower, 0.0F, facing.getOpposite());
        }
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);

        this.waveLength = tagCompound.getFloat("waveLength");
        this.laserPower = tagCompound.getFloat("laserPower");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putFloat("waveLength", this.waveLength);
        tagCompound.putFloat("laserPower", this.laserPower);

        return super.toNBT(tagCompound);
    }

}
