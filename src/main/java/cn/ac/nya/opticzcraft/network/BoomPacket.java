package cn.ac.nya.opticzcraft.network;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server to client particle request, sent by {@code util.BOOM} for its hand-rolled explosion.
 *
 * <p>Ported to Minecraft 1.21.1. The wire layout is the same four values the 1.12.2 {@code toBytes}
 * wrote (a string length + string, then three doubles); the transport changed completely:</p>
 * <ul>
 *   <li>{@code IMessage}/{@code toBytes}/{@code fromBytes} become
 *       {@link CustomPacketPayload} + {@link StreamCodec}. A hand-written codec is used rather than
 *       {@code StreamCodec.composite} on purpose: the 1.12.2 class exposes its first field as
 *       {@code public String type}, and {@code CustomPacketPayload} already declares a method named
 *       {@code type()}. A record component named {@code type} would collide with that method, so the
 *       plain class with its original field names is kept and the codec is spelled out.</li>
 *   <li>{@code send(SimpleNetworkWrapper, int dimension)} becomes
 *       {@link #sendTo(String, Vec3, Level)}: there is no channel object any more, and
 *       {@code PacketDistributor.sendToPlayersInDimension} needs the {@link ServerLevel} rather than
 *       the bare dimension id, so the level is derived at the call site (which is where the old id came
 *       from anyway). {@code wrapper == null} guarded the not-yet-initialised channel; that state
 *       cannot occur now, so the guard is replaced by the {@code ServerLevel} type test.</li>
 * </ul>
 */
public class BoomPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BoomPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(opticzCraft.MODID, "boom"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoomPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeUtf(packet.type);
                buffer.writeDouble(packet.x);
                buffer.writeDouble(packet.y);
                buffer.writeDouble(packet.z);
            },
            buffer -> new BoomPacket(buffer.readUtf(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));

    public String type;
    public double x, y, z;

    public BoomPacket() {
        type = "null";
        x = y = z = 0;
    }

    public BoomPacket(String type, double x, double y, double z) {
        this.type = type;
        this.x = x; this.y = y; this.z = z;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendTo(String type, Vec3 pos, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(
                    serverLevel, new BoomPacket(type, pos.x, pos.y, pos.z));
        }
    }

}
