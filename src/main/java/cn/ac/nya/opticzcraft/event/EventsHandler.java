package cn.ac.nya.opticzcraft.event;

import cn.ac.nya.opticzcraft.opticzCraft;
import cn.ac.nya.opticzcraft.util.BOOM;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * opticzCraft's game-bus handlers.
 *
 * <p>Ported to Minecraft 1.21.1. 1.12.2 split this over {@code event.EventRegister} (which only did
 * {@code MinecraftForge.EVENT_BUS.register(...)} for five singletons) and this class; the registration
 * half is replaced by {@link EventBusSubscriber} here, so {@code EventRegister} has no successor and its
 * contents are accounted for one by one in the module report:</p>
 * <ul>
 *   <li>{@code BlockLoader}/{@code ItemLoader}/{@code TileEntityLoader}/{@code EntityLoader} were
 *       registry-event handlers -- replaced by the {@code DeferredRegister} tables
 *       ({@code OpticzBlocks}, {@code OpticzItems}, {@code OpticzBlockEntities}, {@code OpticzEntities}).</li>
 *   <li>{@code TileEntityModelBinder}/{@code EntityModelBinder} were client model-registry handlers --
 *       to be replaced by {@code EntityRenderersEvent.RegisterRenderers} when the renderers are
 *       ported.</li>
 *   <li>{@code IFlyEquipment.UpdatePlayer} and {@code EventsHandler} carry their own
 *       {@code @EventBusSubscriber} annotations now.</li>
 * </ul>
 *
 * <p>Changes in this class:</p>
 * <ul>
 *   <li>{@code Loader.isModLoaded(...)} becomes {@code ModList.get().isLoaded(...)}.</li>
 *   <li>{@code TextComponentString} becomes {@link Component#literal};
 *       {@code TextFormatting} becomes {@code ChatFormatting}; {@code event.player} becomes
 *       {@code event.getEntity()} and {@code sendMessage} becomes {@code sendSystemMessage}.</li>
 *   <li>{@code onEntityInteract} is <b>not</b> ported: the 1.12.2 body was empty. The equally empty
 *       {@code PlayerInteractEvent.EntityInteract} subscription disappears with it.</li>
 *   <li>The {@code /boomCtl} command that the 1.12.2 entry point registered from
 *       {@code FMLServerStartingEvent} is registered here instead: {@code FMLServerStartingEvent} no
 *       longer exists and {@link RegisterCommandsEvent} is the modern hook (and, being a game-bus
 *       event, it belongs with the other runtime handlers rather than in the mod constructor).</li>
 * </ul>
 */
@EventBusSubscriber(modid = opticzCraft.MODID)
public class EventsHandler {

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModList.get().isLoaded(opticzCraft.PARENT)) {
            event.getEntity().sendSystemMessage(Component.literal(
                    net.minecraft.ChatFormatting.AQUA + "Thanks for using opticzCraft!"
            ));
        }
    }

    /** 1.12.2 {@code opticzCraft.serverLoad}: {@code event.registerServerCommand(new BOOM.CtrlCmd())}. */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        BOOM.CtrlCmd.register(event.getDispatcher());
    }

}
