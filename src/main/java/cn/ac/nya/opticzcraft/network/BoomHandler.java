package cn.ac.nya.opticzcraft.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-side handler for {@link BoomPacket}: spawn one explosion particle.
 *
 * <p>Ported to Minecraft 1.21.1. The 1.12.2 class implemented
 * {@code IMessageHandler<BoomPacket, IMessage>} and the framework instantiated it; 1.21.1 handlers are
 * plain methods referenced from the registrar, so the class keeps its name and role but exposes a
 * {@code static handle} instead of {@code onMessage}.</p>
 *
 * <ul>
 *   <li>{@code MessageContext} becomes {@link IPayloadContext}; {@code Minecraft.addScheduledTask} --
 *       which hopped onto the client thread -- becomes {@code context.enqueueWork}. The payload is
 *       already dispatched on the main thread by default, so this is a deliberate no-op hop kept for
 *       parity with the old code.</li>
 *   <li>{@code EnumParticleTypes.getByName(name)} has no counterpart: 1.21.1 particles are registry
 *       entries with no name lookup. {@link #resolve(String)} reproduces the old behaviour -- it maps
 *       the one legacy name the pack actually sends ({@code hugeexplosion}, which 1.12.2 resolved to
 *       {@code EXPLOSION_HUGE}), falls back to a {@code minecraft:}-prefixed registry lookup, and ends
 *       on the same default the old code used.</li>
 *   <li>{@code mc.world.spawnParticle(type, x, y, z, 0, 0, 0)} becomes
 *       {@code level.addParticle(type, x, y, z, 0, 0, 0)}.</li>
 * </ul>
 *
 * <p>Client-class note: this class names {@link Minecraft} but is loaded on both sides, exactly like the
 * 1.12.2 original (which had no {@code @SideOnly} either). That is safe because it is registered with
 * {@code playToClient}, so a dedicated server never executes {@link #handle}, and the JVM only resolves
 * {@code Minecraft} at that point.</p>
 */
public class BoomHandler {

    public static void handle(BoomPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            level.addParticle(
                    resolve(packet.type), packet.x, packet.y, packet.z,
                    0, 0, 0);
        });
    }

    /** Legacy particle-name resolution; see the class notes. */
    private static ParticleOptions resolve(String name) {
        if ("hugeexplosion".equals(name)) {
            return ParticleTypes.EXPLOSION_EMITTER;
        }
        if (name != null && !name.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(name.contains(":") ? name : "minecraft:" + name);
            if (id != null) {
                ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(id);
                if (type instanceof ParticleOptions options) {
                    return options;
                }
            }
        }
        return ParticleTypes.EXPLOSION_EMITTER;
    }

}
