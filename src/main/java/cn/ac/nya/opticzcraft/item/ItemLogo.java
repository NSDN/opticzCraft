package cn.ac.nya.opticzcraft.item;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.item.Item;

public class ItemLogo extends Item {

    public ItemLogo() {
        super();
        setMaxStackSize(1);
        setUnlocalizedName("Logo");
        setRegistryName(opticzCraft.MODID, "logo");
    }

}
