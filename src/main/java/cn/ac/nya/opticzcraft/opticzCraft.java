package cn.ac.nya.opticzcraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * opticzCraft entry point (Minecraft 1.21.1 / NeoForge).
 *
 * <p>Ported from the 1.12.2 shape that used {@code @Mod.Instance} + {@code @SidedProxy} plus
 * {@code @Mod.EventHandler} preInit/init/postInit/serverLoad. As everywhere else in v3:</p>
 * <ul>
 *   <li>{@code @SidedProxy} is gone. The 1.12.2 proxies only did three things -- start the tab, start
 *       the network wrapper, and register the event handlers -- and all three are now registration
 *       calls made directly from this constructor for the common side, with client-only work living in
 *       a {@code Dist.CLIENT} class. The {@code Dist} parameter is injected by FML and is used for the
 *       load log, exactly as {@code nyasamabuilding} does it.</li>
 *   <li>{@code @Mod.Instance}/{@code getInstance()} are gone: {@code EntityRegistry.registerModEntity}
 *       needed the mod instance, {@code EntityType} registration does not.</li>
 *   <li>{@code FMLServerStartingEvent} is gone. The {@code /boomCtl} command that used to be registered
 *       there is pending its Brigadier port together with {@code util.BOOM} (see the module report);
 *       it is not registered from here yet.</li>
 *   <li>{@code logger} (log4j, assigned from {@code event.getModLog()}) becomes a static
 *       {@link Logger} -- the same SLF4J facade the other v3 modules use.</li>
 * </ul>
 *
 * <p>Still pending (tracked in the module report, not silently dropped): {@code proxy.*} (the three
 * 1.12.2 proxies only did the wiring that now lives in this constructor, so they have no successor
 * class) and {@code event.EventRegister} (its five registrations became the {@code DeferredRegister}
 * tables, and {@code EventsHandler} moved to {@code @EventBusSubscriber}).</p>
 *
 * <p>{@code render.*} has since landed: the four 1.12.2 render classes became
 * {@code render/AbsOpticzTESR}, {@code render/LaserRenderer}, {@code render/ArrowRenderer} and
 * {@code render/ThrowRenderer}, and their client-side binding -- the 1.12.2
 * {@code ClientProxy} {@code ClientRegistry}/{@code RenderingRegistry} calls -- is
 * {@link OpticzRenderers}, a {@code Dist.CLIENT} {@code @EventBusSubscriber} that FML discovers by
 * annotation. Nothing has to be registered from this constructor for them.</p>
 */
@Mod(opticzCraft.MODID)
public class opticzCraft {

    public static final String PARENT = "nyasamaoptics";
    public static final String MODID = "opticzcraft";
    public static final String NAME = "opticzCraft";
    public static final String VERSION = "3.0.0";

    public static final Logger LOGGER = LoggerFactory.getLogger("opticzCraft");

    public opticzCraft(IEventBus modBus, ModContainer container, Dist dist) {
        OpticzBlocks.register(modBus);
        OpticzBlockEntities.register(modBus);
        OpticzItems.register(modBus);
        OpticzEntities.register(modBus);
        OpticzCreativeTab.register(modBus);

        LOGGER.info("[{}] loading on {} (NeoForge, MC 1.21.1)", MODID, dist);
    }

}
