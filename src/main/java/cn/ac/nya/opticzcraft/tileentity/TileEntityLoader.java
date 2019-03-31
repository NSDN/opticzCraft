package cn.ac.nya.opticzcraft.tileentity;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.LinkedList;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class TileEntityLoader {

    private static TileEntityLoader instance;
    public static TileEntityLoader instance() {
        if (instance == null) instance = new TileEntityLoader();
        return instance;
    }

    public static LinkedList<Class<? extends TileEntity>> tileEntities;

    private static void register(Class<? extends TileEntity> tileEntity) {
        String name = tileEntity.getSimpleName().replace("TileEntity", "tile");
        GameRegistry.registerTileEntity(tileEntity, new ResourceLocation(opticzCraft.MODID, name));
    }

    @SubscribeEvent
    public void registerTileEntities(RegistryEvent.Register<Block> event) {
        opticzCraft.logger.info("registering TileEntities");
        for (Class<? extends TileEntity> t : tileEntities)
            register(t);
    }

    public TileEntityLoader() {
        tileEntities = new LinkedList<>();

        tileEntities.add(TileEntityLaserJet.class);
        tileEntities.add(TileEntityLaserDet.class);
        tileEntities.add(TileEntityLaserPbs.class);
        tileEntities.add(TileEntityLaserRec.class);
        tileEntities.add(TileEntityLaserRef.class);
    }

}
