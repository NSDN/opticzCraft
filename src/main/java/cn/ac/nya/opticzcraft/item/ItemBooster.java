package cn.ac.nya.opticzcraft.item;

import cn.ac.nya.opticzcraft.network.FlyPacket;
import cn.ac.nya.opticzcraft.util.BOOM;
import cn.ac.nya.opticzcraft.util.FLY;
import cn.ac.nya.opticzcraft.util.IFlyEquipment;
import cn.ac.nya.opticzcraft.util.ItemNBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The NSA Booster MkI: an off-hand flight assist that accumulates a throttle while fall-flying.
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code ToolMaterial} is now {@link Tier} and the whole material set is {@link Tiers}; the
 *       durability the old constructor took from {@code toolMaterial.getMaxUses()} (250 for iron) is
 *       declared in the item properties by {@code OpticzItems}.</li>
 *   <li>{@code onUpdate} becomes {@link #inventoryTick(ItemStack, Level, Entity, int, boolean)} --
 *       the per-tick inventory hook, same five parameters.</li>
 *   <li>{@code getHeldItemOffhand().isItemEqual(itemStack)} becomes
 *       {@code ItemStack.isSameItemSameComponents(...)}: {@code isItemEqual} compared item <i>and</i>
 *       metadata, and on 1.20.5+ metadata is a component, so the same comparison is the strict one.</li>
 *   <li>{@code world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, ...)} becomes
 *       {@code world.addParticle(ParticleTypes.FIREWORK, ...)}; {@code player.posX/Y/Z} becomes
 *       {@code getX()/getY()/getZ()}; {@code player.motionX/Y/Z} becomes
 *       {@code player.getDeltaMovement()}; {@code itemRand} becomes {@code entity.random} (the item has
 *       no random source of its own any more).</li>
 *   <li>{@code hitEntity} becomes {@code hurtEnemy} and {@code onBlockDestroyed} becomes
 *       {@code mineBlock}; {@code damageItem(n, entity)} becomes
 *       {@code hurtAndBreak(n, entity, EquipmentSlot.MAINHAND)} -- 1.21.1 requires the slot the damage
 *       is attributed to. {@code state.getBlockHardness(world, pos)} becomes
 *       {@code state.getDestroySpeed(level, pos)}.</li>
 *   <li>{@code getItemEnchantability} becomes {@code getEnchantmentValue}; {@code getIsRepairable}
 *       becomes {@code isValidRepairItem}, and the ore-dictionary lookup is replaced by the tier's own
 *       {@code Ingredient} (the ore dictionary does not exist in 1.21.1).</li>
 *   <li>The client update path replaces {@code NetworkWrapper.instance.sendToServer(packet)} with
 *       {@link FlyPacket#sendToServer()}; {@code EntityPlayerSP} becomes {@code LocalPlayer}, which
 *       {@code FlyPacket.fromPlayer} tests for (see that class for the {@code @SideOnly} replacement
 *       reasoning).</li>
 *   <li>{@code addInformation} becomes {@link #appendHoverText(ItemStack, Item.TooltipContext, List,
 *       TooltipFlag)}; {@code TextFormatting} becomes {@code ChatFormatting}.</li>
 *   <li><b>Dropped no-op overrides:</b> 1.12.2 explicitly overrode {@code onLeftClickEntity},
 *       {@code itemInteractionForEntity}, {@code onItemUseFirst}, {@code doesSneakBypassUse},
 *       {@code onItemRightClick} and {@code onItemUse} only to return {@code false}/{@code PASS}. Those
 *       are exactly the 1.21.1 defaults ({@code use} -> {@code pass}, {@code useOn} -> {@code PASS}), so
 *       re-declaring them would add noise without changing behaviour. {@code isFull3D()} is likewise
 *       gone: the 3D-in-hand look is a JSON {@code display} entry now.</li>
 * </ul>
 */
public class ItemBooster extends Item implements IFlyEquipment {

    public ItemNBTHelper<Float> throttle = new ItemNBTHelper<>(Float.class, "throttle");

    @Override
    public float getThrottle(ItemStack stack) {
        return throttle.get(stack);
    }

    @Override
    public void setThrottle(ItemStack stack, float val) {
        throttle.set(stack, val);
    }

    public Tier toolMaterial;

    public ItemBooster(Item.Properties properties) {
        super(properties);
        this.toolMaterial = Tiers.IRON;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(Component.literal(ChatFormatting.AQUA + "boost anything"));
        list.add(Component.literal(ChatFormatting.AQUA + "designed by NSA"));
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
                    Vec3 ext = BOOM.vVec(vec).scale(entity.random.nextGaussian() * 0.05D);
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
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        itemStack.hurtAndBreak(2, target, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!world.isClientSide && (double)state.getDestroySpeed(world, pos) != 0.0D) {
            itemStack.hurtAndBreak(1, entityLiving, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return this.toolMaterial.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack thisStack, ItemStack itemStack) {
        return this.toolMaterial.getRepairIngredient().test(itemStack) || super.isValidRepairItem(thisStack, itemStack);
    }

}
