package cn.ac.nya.opticzcraft.item;

import net.minecraft.world.item.Item;

/**
 * The opticzCraft logo item.
 *
 * <p>Ported to Minecraft 1.21.1. 1.12.2 set {@code setMaxStackSize(1)}, {@code setUnlocalizedName("Logo")}
 * and {@code setRegistryName(opticzCraft.MODID, "logo")} in the constructor; in 1.21.1 the stack size
 * is the only one of those that still belongs to the item, so it arrives as an
 * {@link Item.Properties} argument supplied by the registration table, and the name comes from the
 * registry entry ({@code opticzcraft:logo}), which yields the same translation key
 * {@code item.opticzcraft.logo}.</p>
 */
public class ItemLogo extends Item {

    public ItemLogo(Item.Properties properties) {
        super(properties);
    }

}
