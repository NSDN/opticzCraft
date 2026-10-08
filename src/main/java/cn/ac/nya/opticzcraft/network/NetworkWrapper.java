package cn.ac.nya.opticzcraft.network;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * opticzCraft's network channel.
 *
 * <p>Replaces the 1.12.2 {@code NetworkWrapper}, whose whole body was
 * {@code NetworkRegistry.INSTANCE.newSimpleChannel(MODID)} plus two
 * {@code registerMessage(...)} calls, kept in a {@code public static SimpleNetworkWrapper instance}
 * that every sender reached for ({@code NetworkWrapper.instance.sendToServer(packet)}).</p>
 *
 * <p>In NeoForge 1.21.1 there is no channel object at all: payloads carry their own
 * {@code CustomPacketPayload.Type} and are declared once through {@link RegisterPayloadHandlersEvent},
 * after which {@code PacketDistributor} sends them. Consequences for this class:</p>
 * <ul>
 *   <li>the {@code instance} field is gone -- {@link FlyPacket#sendToServer()} and
 *       {@link BoomPacket#sendTo(String, net.minecraft.world.phys.Vec3, net.minecraft.world.level.Level)}
 *       send directly;</li>
 *   <li>there is no {@code FMLPreInitializationEvent} constructor any more; registration is driven by
 *       {@link EventBusSubscriber} on the mod bus. No {@code bus = ...} argument is given, because
 *       {@code RegisterPayloadHandlersEvent} is an {@code IModBusEvent} and is routed by event type;</li>
 *   <li>the 1.12.2 discriminator ids (0 for the boom packet, 1 for the fly packet) are gone: payload
 *       types are keyed by their {@code ResourceLocation} ({@code opticzcraft:boom},
 *       {@code opticzcraft:fly}), so the ids are not carried over.</li>
 * </ul>
 *
 * <p>The two registrations keep the 1.12.2 directions exactly: the boom/particle packet is
 * {@code playToClient}, the booster movement report is {@code playToServer}.</p>
 */
@EventBusSubscriber(modid = opticzCraft.MODID)
public final class NetworkWrapper {

    /** Bump when a payload layout changes in an incompatible way. */
    public static final String PROTOCOL_VERSION = "3";

    private NetworkWrapper() {
    }

    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(BoomPacket.TYPE, BoomPacket.STREAM_CODEC, BoomHandler::handle);
        registrar.playToServer(FlyPacket.TYPE, FlyPacket.STREAM_CODEC, FlyPacketHandler::handle);
    }

}
