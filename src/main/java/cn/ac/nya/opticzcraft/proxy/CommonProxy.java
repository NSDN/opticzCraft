package cn.ac.nya.opticzcraft.proxy;

import cn.ac.nya.opticzcraft.event.EventRegister;
import cn.ac.nya.opticzcraft.opticzCraft;
import cn.ac.nya.opticzcraft.tab.TabLoader;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.lang.reflect.Field;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        if (Loader.isModLoaded(opticzCraft.PARENT)) {
            opticzCraft.logger.info("Detected NyaSama Optics!");
            try {
                Class cls = Class.forName("club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader");
                Field field = cls.getField("tabNyaSamaOptics");
                Object obj = field.get(cls);
                if (obj instanceof CreativeTabs)
                    TabLoader.opticzCraft = (CreativeTabs) obj;
            } catch (Exception e) {
                opticzCraft.logger.error(e);
            }
        } else {
            new TabLoader(event);
        }

        EventRegister.registerCommon();
    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }

}
