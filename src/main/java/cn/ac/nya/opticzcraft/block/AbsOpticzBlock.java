package cn.ac.nya.opticzcraft.block;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.opticzCraft;
import cn.ac.nya.opticzcraft.tab.TabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created by drzzm32 on 2019.3.30.
 */
public abstract class AbsOpticzBlock extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool LIGHTED = PropertyBool.create("lighted");

    public AbsOpticzBlock(String name, String id) {
        super(Material.IRON);
        setUnlocalizedName(name);
        setRegistryName(opticzCraft.MODID, id);
        setHardness(2.0F);
        setResistance(blockHardness * 5.0F);
        setSoundType(SoundType.METAL);
        setCreativeTab(TabLoader.opticzCraft);

        setDefaultState(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.UP)
                .withProperty(LIGHTED, false)
        );
    }

    @Nullable
    @Override
    public abstract TileEntity createNewTileEntity(World world, int i);

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(LIGHTED) ? 1 : 0;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return Block.FULL_BLOCK_AABB;
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta)).withProperty(LIGHTED, (meta & 0x8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() | (state.getValue(LIGHTED) ? 0x8 : 0x0);
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIGHTED);
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return facing != state.getValue(FACING);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase player) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, player);
        state = state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, player));
        return state;
    }

    public static String getLineFromBook(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        if (itemStack.getItem() instanceof ItemWritableBook) {
            NBTTagCompound tagCompound = itemStack.getTagCompound();
            if (tagCompound == null) return null;
            NBTTagList tagList = tagCompound.getTagList("pages", 8);
            return tagList.getStringTagAt(0);
        }
        return null;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.scheduleUpdate(pos, this, 1);
    }

    @Override
    public int tickRate(World world) {
        return 20;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote) {
            state = world.getBlockState(pos);
            EnumFacing facing = state.getValue(FACING);
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof IOpticzDev) {
                IOpticzDev dev = (IOpticzDev) tileEntity;

                dev.clearOutputs();
                tick(world, pos, dev, facing);
                dev.clearInputs();

                world.notifyNeighborsOfStateChange(pos, this, false);
                world.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this, false);

                world.notifyBlockUpdate(pos, state, state, 2);
                world.markBlockRangeForRenderUpdate(pos, pos);

                world.scheduleUpdate(pos, this, 1);
            }

        }
    }

    public abstract void tick(World world, BlockPos pos, IOpticzDev dev, EnumFacing facing);

}
