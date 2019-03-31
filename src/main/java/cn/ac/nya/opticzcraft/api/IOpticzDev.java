package cn.ac.nya.opticzcraft.api;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.thewdj.optics.IOpticalDevice;

import java.util.List;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public interface IOpticzDev extends IOpticalDevice {

    float WAVE_BEGIN = 380.0F;
    float WAVE_END = 780.0F;
    float HUE_BEGIN = 0.0F;
    float HUE_END = 300.0F;

    float MAX_POWER = (float) (1e12 * 1e3); // 1TW, 1PmW

    int LASER_MAX_DIST = 256;
    float LASER_DEFAULT_POWER = 500.0F;

    float LASER_HURT_ENTITY_DIV = 500.0F;
    float LASER_HURT_BLOCK_GATE = 1000.0F;

    float $2PI = (float) Math.PI * 2;
    float $PI4 = (float) Math.PI / 4;

    class Vec2 {

        public float x, y;

        public Vec2(float x, float y) {
            this.x = x; this.y = y;
        }

        public Vec2(double x, double y) {
            this.x = (float) x; this.y = (float) y;
        }

        public float angle() {
            return (float) Math.atan2(y, x);
        }

        public float length() {
            return (float) Math.sqrt(x * x + y * y);
        }

        public Vec2 add(Vec2 vec) {
            return new Vec2(
                    this.x + vec.x,
                    this.y + vec.y
            );
        }

        public Vec2 rotate(float angle) {
            return new Vec2(
                    x * Math.cos(angle) - y * Math.sin(angle),
                    x * Math.sin(angle) + y * Math.cos(angle)
            );
        }

        public static Vec2 fromLenAngle(float len, float angle) {
            return new Vec2(len, 0).rotate(angle);
        }

    }

    class Wave {

        float w, a, p;

        public String length() { return String.format("%1.1f", w); }
        public float waveLen() { return w; }
        public float power() { return a; }
        public float phase() { return p; }

        public Wave(float waveLength, float power, float phase) {
            w = waveLength; a = power; p = phase;
        }

        @Override
        public String toString() {
            return String.format(
                    "[len: %1.1f nm, pwr: %1.1f, pha: %1.1f pi]",
                    w, power(),
                    phase() / Math.PI
            );
        }

        public Wave sum(Wave wave) {
            if (this.w != wave.w) {
                opticzCraft.logger.error("Wave "+ this + " and "+ wave +" is NOT EQUAL!");
            }

            Vec2 src = Vec2.fromLenAngle(this.a, this.p);
            Vec2 dst = Vec2.fromLenAngle(wave.a, wave.p);
            dst = dst.add(src);

            float length = dst.length();
            if (length > MAX_POWER)
                length = MAX_POWER;

            return new Wave(this.w, length, dst.angle());
        }

    }

    class Color4 {

        public float r, g, b, a;

        public static Color4 TRANS;

        static {
            TRANS = new Color4();
            TRANS.r = 0; TRANS.g = 0; TRANS.b = 0; TRANS.a = 0;
        }

        private static float parse(byte[] bytes, int index) {
            if (index < bytes.length)
                return (bytes[index] & 0xFF) / 255.0F;
            return 0.0F;
        }

        private static byte parse(float value) {
            if (value < 0) value = 0;
            if (value > 1) value = 1;
            return (byte) (255 * value);
        }

        public static Color4 fromBytes(byte[] bytes) {
            Color4 result = new Color4();
            result.r = parse(bytes, 0);
            result.g = parse(bytes, 1);
            result.b = parse(bytes, 2);
            result.a = parse(bytes, 3);
            return result;
        }

        public static byte[] toBytes(Color4 color) {
            return new byte[] {
                parse(color.r), parse(color.g),
                parse(color.b), parse(color.a)
            };
        }

        public byte[] toBytes() {
            return toBytes(this);
        }

        @Override
        public String toString() {
            return String.format(
                    "(%1.1f, %1.1f, %1.1f, %1.1f)",
                    r, g ,b, a
            );
        }

    }

    static Color4 getColor(float waveLength, float power) {
        float h = (waveLength - WAVE_BEGIN) / (WAVE_END - WAVE_BEGIN) * (HUE_END - HUE_BEGIN) + HUE_BEGIN;
        float s = 1.0F;
        float v = 1.0F;
        if (power < 0) power = 0.0F;
        if (power > 1) power = 1.0F;

        h = h % 360;
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = v - c;
        float rt, gt, bt;
        if (h >= 0 && h < 60) {
            rt = c; gt = x; bt = 0;
        } else if (h >= 60 && h < 120) {
            rt = x; gt = c; bt = 0;
        } else if (h >= 120 && h < 180) {
            rt = 0; gt = c; bt = x;
        } else if (h >= 180 && h < 240) {
            rt = 0; gt = x; bt = c;
        } else if (h >= 240 && h < 300) {
            rt = x; gt = 0; bt = c;
        } else if (h >= 300 && h < 360) {
            rt = c; gt = 0; bt = x;
        } else {
            rt = gt = bt = 0;
        }
        Color4 color = new Color4();
        color.r = rt + m;
        color.g = gt + m;
        color.b = bt + m;
        color.a = power;

        if (waveLength > WAVE_END || waveLength < WAVE_BEGIN)
            color.r = color.g = color.b = color.a = 0.0F;

        return color;
    }

    int getInput(EnumFacing facing);
    int getInput();
    boolean hasInput();
    void clearInputs();
    void clearOutputs();

    // Called from TileEntity
    void input(float wave, float power, float phase, EnumFacing facing);

    static Vec3i transform(World world, BlockPos pos, EnumFacing facing) {
        for (int i = 1; i < LASER_MAX_DIST; i++) {
            BlockPos offset = pos.offset(facing, i);

            AxisAlignedBB aabb = Block.FULL_BLOCK_AABB.offset(offset);
            aabb = aabb.contract(0.25, 0.25, 0.25);
            aabb = aabb.contract(-0.25, -0.25, -0.25);
            List<Entity> list = world.getEntitiesWithinAABB(Entity.class, aabb);
            if (!list.isEmpty()) {
                return offset.subtract(pos);
            }

            IBlockState state = world.getBlockState(offset);
            if (state.getMaterial().getCanBurn()) {
                return offset.subtract(pos);
            }
            if (!state.isTranslucent()) {
                return offset.subtract(pos);
            }
        }
        return BlockPos.ORIGIN.offset(facing, LASER_MAX_DIST);
    }

    static void perform(World world, BlockPos pos, EnumFacing facing, float power) {
        for (int i = 1; i < LASER_MAX_DIST; i++) {
            BlockPos offset = pos.offset(facing, i);

            AxisAlignedBB aabb = Block.FULL_BLOCK_AABB.offset(offset);
            aabb = aabb.contract(0.25, 0.25, 0.25);
            aabb = aabb.contract(-0.25, -0.25, -0.25);
            List<Entity> list = world.getEntitiesWithinAABB(Entity.class, aabb);
            if (!list.isEmpty()) {
                for (Entity entity : list)
                    entity.setFire(MathHelper.floor(power / LASER_HURT_ENTITY_DIV));
                return;
            }

            IBlockState state = world.getBlockState(offset);
            if (state.getMaterial().getCanBurn()) {
                if (power >= LASER_HURT_BLOCK_GATE) {
                    offset = offset.offset(facing.getOpposite());
                    if (world.getBlockState(offset).getMaterial().isReplaceable())
                        world.setBlockState(offset, Blocks.FIRE.getDefaultState());
                }
                return;
            }
            if (!state.isTranslucent()) {
                return;
            }
        }
    }

    // Called from Block
    void tick(EnumFacing facing);
    // Called by Client
    Color4 colorOf(EnumFacing facing);

}
