package cn.ac.nya.opticzcraft.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Elytra-flight helpers for the opticzCraft boosters.
 *
 * <p>Ported to Minecraft 1.21.1. The semantics are unchanged -- the boosters only work while the
 * player is fall-flying, and {@link #control} steers by blending the current velocity towards (or away
 * from) the look direction -- but every accessor moved:</p>
 * <ul>
 *   <li>{@code EntityPlayerMP.isElytraFlying()/setElytraFlying()/clearElytraFlying()} became
 *       {@link Player#isFallFlying()}/{@link Player#startFallFlying()}/{@link Player#stopFallFlying()}
 *       and moved from the server player up to {@link Player}. The {@code instanceof ServerPlayer}
 *       guard is kept on purpose: 1.12.2 deliberately only mutated the server-side instance, and the
 *       flag is synchronised to the client, so flipping it client-side would be wrong.</li>
 *   <li>{@code player.motionX/Y/Z} became {@link Player#getDeltaMovement()} plus
 *       {@link Player#setDeltaMovement(double, double, double)} -- the velocity is no longer a mutable
 *       field.</li>
 *   <li>{@code player.getLookVec()} became {@link Player#getLookAngle()}, and
 *       {@code Vec3d.rotateYaw(float)} became {@link Vec3#yRot(float)}; both take radians, so the
 *       {@code moveStrafing * 5.0F} argument is carried over untouched. {@code moveStrafing} itself is
 *       now {@code player.xxa}.</li>
 * </ul>
 */
public class FLY {

    public static boolean setFly(Player player, boolean state) {
        if (player instanceof ServerPlayer mp) {

            if (state)
                mp.startFallFlying();
            else
                mp.stopFallFlying();
            return true;
        }
        return false;
    }

    public static boolean canFly(Player player) {
        return player.isFallFlying();
    }

    public static void control(Player player, float throttle) {
        Vec3 vec = player.getLookAngle();
        vec = vec.yRot(player.xxa * 5.0F);

        if (throttle > 0) {
            double v = 240 / 3.6 / 20 * throttle; // V = 0.5V + 0.5vL
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(
                    motion.x + (vec.x * v - motion.x) * 0.5D,
                    motion.y + (vec.y * v - motion.y) * 0.5D,
                    motion.z + (vec.z * v - motion.z) * 0.5D);
        } else if (throttle < 0) {
            double u = 0.1 * throttle; // V = V - uL
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(
                    motion.x + vec.x * u,
                    motion.y + vec.y * u,
                    motion.z + vec.z * u);
        }
    }

}
