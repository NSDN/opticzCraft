package cn.ac.nya.opticzcraft.network;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client to server movement report for the boosters (the client player's own rotation and input).
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code IMessage} becomes {@link CustomPacketPayload} plus a hand-written {@link StreamCodec}
 *       that writes the same four floats (yaw, pitch, strafe, forward) the old {@code toBytes} did.</li>
 *   <li>{@code EntityPlayerSP.movementInput.updatePlayerMoveState()} and
 *       {@code EntityPlayerSP.updateEntityActionState()} have no counterpart: 1.21.1 keeps the local
 *       player's input fields ({@code xxa}/{@code zza}) up to date every tick already, so
 *       {@link #fromPlayer(Player)} only reads them. {@code rotationYaw}/{@code rotationPitch} become
 *       {@code getYRot()}/{@code getXRot()}.</li>
 *   <li>{@code @SideOnly(Side.CLIENT)} is not available in 1.21.1 (and is banned by the port rules), so
 *       the client-only half is not annotated at all. This is safe by the same reasoning the 1.12.2 code
 *       relied on: {@link #fromPlayer(Player)} is only ever called from the client-side branch of the
 *       booster, and the JVM resolves {@link LocalPlayer} at that instruction, not when the class is
 *       loaded.</li>
 *   <li>{@code NetworkWrapper.instance.sendToServer(packet)} becomes {@link #sendToServer()}, which uses
 *       {@code PacketDistributor} directly -- there is no channel object to hold.</li>
 * </ul>
 */
public class FlyPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FlyPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(opticzCraft.MODID, "fly"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlyPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeFloat(packet.yaw);
                buffer.writeFloat(packet.pitch);
                buffer.writeFloat(packet.strafe);
                buffer.writeFloat(packet.forward);
            },
            buffer -> new FlyPacket(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));

    public float yaw, pitch;
    public float strafe, forward;

    public FlyPacket() {
        yaw = 0; pitch = 0;
        strafe = 0; forward = 0;
    }

    public FlyPacket(float yaw, float pitch, float strafe, float forward) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.strafe = strafe;
        this.forward = forward;
    }

    public static FlyPacket fromPlayer(Player player) {
        if (player instanceof LocalPlayer sp) {
            return new FlyPacket(sp.getYRot(), sp.getXRot(), sp.xxa, sp.zza);
        }
        return null;
    }

    public void sendToServer() {
        PacketDistributor.sendToServer(this);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
