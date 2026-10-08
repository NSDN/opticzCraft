package cn.ac.nya.opticzcraft;

import cn.ac.nya.opticzcraft.item.ItemBooster;
import cn.ac.nya.opticzcraft.item.ItemBurner;
import cn.ac.nya.opticzcraft.item.ItemLogo;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration for {@code opticzcraft} standalone items.
 *
 * <p>Replaces the 1.12.2 {@code item.ItemLoader}, which built a {@code LinkedList<Item>} and pushed it
 * through {@code RegistryEvent.Register<Item>} plus a client-only {@code ModelRegistryEvent} handler
 * for the item icons. Both hooks are gone; the table below is the whole replacement.</p>
 *
 * <p>1.12.2 registered five items: {@code logo}, {@code throw}, {@code arrow}, {@code booster} and
 * {@code burner}. {@code ItemLoader} also kept static references to the logo, the throw item and the
 * arrow because other classes asked {@code ItemLoader} for them at runtime (the throwable entity draws
 * itself with {@code ItemLoader.itemThrow}, the cluster missile returns {@code ItemLoader.itemArrow}
 * as its pickup stack). Those references become {@link #LOGO}, {@link #THROW} and {@link #ARROW} --
 * the same lazy {@code DeferredItem} holders, so the call sites keep their shape.</p>
 *
 * <p>Creative-tab note: 1.12.2 called {@code setCreativeTab(null)} on the throw/arrow/booster/burner
 * items and left the logo on the default tab. That flag is gone in 1.21.1 -- an item appears in a tab
 * only if the tab's {@code displayItems} generator emits it -- and the 1.12.2 behaviour is reproduced
 * by {@link OpticzCreativeTab} listing the block items only.</p>
 */
public final class OpticzItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(opticzCraft.MODID);

    private OpticzItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    /** 1.12.2 {@code ItemLoader.logo}: stack size 1, and the opticzCraft creative tab's icon. */
    public static final DeferredItem<ItemLogo> LOGO =
            ITEMS.registerItem("logo", ItemLogo::new, new Item.Properties().stacksTo(1));

    /**
     * 1.12.2 {@code ItemLoader}: {@code new ItemBooster()} with
     * {@code setMaxDamage(toolMaterial.getMaxUses())} -- iron's 250 uses.
     */
    public static final DeferredItem<ItemBooster> BOOSTER =
            ITEMS.registerItem("booster", ItemBooster::new,
                    new Item.Properties().stacksTo(1).durability(250));

    /** 1.12.2 {@code ItemLoader}: {@code new ItemBurner()} with {@code setMaxDamage(1024)}. */
    public static final DeferredItem<ItemBurner> BURNER =
            ITEMS.registerItem("burner", ItemBurner::new, new Item.Properties().durability(1024));

    // The 1.12.2 ItemLoader fields kept for the runtime lookups that survive the port:
    //   public static ItemThrow itemThrow;
    //   public static ItemArrow itemArrow;
    // They are declared by the classes that own them (ItemThrow / ItemArrow) once those are ported.
}
