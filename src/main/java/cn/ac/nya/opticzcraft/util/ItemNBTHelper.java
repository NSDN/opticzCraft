package cn.ac.nya.opticzcraft.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Created by drzzm32 on 2020.3.1
 *
 * <p>Ported to Minecraft 1.21.1. The public shape is deliberately unchanged -- {@code set} and
 * {@code get} still take an {@link ItemStack} and a {@code Class<T>} tag selector -- because every
 * call site (the fly equipment's throttle) keeps working through it.</p>
 *
 * <p>What changed is the storage: {@code ItemStack#getTagCompound}/{@code setTagCompound} were removed
 * in 1.20.5, so the helper now reads and writes the same {@link CompoundTag} through
 * {@link DataComponents#CUSTOM_DATA} and {@link CustomData}. This is the mechanical migration path
 * agreed for the whole pack, and it keeps the NBT keys identical.</p>
 */
public class ItemNBTHelper<T> {

    private Class<T> cls;
    private String nbtTag;

    public ItemNBTHelper(Class<T> cls, String nbtTag) {
        this.cls = cls;
        this.nbtTag = nbtTag;
    }

    public void set(ItemStack itemStack, T value) {
        try {
            Object obj = (Object) value;
            CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tagCompound -> {
                if (cls.equals(Integer.class)) tagCompound.putInt(nbtTag, (int) obj);
                else if (cls.equals(int[].class)) tagCompound.putIntArray(nbtTag, (int[]) obj);
                else if (cls.equals(Float.class)) tagCompound.putFloat(nbtTag, (float) obj);
                else if (cls.equals(String.class)) tagCompound.putString(nbtTag, (String) obj);
                else if (cls.equals(Boolean.class)) tagCompound.putBoolean(nbtTag, (Boolean) obj);
            });
        } catch (Exception e) {
            return;
        }
    }

    public T get(ItemStack itemStack) {
        try {
            CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
            if (data == null) return null;
            CompoundTag tagCompound = data.copyTag();
            T t = null;
            if (cls.equals(Integer.class)) t = cls.cast(tagCompound.getInt(nbtTag));
            else if (cls.equals(int[].class)) t = cls.cast(tagCompound.getIntArray(nbtTag));
            else if (cls.equals(Float.class)) t = cls.cast(tagCompound.getFloat(nbtTag));
            else if (cls.equals(String.class)) t = cls.cast(tagCompound.getString(nbtTag));
            else if (cls.equals(Boolean.class)) t = cls.cast(tagCompound.getBoolean(nbtTag));
            return t;
        } catch (Exception e) {
            return null;
        }
    }

}
