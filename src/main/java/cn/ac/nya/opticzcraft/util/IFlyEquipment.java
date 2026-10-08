package cn.ac.nya.opticzcraft.util;

import cn.ac.nya.opticzcraft.opticzCraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Contract shared by the two booster items (the NSA Booster and the NSF Thermit Booster).
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code LivingEvent.LivingUpdateEvent} no longer exists in NeoForge 1.21.1 (verified by
 *       listing the jar: {@code LivingEvent} only carries {@code LivingJumpEvent} and
 *       {@code LivingVisibilityEvent}). The replacement is
 *       {@link PlayerTickEvent.Post}, which is a closer match anyway -- the old handler opened with an
 *       {@code instanceof EntityPlayer} test, so it only ever cared about players.</li>
 *   <li>The handler is registered by {@link EventBusSubscriber} on the payload class instead of by
 *       {@code event.EventRegister} through {@code MinecraftForge.EVENT_BUS}; the 1.12.2
 *       {@code instance()} singleton accessors disappear with it. Following the v3 convention the
 *       handler is {@code static}, and no {@code bus = ...} argument is given.</li>
 *   <li>{@code player.moveForward} became {@code player.zza}, {@code Vec3d.lengthVector()} became
 *       {@link Vec3#length()}, and {@code MathHelper.clamp} became {@link Mth#clamp}.</li>
 * </ul>
 *
 * <pre>
 * DEFERRED -- NOT PORTED IN THIS PASS:
 *   The nested {@code RenderThrottle} class (the on-screen "FLY/IDLE" throttle bar) is a HUD drawing
 *   concern, so it is deliberately left out of this batch together with the rest of the render layer.
 *   It cannot be carried over mechanically: 1.12.2 subscribed to {@code RenderGameOverlayEvent.Text} and
 *   drew straight through {@code FontRenderer.drawStringWithShadow} on a {@code ScaledResolution};
 *   1.21.1 replaces that with either a {@code LayeredDraw.Layer} registered through
 *   {@code RegisterGuiLayersEvent} or {@code RenderGuiEvent.Post} + {@code GuiGraphics}. The three
 *   strings and their layout ("FLY"/"IDLE", the {@code -|-} throttle bar, the speed line) are recorded
 *   here so the port is mechanical when the GUI batch happens.
 * </pre>
 */
public interface IFlyEquipment {

    float getThrottle(ItemStack stack);
    void  setThrottle(ItemStack stack, float val);

    float STEP = 0.1F;

    /**
     * Advances the throttle from the player's forward input and applies it while fall-flying.
     *
     * <p>Registered on the game event bus by the annotation: {@code PlayerTickEvent} is not an
     * {@code IModBusEvent}, so routing by event type already sends it to {@code NeoForge.EVENT_BUS}.</p>
     */
    @EventBusSubscriber(modid = opticzCraft.MODID)
    class UpdatePlayer {

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            ItemStack stack = player.getOffhandItem();
            if (stack.getItem() instanceof IFlyEquipment equ) {

                if (FLY.canFly(player)) {
                    float now = equ.getThrottle(stack);
                    now = now + player.zza * STEP;
                    now = Mth.clamp(now, -1, 1);
                    equ.setThrottle(stack, now);
                    FLY.control(player, now);
                }
                if (Math.abs(player.zza) < 0.01) {
                    Vec3 vel = player.getDeltaMovement();
                    if (vel.length() < 0.01)
                        equ.setThrottle(stack, 0.0F);
                }
            }
        }

    }

}
