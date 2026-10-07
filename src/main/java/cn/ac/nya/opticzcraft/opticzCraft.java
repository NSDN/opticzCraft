package cn.ac.nya.opticzcraft;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * v3 skeleton entry point (Minecraft 1.21.1 / NeoForge).
 *
 * <p>This class only proves that the toolchain, mod metadata and dependency graph work.
 * The real 1.12.2 implementation lives in the corresponding module of
 * {@code NyaSamaCompletePack-v2} and will be ported into this package tree.</p>
 */
@Mod(opticzCraft.MODID)
public class opticzCraft {

    public static final String MODID = "opticzcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger("opticzCraft");

    public opticzCraft(IEventBus modBus, ModContainer container) {
        LOGGER.info("[{}] v3 skeleton loaded (NeoForge, MC 1.21.1)", MODID);
    }
}