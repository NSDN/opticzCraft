package cn.ac.nya.opticzcraft.event;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class EventsHandler {
    private static EventsHandler instance;

    public static EventsHandler instance() {
        if (instance == null)
            instance = new EventsHandler();
        return instance;
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {

    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Loader.isModLoaded(opticzCraft.PARENT)) {
            TextComponentString str = new TextComponentString(
                    TextFormatting.AQUA + "Thanks for using opticzCraft!"
            );
            event.player.sendMessage(str);
        }
    }

}
