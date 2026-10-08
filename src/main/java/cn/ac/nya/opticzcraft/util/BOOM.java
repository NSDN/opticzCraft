package cn.ac.nya.opticzcraft.util;

import cn.ac.nya.opticzcraft.network.BoomPacket;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * The hand-rolled "vacuum explosion" used by the cluster missile and the pressure bomb, plus its
 * toggle command.
 *
 * <p>Ported to Minecraft 1.21.1. The physics and the eight-particle ring are carried over verbatim;
 * the mechanical substitutions are:</p>
 * <ul>
 *   <li>{@code Vec3d} becomes {@link Vec3} ({@code rotateYaw} is {@link Vec3#yRot}, still in radians,
 *       so the literal {@code 45} is kept exactly as it was), and {@code MathHelper} becomes
 *       {@link Mth}.</li>
 *   <li>{@code world.createExplosion(null, x, y, z, radius, canHurt(world))} becomes
 *       {@code level.explode(null, x, y, z, radius, canHurt(world), ExplosionInteraction.TNT)}. Note
 *       the boolean that the 1.12.2 call passed was the explosion's <i>flamming</i> flag -- the same
 *       {@code canHurt} switch is reused here for the flamming flag and again below for the potion
 *       effect, exactly as before. {@code TNT} is the closest modern interaction mode: the old default
 *       destroyed blocks and dropped some of them.</li>
 *   <li>{@code world.getEntitiesWithinAABBExcludingEntity(null, aabb)} becomes
 *       {@code world.getEntities((Entity) null, aabb)} -- the same query with a null filter, which in
 *       1.21.1 means "every entity in the box".</li>
 *   <li>{@code entity.isImmuneToExplosions()} does not exist in 1.21.1 (verified with javap; the
 *       closest replacement is the instance-based {@code Entity#ignoreExplosion(Explosion)}, which
 *       needs an {@code Explosion} object the manual path never builds). The guard is therefore
 *       expressed as {@code !entity.isInvulnerableTo(entity.damageSources().explosion(null, null))},
 *       which preserves the intent for the mod's own projectiles -- they override
 *       {@code ignoreExplosion} and are invulnerable to that damage source.</li>
 *   <li>{@code entity.motionX += ...} becomes {@link Entity#push(double, double, double)};
 *       {@code MathHelper.sqrt} becomes {@code Math.sqrt}; {@code entity.posX} becomes
 *       {@code getX()}.</li>
 *   <li>{@code SoundEvents.ENTITY_GENERIC_EXPLODE} is {@code SoundEvents.GENERIC_EXPLODE} (a
 *       {@code Holder<SoundEvent>}, accepted directly by the modern {@code playSound} overload);
 *       {@code world.rand} becomes {@code world.getRandom()};
 *       {@code BoomPacket.sendTo(name, pos, NetworkWrapper.instance, dim)} becomes
 *       {@code BoomPacket.sendTo(name, pos, level)}.</li>
 *   <li><b>Potions:</b> 1.12.2 looked up {@code PotionType.getPotionTypeForName("long_weakness")} and
 *       {@code ("strong_harming")} and called {@code Potion#affectEntity} on each of their effects.
 *       That method forwarded to {@code Potion#performEffect}, which is empty for non-instant potions
 *       -- so the weakness half was a no-op in 1.12.2 and is not reproduced here; the observable
 *       behaviour was the harming half: {@code attackEntityFrom(DamageSource.magic, 6 << amplifier)}.
 *       {@code MobEffectInstance(MobEffects.HARM, 1, 1)} produces exactly that in 1.21.1 (verified in
 *       {@code HealOrHarmMobEffect#applyEffectTick}: {@code hurt(magic(), 6 << amplifier)}), so the
 *       port is behaviourally identical. Weakness would be a gameplay change, so it is deliberately
 *       left out rather than silently added.</li>
 *   <li><b>Command:</b> {@code CtrlCmd extends CommandBase} becomes a Brigadier registration helper
 *       ({@link CtrlCmd#register(CommandDispatcher)}). {@code checkPermission} keeps its shape:
 *       op-listed on a dedicated server, creative in singleplayer, and console allowed. One behavioural
 *       difference: the old code swallowed unparsable arguments as 0, whereas Brigadier rejects them as
 *       an invalid argument -- that is the standard modern behaviour and is called out here rather than
 *       emulated.</li>
 * </ul>
 */
public class BOOM {

    private static final LinkedHashMap<Level, Boolean> canHurt = new LinkedHashMap<>();

    private static boolean canHurt(Level world) {
        if (!canHurt.containsKey(world))
            canHurt.put(world, false);
        return canHurt.get(world);
    }

    private static final LinkedHashMap<Level, Boolean> canBoom = new LinkedHashMap<>();

    private static boolean canBoom(Level world) {
        if (!canBoom.containsKey(world))
            canBoom.put(world, false);
        return canBoom.get(world);
    }

    /** 1.12.2 {@code boomCtl <0/1> [<0/1>]}: toggles the vacuum explosion and its damage. */
    public static class CtrlCmd {

        public static final String NAME = "boomCtl";

        /** Kept for parity with the 1.12.2 {@code getAliases()} implementation. */
        public static List<String> aliases() {
            return Lists.newArrayList("boomCtl");
        }

        /** 1.12.2 {@code getUsage}. */
        public static String usage() {
            return "boomCtl <0/1>";
        }

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(Commands.literal(NAME)
                    .requires(CtrlCmd::checkPermission)
                    .executes(context -> run(context, null, null))
                    .then(Commands.argument("boom", IntegerArgumentType.integer(0, 1))
                            .executes(context -> run(context,
                                    IntegerArgumentType.getInteger(context, "boom"), null))
                            .then(Commands.argument("hurt", IntegerArgumentType.integer(0, 1))
                                    .executes(context -> run(context,
                                            IntegerArgumentType.getInteger(context, "boom"),
                                            IntegerArgumentType.getInteger(context, "hurt"))))));
        }

        private static int run(CommandContext<CommandSourceStack> context, Integer boom, Integer hurt) {
            CommandSourceStack source = context.getSource();

            if (!(source.getEntity() instanceof Player)) {
                source.sendFailure(Component.literal(
                        net.minecraft.ChatFormatting.RED + "[ERROR] This command can only be executed by player!"));
                return 0;
            }

            Level world = source.getLevel();

            if (boom == null) {
                source.sendSuccess(() -> Component.literal(
                        net.minecraft.ChatFormatting.GRAY + "boomCtl: " + (canBoom(world) ? "1" : "0")), false);
                source.sendSuccess(() -> Component.literal(
                        net.minecraft.ChatFormatting.GRAY + "hurtCtl: " + (canHurt(world) ? "1" : "0")), false);
                return 1;
            }

            canBoom.put(world, boom > 0);
            source.sendSuccess(() -> Component.literal(
                    net.minecraft.ChatFormatting.GRAY + "boomCtl: " + (canBoom(world) ? "1" : "0")), false);

            if (hurt != null) {
                canHurt.put(world, hurt > 0);
                source.sendSuccess(() -> Component.literal(
                        net.minecraft.ChatFormatting.GRAY + "hurtCtl: " + (canHurt(world) ? "1" : "0")), false);
            }

            return 1;
        }

        /**
         * 1.12.2 {@code checkPermission}: dedicated server asks the op list, singleplayer asks for
         * creative, and the console is always allowed.
         */
        private static boolean checkPermission(CommandSourceStack source) {
            if (source.getEntity() instanceof Player player) {
                if (source.getServer().isDedicatedServer()) {
                    return source.getServer().getPlayerList().isOp(player.getGameProfile());
                } else {
                    return player.isCreative();
                }
            }

            return source.getEntity() == null;
        }

    }

    public static Vec3 vVec(Vec3 vecIn) {
        double x = vecIn.x, y = vecIn.y, z = vecIn.z;
        Vec3 res = Vec3.ZERO;
        if (x != 0) {
            res = new Vec3(-(y + z) / x, 1, 1);
        } else if (y != 0) {
            res = new Vec3(1, -(x + z) / y, 1);
        } else if (z != 0) {
            res = new Vec3(1, 1, -(x + y) / z);
        }
        return res.normalize();
    }

    public static Vec3 rotate(Vec3 source, Vec3 axis, float angle) {
        Vec3 nor = axis.normalize();
        double x = nor.x, y = nor.y, z = nor.z;
        float sin = Mth.sin(angle), cos = Mth.cos(angle);

        double[][] mat = new double[][] {
                { cos + x * x * (1 - cos), x * y * (1 - cos) + z * sin, x * z * (1 - cos) - y * sin },
                { x * y * (1 - cos) - z * sin, cos + y * y * (1 - cos), z * y * (1 - cos) + x * sin },
                { x * z * (1 - cos) + y * sin, y * z * (1 - cos) - x * sin, cos + z * z * (1 - cos) }
        };
        x = mat[0][0] * source.x + mat[0][1] * source.y + mat[0][2] * source.z;
        y = mat[1][0] * source.x + mat[1][1] * source.y + mat[1][2] * source.z;
        z = mat[2][0] * source.x + mat[2][1] * source.y + mat[2][2] * source.z;

        return new Vec3(x, y, z);
    }

    public static void $(Level world, double x, double y, double z, float radius) {
        if (canBoom(world)) {
            world.explode(null, x, y, z, radius, canHurt(world), Level.ExplosionInteraction.TNT);
            return;
        }

        float d = radius * 2.0F;
        int x1 = Mth.floor(x - (double)d - 1.0D);
        int x2 = Mth.floor(x + (double)d + 1.0D);
        int y1 = Mth.floor(y - (double)d - 1.0D);
        int y2 = Mth.floor(y + (double)d + 1.0D);
        int z1 = Mth.floor(z - (double)d - 1.0D);
        int z2 = Mth.floor(z + (double)d + 1.0D);
        List<Entity> list = world.getEntities(
                (Entity) null,
                new AABB((double)x1, (double)y1, (double)z1, (double)x2, (double)y2, (double)z2)
        );

        String particleName = "hugeexplosion";
        Vec3 pos = new Vec3(x, y, z);
        BoomPacket.sendTo(particleName, pos, world);
        Vec3 vec = new Vec3(radius / 2, 0, 0);
        for (int i = 0; i < 8; i++) {
            Vec3 tmp = pos.add(vec);
            BoomPacket.sendTo(particleName, tmp, world);
            vec = vec.yRot(45);
        }
        world.playSound(null, x, y, z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.AMBIENT,
                4.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);

        for (Entity entity : list) {
            if (!entity.isInvulnerableTo(entity.damageSources().explosion(null, null))) {
                double v = Math.sqrt(entity.distanceToSqr(x, y, z)) / (double)d;
                if (v <= 1.0D) {
                    double dx = entity.getX() - x;
                    double dy = entity.getY() + (double)entity.getEyeHeight() - y;
                    double dz = entity.getZ() - z;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist != 0.0D) {
                        dx /= dist;
                        dy /= dist;
                        dz /= dist;
                        double force = (1.0D - v) * radius;

                        entity.push(dx * force, dy * force, dz * force);
                    }
                }
            }

            if (entity instanceof LivingEntity living && canHurt(world)) {
                float health = living.getHealth();
                if (Float.isInfinite(health) || Float.isNaN(health))
                    health = 32.0F;
                living.setHealth(health / 2.0F);
                // 1.12.2 "strong_harming": the instant half is the only observable one (see the class
                // notes); amplifier 1 reproduces "strong" and yields 6 << 1 damage.
                living.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
            }
        }
    }

}
