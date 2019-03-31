package cn.ac.nya.opticzcraft;

import cn.ac.nya.opticzcraft.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

/**
 * Created by drzzm32 on 2019.3.30.
 */
@Mod(modid = opticzCraft.MODID, name = opticzCraft.NAME, version = opticzCraft.VERSION)
public class opticzCraft {

    @Mod.Instance
    public static opticzCraft instance;
    public static final String PARENT = "nyasamaoptics";
    public static final String MODID = "opticzcraft";
    public static final String NAME = "opticzCraft";
    public static final String VERSION = "1.0";
    public static Logger logger;

    @SidedProxy(
            clientSide = "cn.ac.nya.opticzcraft.proxy.ClientProxy",
            serverSide = "cn.ac.nya.opticzcraft.proxy.ServerProxy")
    public static CommonProxy proxy;

    public static opticzCraft getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
