package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public class BlockLoader {

    private static BlockLoader instance;
    public static BlockLoader instance() {
        if (instance == null) instance = new BlockLoader();
        return instance;
    }

    public static LinkedList<Block> blocks;
    public static LinkedHashMap<Block, Item> itemBlocks;

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        opticzCraft.logger.info("registering Blocks");
        event.getRegistry().registerAll(blocks.toArray(new Block[0]));
    }

    @SubscribeEvent
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
        opticzCraft.logger.info("registering ItemBlocks");
        for (Block b : blocks) {
            String regName = b.getUnlocalizedName().toLowerCase();
            if (b.getRegistryName() != null)
                regName = b.getRegistryName().toString().split(":")[1];
            itemBlocks.put(b, new ItemBlock(b).setRegistryName(opticzCraft.MODID, regName));
        }
        event.getRegistry().registerAll(itemBlocks.values().toArray(new Item[0]));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerItemBlockModels(ModelRegistryEvent event) {
        opticzCraft.logger.info("registering ItemBlock Models (Block's Icon)");
        for (Item i : itemBlocks.values()) {
            String regName = i.getUnlocalizedName().toLowerCase();
            if (i.getRegistryName() != null)
                regName = i.getRegistryName().toString();
            ModelLoader.setCustomModelResourceLocation(i, 0,
                    new ModelResourceLocation(regName, "inventory")
            );
        }
    }

    public BlockLoader() {
        blocks = new LinkedList<>();
        itemBlocks = new LinkedHashMap<>();

        blocks.add(new BlockLaserJet());
        blocks.add(new BlockLaserDet());
        blocks.add(new BlockLaserPbs());
        blocks.add(new BlockLaserRec());
        blocks.add(new BlockLaserRef());
        blocks.add(BlockLaserPbs.getSoft());
        blocks.add(BlockLaserRef.getSoft());
    }

}