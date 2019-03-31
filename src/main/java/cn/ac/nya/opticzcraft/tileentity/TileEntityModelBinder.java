package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.opticzCraft;
import cn.ac.nya.opticzcraft.render.AbsTESR;
import cn.ac.nya.opticzcraft.render.LaserRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TileEntityModelBinder {

    private static TileEntityModelBinder instance;
    public static TileEntityModelBinder instance() {
        if (instance == null) instance = new TileEntityModelBinder();
        return instance;
    }

    public static LinkedHashMap<Class<? extends TileEntity>, AbsTESR> renderers;

    private static void bind(AbsTESR renderer, Class<? extends TileEntity> tileEntity) {
        ClientRegistry.bindTileEntitySpecialRenderer(tileEntity, renderer);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerTileEntitySpecialRenderers(ModelRegistryEvent event) {
        opticzCraft.logger.info("registering TESRs");
        for (Class<? extends TileEntity> tile : renderers.keySet()) {
            bind(renderers.get(tile), tile);
        }
    }

    public TileEntityModelBinder() {
        renderers = new LinkedHashMap<>();

        renderers.put(TileEntityLaserJet.class, new LaserRenderer());
        renderers.put(TileEntityLaserDet.class, new LaserRenderer());
        renderers.put(TileEntityLaserPbs.class, new LaserRenderer());
        renderers.put(TileEntityLaserRec.class, new LaserRenderer());
        renderers.put(TileEntityLaserRef.class, new LaserRenderer());
    }

}
