package cn.ac.nya.opticzcraft.item;

import cn.ac.nya.opticzcraft.OpticzEntities;
import cn.ac.nya.opticzcraft.entity.EntityArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The NSF cluster missile item: the arrow that the mod's bow fires, which splits mid-air
 * (see {@link EntityArrow}).
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code extends net.minecraft.item.ItemArrow} becomes {@code extends ArrowItem}. That is still
 *       a concrete {@code Item} subclass in 1.21.1 (verified with javap: it implements
 *       {@code ProjectileItem} and overrides {@code createArrow} plus {@code asProjectile}), which is
 *       in fact a better fit than before -- the new {@code asProjectile} hook makes dispensers and
 *       crossbows go through {@code createArrow} as well, so the override below covers all three
 *       firing paths. No separate override is needed for that method.</li>
 *   <li>{@code setUnlocalizedName}/{@code setRegistryName}/{@code setCreativeTab(null)} are gone, as in
 *       {@link ItemThrow}: the {@code opticzcraft:arrow} registry entry supplies the translation key
 *       and {@code OpticzCreativeTab} decides tab membership.</li>
 *   <li>{@code createArrow(World, ItemStack, EntityLivingBase)} becomes
 *       {@link #createArrow(Level, ItemStack, LivingEntity, ItemStack)}: the modern signature adds the
 *       weapon stack, which this port does not need. The owner is <b>not</b> attached here -- the
 *       caller does that, exactly as {@code BowItem} does with the vanilla {@code Arrow}.</li>
 *   <li>{@code new EntityArrow(world, player)} becomes {@code new EntityArrow(type, player, level)};
 *       the {@code EntityType} argument is mandatory now, and the pickup stack is set by that
 *       constructor (see {@link EntityArrow}).</li>
 * </ul>
 */
public class ItemArrow extends ArrowItem {

    public ItemArrow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public EntityArrow createArrow(Level level, ItemStack stack, LivingEntity shooter, ItemStack weapon) {
        return new EntityArrow(OpticzEntities.ARROW.get(), shooter, level);
    }

}
