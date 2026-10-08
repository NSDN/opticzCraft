package cn.ac.nya.opticzcraft.api;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.thewdj.optics.ILightBeam;
import org.thewdj.optics.IOpticalDevice;

import java.util.List;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Ported to Minecraft 1.21.1 / NeoForge. The interface is pure logic (wavelength to colour, ray
 * march, laser burn), so almost nothing changed; the mechanical substitutions are:</p>
 * <ul>
 *   <li>{@code EnumFacing} to {@link Direction}, {@code getIndex()} to {@link Direction#get3DDataValue()},
 *       {@code EnumFacing.VALUES} to {@link Direction#values()}.</li>
 *   <li>{@code AxisAlignedBB} to {@link AABB}; 1.12.2's {@code Block.FULL_BLOCK_AABB} no longer exists,
 *       and {@code new AABB(BlockPos)} is the exact equivalent box for one block position.</li>
 *   <li>{@code world.getEntitiesWithinAABB(Entity.class, aabb)} to
 *       {@code world.getEntitiesOfClass(Entity.class, aabb)} (same bounding-box query, renamed).</li>
 *   <li>{@code state.getMaterial().getCanBurn()} to {@link BlockState#ignitedByLava()} and
 *       {@code state.getMaterial().blocksLight()} to {@link BlockState#canOcclude()}. {@code Material}
 *       does not exist in 1.21.1 at all (verified with javap: {@code net.minecraft.world.level.material.Material}
 *       is absent), so the two flags the old code actually read are used directly.</li>
 *   <li>{@code state.getMaterial().isReplaceable()} to {@link BlockState#canBeReplaced()},
 *       {@code entity.setFire(n)} to {@link Entity#igniteForSeconds(float)} (both are
 *       {@code n * 20} fire ticks), {@code world.setBlockState} to
 *       {@link Level#setBlockAndUpdate}.</li>
 *   <li>{@code MathHelper.floor} to {@link Mth#floor}.</li>
 * </ul>
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
                opticzCraft.LOGGER.error("Wave "+ this + " and "+ wave +" is NOT EQUAL!");
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

    int getInput(Direction facing);
    int getInput();
    boolean hasInput();
    void clearInputs();
    void clearOutputs();

    // Called from TileEntity
    void input(float wave, float power, float phase, Direction facing);

    static Vec3i transform(Level world, BlockPos pos, Direction facing) {
        for (int i = 1; i < LASER_MAX_DIST; i++) {
            BlockPos offset = pos.relative(facing, i);

            // 1.12.2: Block.FULL_BLOCK_AABB.offset(offset), then contract(+0.25) and contract(-0.25).
            // The two contractions cancel out, so this is literally the full block box at `offset`;
            // both calls are kept so the arithmetic stays comparable with the 1.12.2 source.
            AABB aabb = new AABB(offset);
            aabb = aabb.contract(0.25, 0.25, 0.25);
            aabb = aabb.contract(-0.25, -0.25, -0.25);
            List<Entity> list = world.getEntitiesOfClass(Entity.class, aabb);
            if (!list.isEmpty()) {
                return offset.subtract(pos);
            }

            BlockState state = world.getBlockState(offset);
            Block block = state.getBlock();

            if (block instanceof ILightBeam) continue;

            if (state.ignitedByLava()) {
                return offset.subtract(pos);
            }
            if (state.canOcclude()) {
                return offset.subtract(pos);
            }
        }
        return BlockPos.ZERO.relative(facing, LASER_MAX_DIST);
    }

    static void perform(Level world, BlockPos pos, Direction facing, float power) {
        for (int i = 1; i < LASER_MAX_DIST; i++) {
            BlockPos offset = pos.relative(facing, i);

            AABB aabb = new AABB(offset);
            aabb = aabb.contract(0.25, 0.25, 0.25);
            aabb = aabb.contract(-0.25, -0.25, -0.25);
            List<Entity> list = world.getEntitiesOfClass(Entity.class, aabb);
            if (!list.isEmpty()) {
                for (Entity entity : list)
                    entity.igniteForSeconds(Mth.floor(power / LASER_HURT_ENTITY_DIV));
                return;
            }

            BlockState state = world.getBlockState(offset);
            Block block = state.getBlock();

            if (block instanceof ILightBeam) continue;

            if (state.ignitedByLava()) {
                if (power >= LASER_HURT_BLOCK_GATE) {
                    offset = offset.relative(facing.getOpposite());
                    if (world.getBlockState(offset).canBeReplaced())
                        world.setBlockAndUpdate(offset, Blocks.FIRE.defaultBlockState());
                }
                return;
            }
            if (state.canOcclude()) {
                return;
            }
        }
    }

    // Called from Block
    void tick(Direction facing);
    // Called by Client
    Color4 colorOf(Direction facing);

}
