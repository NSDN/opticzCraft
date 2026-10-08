package cn.ac.nya.opticzcraft.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-side handler for {@link FlyPacket}: adopt the client's reported rotation and input.
 *
 * <p>Ported to Minecraft 1.21.1. 1.12.2 implemented {@code IMessageHandler} and read the sender from
 * {@code context.getServerHandler().player}; here the sender is {@code context.player()}, which is
 * already the {@link ServerPlayer} on the server side. {@code getServerWorld().addScheduledTask(...)}
 * becomes {@code context.enqueueWork(...)}.</p>
 *
 * <p>The assignments map field for field: {@code rotationYaw}/{@code rotationPitch} are now
 * {@code setYRot}/{@code setXRot}, and the movement inputs are the {@code xxa}/{@code zza} fields
 * ({@code moveStrafing}/{@code moveForward} in 1.12.2).</p>
 */
public class FlyPacketHandler {

    public static void handle(FlyPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            player.setYRot(packet.yaw);
            player.setXRot(packet.pitch);
            player.xxa = packet.strafe;
            player.zza = packet.forward;
        });
    }

}
