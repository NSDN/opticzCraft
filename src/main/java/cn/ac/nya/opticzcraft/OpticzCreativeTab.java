package cn.ac.nya.opticzcraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The opticzCraft creative tab.
 *
 * <p>Replaces the 1.12.2 {@code tab.TabLoader}. That class built an anonymous
 * {@code CreativeTabs("opticzCraft")} in preInit and, when {@code nyasamaoptics} was present, replaced
 * it via reflection with {@code club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader.tabNyaSamaOptics}
 * so that both mods shared one tab. Two things make that whole mechanism obsolete:</p>
 * <ul>
 *   <li>1.21.1 creative tabs are registry entries built by {@link CreativeModeTab#builder()}; the tab
 *       title is a {@link Component}, and the translation key is kept identical to the 1.12.2 lang
 *       entry ({@code itemGroup.opticzCraft}).</li>
 *   <li>the reflection target does not exist in v3 (nyasamaoptics is itself mid-port), and cross-mod
 *       tab sharing is better expressed by NeoForge's {@code BuildCreativeModeTabContentsEvent} than
 *       by reaching into another mod's class. opticzcraft therefore always registers its own tab.</li>
 * </ul>
 *
 * <p>Membership: 1.12.2 put every device block into the tab through
 * {@code setCreativeTab(TabLoader.opticzCraft)} and explicitly kept the standalone items out of it with
 * {@code setCreativeTab(null)}. The {@code displayItems} generator below reproduces exactly that --
 * the seven block items, and the logo used as the icon only.</p>
 */
public final class OpticzCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, opticzCraft.MODID);

    private OpticzCreativeTab() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OPTICZCRAFT =
            TABS.register("opticzcraft", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.opticzCraft"))
                    .icon(() -> OpticzItems.LOGO.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(OpticzBlocks.LASER_JET.get());
                        output.accept(OpticzBlocks.LASER_DET.get());
                        output.accept(OpticzBlocks.LASER_PBS.get());
                        output.accept(OpticzBlocks.LASER_REC.get());
                        output.accept(OpticzBlocks.LASER_REF.get());
                        output.accept(OpticzBlocks.LASER_PBS_SOFT.get());
                        output.accept(OpticzBlocks.LASER_REF_SOFT.get());
                    })
                    .build());
}
