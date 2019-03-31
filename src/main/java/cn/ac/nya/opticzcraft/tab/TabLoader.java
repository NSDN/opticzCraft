package cn.ac.nya.opticzcraft.tab;

import cn.ac.nya.opticzcraft.item.ItemLoader;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TabLoader {

    public static CreativeTabs opticzCraft;

    public TabLoader(FMLPreInitializationEvent event) {
        opticzCraft = new CreativeTabs("opticzCraft") {
            @Override
            public ItemStack getTabIconItem() {
                return ItemLoader.logo.getDefaultInstance();
            }
        };
    }

}
