package cn.ac.nya.opticzcraft.item;

import cn.ac.nya.opticzcraft.network.FlyPacket;
import cn.ac.nya.opticzcraft.util.BOOM;
import cn.ac.nya.opticzcraft.util.FLY;
import cn.ac.nya.opticzcraft.util.IFlyEquipment;
import cn.ac.nya.opticzcraft.util.ItemNBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The NSF Thermit Booster: a pickaxe that doubles as an off-hand flight assist and fires a small
 * fireball on use.
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code extends ItemPickaxe} becomes {@code extends PickaxeItem}; the 1.12.2 constructor took
 *       the whole {@code ToolMaterial} and the durability was overridden with {@code setMaxDamage(1024)},
 *       which in 1.21.1 is the {@code durability(1024)} item property supplied by {@code OpticzItems}.
 *       The material itself is {@link Tiers#IRON}, exactly as before.</li>
 *   <li>{@code onItemRightClick} becomes {@link #use(Level, Player, InteractionHand)} returning
 *       {@code InteractionResultHolder}; {@code ActionResult.newResult(SUCCESS, stack)} becomes
 *       {@code InteractionResultHolder.success(stack)}.</li>
 *   <li>{@code EntitySmallFireball} becomes {@link SmallFireball}, and its five-double constructor
 *       becomes the {@code (Level, x, y, z, Vec3)} form which stores the vector as the initial
 *       velocity -- so {@code ball.motionX += player.motionX} becomes an
 *       {@code setDeltaMovement(getDeltaMovement().add(...))}. {@code ball.shootingEntity = player}
 *       becomes {@code setOwner(player)}.</li>
 *   <li>{@code StatList.getObjectUseStats(this)} + {@code player.addStat(...)} become
 *       {@code player.awardStat(Stats.ITEM_USED.get(this))}.</li>
 *   <li>{@code SoundEvents.ENTITY_FIREWORK_LAUNCH} is {@code SoundEvents.FIREWORK_ROCKET_LAUNCH};
 *       {@code itemRand} becomes {@code world.random}; {@code world.spawnEntity} becomes
 *       {@code world.addFreshEntity}; {@code stack.damageItem(1, player)} becomes
 *       {@code hurtAndBreak(1, player, EquipmentSlot.MAINHAND)}.</li>
 *   <li>{@code getPositionVector()} becomes {@code position()}; {@code getLookVec()} becomes
 *       {@code getLookAngle()}.</li>
 *   <li>The inventory/flight-assist half is identical to {@link ItemBooster}: {@code onUpdate} becomes
 *       {@code inventoryTick}, {@code isItemEqual} becomes
 *       {@code ItemStack.isSameItemSameComponents}, and {@code isFull3D()} is gone (the in-hand look is
 *       a JSON {@code display} entry now).</li>
 * </ul>
 */
public class ItemBurner extends PickaxeItem implements IFlyEquipment {

    public ItemNBTHelper<Float> throttle = new ItemNBTHelper<>(Float.class, "throttle");

    @Override
    public float getThrottle(ItemStack stack) {
        return throttle.get(stack);
    }

    @Override
    public void setThrottle(ItemStack stack, float val) {
        throttle.set(stack, val);
    }

    public ItemBurner(Item.Properties properties) {
        super(Tiers.IRON, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(Component.literal(ChatFormatting.LIGHT_PURPLE + "boost via your hand"));
        list.add(Component.literal(ChatFormatting.LIGHT_PURPLE + "designed by NSF"));
    }

    public void onClientUpdate(Entity entity) {
        if (entity instanceof Player player) {
            FlyPacket packet = FlyPacket.fromPlayer(player);
            if (packet != null) {
                packet.sendToServer();
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level world, Entity entity, int index, boolean inHand) {
        if (entity instanceof Player player) {

            if (ItemStack.isSameItemSameComponents(player.getOffhandItem(), itemStack)) {
                if (world.isClientSide)
                    onClientUpdate(player);
                if (FLY.canFly(player)) {
                    Vec3 vec = player.getDeltaMovement();
                    Vec3 inv = vec.normalize().scale(-1);
                    Vec3 ext = BOOM.vVec(vec).scale(entity.getRandom().nextGaussian() * 0.05D);
                    vec = vec.scale(-0.5).add(ext);
                    world.addParticle(ParticleTypes.FIREWORK,
                            player.getX() + inv.x,
                            player.getY() + inv.y,
                            player.getZ() + inv.z,
                            vec.x, vec.y, vec.z);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 pos = player.position();
        Vec3 vec = player.getLookAngle();
        world.playSound(
                null, pos.x, pos.y, pos.z,
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL,
                0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
        if (!world.isClientSide) {
            SmallFireball ball = new SmallFireball(world,
                    player.getX(), player.getY() + (double)player.getEyeHeight() - 0.1, player.getZ(),
                    vec);
            ball.setOwner(player);
            ball.setDeltaMovement(ball.getDeltaMovement().add(player.getDeltaMovement()));
            world.addFreshEntity(ball);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

}
