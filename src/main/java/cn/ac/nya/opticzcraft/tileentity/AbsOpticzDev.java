package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public abstract class AbsOpticzDev extends TileEntity implements IOpticzDev {

    class Face extends LinkedHashMap<String, Wave> {

        public boolean hasWave(Wave wave) {
            return this.containsKey(wave.length());
        }

        public void put(Wave wave) {
            this.put(wave.length(), wave);
        }

    }

    protected LinkedHashMap<EnumFacing, Face> inputs;
    protected LinkedHashMap<EnumFacing, Face> outputs;

    protected Vec3i[] outPoints = new Vec3i[6];

    public void putOutpoint(EnumFacing facing, Vec3i vec) {
        outPoints[facing.getIndex()] = vec;
    }

    public Vec3i getOutpoint(EnumFacing facing) {
        return outPoints[facing.getIndex()];
    }

    public AbsOpticzDev() {
        super();

        inputs = new LinkedHashMap<>();
        for (EnumFacing facing : EnumFacing.VALUES)
            inputs.put(facing, new Face());
        outputs = new LinkedHashMap<>();
        for (EnumFacing facing : EnumFacing.VALUES)
            outputs.put(facing, new Face());
    }

    protected int[] inputVals = new int[] { 0, 0, 0, 0, 0, 0 };
    protected float powerLimit() {
        return LASER_DEFAULT_POWER;
    }

    public void calcInput(EnumFacing facing) {
        int result = 0;
        if (!inputs.get(facing).isEmpty()) {
            for (Wave wave : inputs.get(facing).values())
                result += (wave.power() / this.powerLimit() * 15);
        }
        if (result > 15) result = 15;
        inputVals[facing.getIndex()] = result;
    }

    @Override
    public float mWInput() {
        float result = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            for (Wave wave : inputs.get(facing).values())
                result += wave.power();
        }
        return result;
    }

    @Override
    public float mWOutput() {
        float result = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            for (Wave wave : outputs.get(facing).values())
                result += wave.power();
        }
        return result;
    }

    public float getOutput(EnumFacing facing) {
        float result = 0;
        for (Wave wave : outputs.get(facing).values())
            result += wave.power();
        return result;
    }

    public float getOutput() {
        float result = 0;
        for (EnumFacing facing : EnumFacing.VALUES)
            result += getOutput(facing);
        return result;
    }

    @Override
    public int getInput(EnumFacing facing) {
        return inputVals[facing.getIndex()];
    }

    @Override
    public int getInput() {
        int result = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
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
        for (EnumFacing facing : EnumFacing.VALUES)
            inputs.get(facing).clear();
    }

    @Override
    public void clearOutputs() {
        for (EnumFacing facing : EnumFacing.VALUES)
            outputs.get(facing).clear();
        for (int i = 0; i < outPoints.length; i++)
            outPoints[i] = null;
    }

    public Face getInputFace(EnumFacing facing) { return inputs.get(facing); }

    public Face getOutputFace(EnumFacing facing) { return outputs.get(facing); }

    public void pushInput(EnumFacing facing, float waveLength, float power, float phase) {
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

    public void pushOutput(EnumFacing facing, float waveLength, float power, float phase) {
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
        outColors[facing.getIndex()] = color.toBytes();
    }

    protected byte[][] outColors = new byte[6][];

    protected Color4 calculateColor(EnumFacing facing) {
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

    @SideOnly(Side.CLIENT)
    @Override
    public Color4 colorOf(EnumFacing facing) {
        return Color4.fromBytes(outColors[facing.getIndex()]);
    }

    public void fromNBT(NBTTagCompound tagCompound) {
        for (int i = 0; i < outPoints.length; i++) {
            if (tagCompound.hasKey("outPoint" + i)) {
                int[] array = tagCompound.getIntArray("outPoint" + i);
                if (array.length != 3) continue;
                outPoints[i] = new Vec3i(array[0], array[1], array[2]);
            } else {
                outPoints[i] = null;
            }
        }
        for (int i = 0; i < outColors.length; i++) {
            if (tagCompound.hasKey("outColor" + i)) {
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

    public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
        for (int i = 0; i < outPoints.length; i++) {
            if (outPoints[i] != null) {
                tagCompound.setIntArray(
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
                tagCompound.setByteArray(
                        "outColor" + i,
                        outColors[i]
                );
            }
        }

        return tagCompound;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 65535.0;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        fromNBT(tagCompound);
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        toNBT(tagCompound);
        return super.writeToNBT(tagCompound);
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound = writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound = writeToNBT(tagCompound);
        return new SPacketUpdateTileEntity(getPos(), 1, tagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
        NBTTagCompound tagCompound = packet.getNbtCompound();
        readFromNBT(tagCompound);
    }

}
