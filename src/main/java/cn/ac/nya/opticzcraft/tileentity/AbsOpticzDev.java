package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1 / NeoForge. The wave bookkeeping (per-face input/output faces, the
 * colour averaging, the {@code outPoint}/{@code outColor} arrays) is carried over verbatim; only the
 * host class changed:</p>
 * <ul>
 *   <li>{@code extends TileEntity} becomes {@code extends BlockEntity}, and the no-argument
 *       constructor becomes the mandatory {@code (BlockEntityType, BlockPos, BlockState)} triple --
 *       a 1.21.1 block entity must know its own type. Concrete devices pass their registered type
 *       through.</li>
 *   <li>{@code EnumFacing} to {@link Direction}; {@code getIndex()} to {@link Direction#get3DDataValue()}
 *       (the same 0..5 DOWN/UP/NORTH/SOUTH/WEST/EAST indexing the {@code outPoints}/{@code outColors}
 *       arrays are keyed by).</li>
 *   <li>{@code readFromNBT}/{@code writeToNBT} become {@code loadAdditional}/{@code saveAdditional}
 *       with a {@code HolderLookup.Provider}; the mod's own {@code fromNBT}/{@code toNBT} pair is kept
 *       so subclasses retain their 1.12.2 shape, and {@code hasKey}/{@code setIntArray}/{@code setByteArray}
 *       become {@code contains}/{@code putIntArray}/{@code putByteArray}.</li>
 *   <li>{@code getUpdatePacket}/{@code onDataPacket} collapse into {@link #getUpdatePacket()} returning
 *       a {@code ClientboundBlockEntityDataPacket} -- vanilla applies the payload itself, so
 *       {@code onDataPacket} disappears.</li>
 *   <li>{@code shouldRefresh} disappears: vanilla recreates the block entity when the block changes,
 *       which is exactly what the old override implemented by hand.</li>
 * </ul>
 *
 * <pre>
 * NOT PORTED HERE -- verified with javap against neoforge-21.1.256-merged.jar:
 *   {@code getRenderBoundingBox()} and {@code getMaxRenderDistanceSquared()} no longer exist on
 *   {@code BlockEntity}; both moved to the renderer side ({@code BlockEntityRenderer#shouldRender} /
 *   {@code getViewDistance}). The 1.12.2 overrides returned {@code INFINITE_EXTENT_AABB} and
 *   {@code 65535.0} so that the laser beams stay visible far outside the block; they will be
 *   re-expressed when {@code render/LaserRenderer} is ported.
 * </pre>
 */
public abstract class AbsOpticzDev extends BlockEntity implements IOpticzDev {

    class Face extends LinkedHashMap<String, Wave> {

        public boolean hasWave(Wave wave) {
            return this.containsKey(wave.length());
        }

        public void put(Wave wave) {
            this.put(wave.length(), wave);
        }

    }

    protected LinkedHashMap<Direction, Face> inputs;
    protected LinkedHashMap<Direction, Face> outputs;

    protected Vec3i[] outPoints = new Vec3i[6];

    public void putOutpoint(Direction facing, Vec3i vec) {
        outPoints[facing.get3DDataValue()] = vec;
    }

    public Vec3i getOutpoint(Direction facing) {
        return outPoints[facing.get3DDataValue()];
    }

    public AbsOpticzDev(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        inputs = new LinkedHashMap<>();
        for (Direction facing : Direction.values())
            inputs.put(facing, new Face());
        outputs = new LinkedHashMap<>();
        for (Direction facing : Direction.values())
            outputs.put(facing, new Face());
    }

    protected int[] inputVals = new int[] { 0, 0, 0, 0, 0, 0 };
    protected float powerLimit() {
        return LASER_DEFAULT_POWER;
    }

    public void calcInput(Direction facing) {
        int result = 0;
        if (!inputs.get(facing).isEmpty()) {
            for (Wave wave : inputs.get(facing).values())
                result += (wave.power() / this.powerLimit() * 15);
        }
        if (result > 15) result = 15;
        inputVals[facing.get3DDataValue()] = result;
    }

    @Override
    public float mWInput() {
        float result = 0;
        for (Direction facing : Direction.values()) {
            for (Wave wave : inputs.get(facing).values())
                result += wave.power();
        }
        return result;
    }

    @Override
    public float mWOutput() {
        float result = 0;
        for (Direction facing : Direction.values()) {
            for (Wave wave : outputs.get(facing).values())
                result += wave.power();
        }
        return result;
    }

    public float getOutput(Direction facing) {
        float result = 0;
        for (Wave wave : outputs.get(facing).values())
            result += wave.power();
        return result;
    }

    public float getOutput() {
        float result = 0;
        for (Direction facing : Direction.values())
            result += getOutput(facing);
        return result;
    }

    @Override
    public int getInput(Direction facing) {
        return inputVals[facing.get3DDataValue()];
    }

    @Override
    public int getInput() {
        int result = 0;
        for (Direction facing : Direction.values()) {
            result += getInput(facing);
        }
        if (result > 15) result = 15;
        return result;
    }

    @Override
    public boolean hasInput() {
        return getInput() > 0;
    }

    @Override
    public void clearInputs() {
        for (Direction facing : Direction.values())
            inputs.get(facing).clear();
    }

    @Override
    public void clearOutputs() {
        for (Direction facing : Direction.values())
            outputs.get(facing).clear();
        for (int i = 0; i < outPoints.length; i++)
            outPoints[i] = null;
    }

    public Face getInputFace(Direction facing) { return inputs.get(facing); }

    public Face getOutputFace(Direction facing) { return outputs.get(facing); }

    public void pushInput(Direction facing, float waveLength, float power, float phase) {
        Face face = getInputFace(facing);
        Wave wave = new Wave(waveLength, power, phase % $2PI);
        if (face.hasWave(wave)) {
            Wave old = face.get(wave.length());
            wave = wave.sum(old);
            if (Math.abs(wave.power()) < 1e-4)
                face.remove(wave.length());
        }
        face.put(wave);

        this.calcInput(facing);
    }

    public void pushOutput(Direction facing, float waveLength, float power, float phase) {
        Face face = getOutputFace(facing);
        Wave wave = new Wave(waveLength, power, phase % $2PI);
        if (face.hasWave(wave)) {
            Wave old = face.get(wave.length());
            wave = wave.sum(old);
            if (Math.abs(wave.power()) < 1e-4)
                face.remove(wave.length());
        }
        face.put(wave);

        Color4 color = calculateColor(facing);
        if (color == null) color = Color4.TRANS;
        outColors[facing.get3DDataValue()] = color.toBytes();
    }

    protected byte[][] outColors = new byte[6][];

    protected Color4 calculateColor(Direction facing) {
        LinkedList<Color4> list = new LinkedList<>();
        for (Wave wave : getOutputFace(facing).values()) {
            list.add(IOpticzDev.getColor(wave.waveLen(), wave.power() / this.powerLimit()));
        }
        if (list.isEmpty()) return null;
        Color4 color = list.pop();
        while (!list.isEmpty()) {
            Color4 tmp = list.pop();
            color.r = (color.r * color.a + tmp.r * tmp.a) / 2;
            color.g = (color.g * color.a + tmp.g * tmp.a) / 2;
            color.b = (color.b * color.a + tmp.b * tmp.a) / 2;
            color.a = (color.a * color.a + tmp.a * tmp.a) / 2;
            if (color.a > 1) color.a = 1;
        }
        return color;
    }

    // 1.12.2 marked this @SideOnly(Side.CLIENT) because only the TESR reads it. 1.21.1 has no
    // @SideOnly and the value is plain serialised data, so it is a normal method here.
    @Override
    public Color4 colorOf(Direction facing) {
        return Color4.fromBytes(outColors[facing.get3DDataValue()]);
    }

    public void fromNBT(CompoundTag tagCompound) {
        for (int i = 0; i < outPoints.length; i++) {
            if (tagCompound.contains("outPoint" + i)) {
                int[] array = tagCompound.getIntArray("outPoint" + i);
                if (array.length != 3) continue;
                outPoints[i] = new Vec3i(array[0], array[1], array[2]);
            } else {
                outPoints[i] = null;
            }
        }
        for (int i = 0; i < outColors.length; i++) {
            if (tagCompound.contains("outColor" + i)) {
                byte[] array = tagCompound.getByteArray("outColor" + i);
                if (array.length != 4) {
                    outColors[i] = new byte[] { 0, 0, 0, 0 };
                    continue;
                }
                outColors[i] = array;
            } else {
                outColors[i] = new byte[] { 0, 0, 0, 0 };
            }
        }
    }

    public CompoundTag toNBT(CompoundTag tagCompound) {
        for (int i = 0; i < outPoints.length; i++) {
            if (outPoints[i] != null) {
                tagCompound.putIntArray(
                        "outPoint" + i,
                        new int[] {
                                outPoints[i].getX(),
                                outPoints[i].getY(),
                                outPoints[i].getZ()
                        });
            }
        }
        for (int i = 0; i < outColors.length; i++) {
            if (outColors[i] != null) {
                tagCompound.putByteArray(
                        "outColor" + i,
                        outColors[i]
                );
            }
        }

        return tagCompound;
    }

    @Override
    protected void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider registries) {
        super.loadAdditional(tagCompound, registries);
        fromNBT(tagCompound);
    }

    @Override
    protected void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider registries) {
        toNBT(tagCompound);
        super.saveAdditional(tagCompound, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return toNBT(new CompoundTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
