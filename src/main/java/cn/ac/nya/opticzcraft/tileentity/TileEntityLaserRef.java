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
 * <p>Ported to Minecraft 1.21.1: {@code EnumFacing} to {@link Direction}, {@code this.world}/
 * {@code this.pos} to {@code this.level}/{@code this.worldPosition}, {@code pos.add(vec)} to
 * {@code worldPosition.offset(...)}. The reflector bends every incoming beam towards its own facing
 * with a {@code -$PI4} phase shift on the back face.</p>
 */
public class TileEntityLaserRef extends AbsOpticzDev {

    public TileEntityLaserRef(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
                    shift = -$PI4;

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
            } else {
                this.calcInput(f);
            }
        }
        IOpticzDev.perform(level, worldPosition, facing, this.getOutput(facing));
    }

}
