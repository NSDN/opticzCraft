package cn.ac.nya.opticzcraft.event;

import cn.ac.nya.opticzcraft.block.BlockLoader;
import cn.ac.nya.opticzcraft.item.ItemLoader;
import cn.ac.nya.opticzcraft.tileentity.TileEntityLoader;
import cn.ac.nya.opticzcraft.tileentity.TileEntityModelBinder;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class EventRegister {

    public static void registerCommon() {
        MinecraftForge.EVENT_BUS.register(BlockLoader.instance());
        MinecraftForge.EVENT_BUS.register(ItemLoader.instance());

        MinecraftForge.EVENT_BUS.register(EventsHandler.instance());

        MinecraftForge.EVENT_BUS.register(TileEntityLoader.instance());

    }

    public static void registerServer() {

    }

    public static void registerClient() {
        MinecraftForge.EVENT_BUS.register(TileEntityModelBinder.instance());
    }

}
