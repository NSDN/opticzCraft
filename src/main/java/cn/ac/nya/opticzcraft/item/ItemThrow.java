package cn.ac.nya.opticzcraft.item;

import cn.ac.nya.opticzcraft.entity.EntityThrow;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The NSF Pressure Bomb item: throw it and it detonates (see {@link EntityThrow}).
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code setMaxStackSize(42)} is not a constructor call any more -- it is the
 *       {@code stacksTo(42)} property supplied by {@code OpticzItems}. {@code setUnlocalizedName} and
 *       {@code setRegistryName} are likewise gone and are accounted for by the registry entry
 *       ({@code opticzcraft:throw} yields the same {@code item.opticzcraft.throw} translation key).
 *       {@code setCreativeTab(null)} is a display-list decision now, not an item flag; see
 *       {@code OpticzCreativeTab}.</li>
 *   <li>{@code onItemRightClick(World, EntityPlayer, EnumHand)} becomes
 *       {@link #use(Level, Player, InteractionHand)}; {@code ActionResult.newResult(SUCCESS, stack)}
 *       becomes {@code InteractionResultHolder.success(stack)}.</li>
 *   <li>{@code SoundEvents.ENTITY_SNOWBALL_THROW} is {@code SoundEvents.SNOWBALL_THROW}; {@code itemRand}
 *       becomes {@code world.random}; {@code getPositionVector()} becomes {@code position()}. The
 *       {@code null} first argument of the 1.12.2 {@code world.playSound} call -- "the sound source is
 *       not a player" -- is the modern {@code null} {@code Player} overload of the same method.</li>
 *   <li>{@code entityThrow.shoot(player, pitch, yaw, 0.0F, 2.0F, 1.0F)} becomes
 *       {@code shootFromRotation(player, pitch, yaw, 0.0F, 2.0F, 1.0F)}. The name change is the whole
 *       change: {@code Projectile} in 1.21.1 splits the two-argument {@code shoot} from the
 *       entity-relative {@code shootFromRotation}, with identical parameters and semantics.</li>
 *   <li>{@code world.spawnEntity} becomes {@code world.addFreshEntity}.</li>
 *   <li>{@code StatList.getObjectUseStats(this)} + {@code player.addStat(stat)} become
 *       {@code player.awardStat(Stats.ITEM_USED.get(this))}. The 1.12.2 null check around them is not
 *       reproduced: {@code Stats.ITEM_USED.get(item)} is registered for every item by the stats table,
 *       so there is no null case to guard (the old code guarded against a lookup miss that cannot
 *       happen here).</li>
 *   <li>{@code !player.isCreative()} still gates the consumption of the stack, and
 *       {@code stack.shrink(1)} is unchanged.</li>
 * </ul>
 */
public class ItemThrow extends Item {

    public ItemThrow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 pos = player.position();
        world.playSound(
                null, pos.x, pos.y, pos.z,
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
        if (!world.isClientSide) {
            EntityThrow entityThrow = new EntityThrow(world, player);
            entityThrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);
            world.addFreshEntity(entityThrow);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }

}
