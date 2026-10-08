package cn.ac.nya.opticzcraft.entity;

import cn.ac.nya.opticzcraft.OpticzEntities;
import cn.ac.nya.opticzcraft.OpticzItems;
import cn.ac.nya.opticzcraft.util.BOOM;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The NSF Pressure Bomb: a thrown projectile that detonates with the mod's hand-rolled vacuum
 * explosion.
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code extends EntityThrowable} becomes {@code extends ThrowableItemProjectile}, the modern
 *       base for "thrown item" projectiles. Every 1.12.2 constructor gains the mandatory
 *       {@code EntityType} as its first argument; the world/owner/position forms are otherwise kept, so
 *       the two convenience constructors the old code used
 *       ({@code (World, EntityLivingBase, Vec3d)} and {@code (World, Vec3d)}) survive with the same
 *       shape (they resolve the registered type themselves).</li>
 *   <li>{@code onImpact(RayTraceResult)} becomes {@link #onHit(HitResult)} -- the modern dispatch point
 *       ({@code ThrowableProjectile#tick} calls {@code Projectile#hitTargetOrDeflectSelf} which calls
 *       {@code onHit}), so the override is the exact counterpart.</li>
 *   <li>{@code onUpdate} becomes {@code tick}. Its particle block was guarded by {@code !this.inGround};
 *       in 1.21.1 {@code ThrowableProjectile} has no {@code inGround} field at all, and this entity
 *       discards itself the moment it hits anything, so the guard could never be false while the
 *       particle code ran. It is therefore dropped, not weakened -- noted rather than silently
 *       changed.</li>
 *   <li>{@code world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, ...)} becomes
 *       {@code level().addParticle(ParticleTypes.FIREWORK, ...)}; {@code rand} becomes
 *       {@code getRandom()}; {@code posX/posY/posZ} become {@code getX()/getY()/getZ()};
 *       {@code getPositionVector()} becomes {@code position()}.</li>
 *   <li>{@code world.setEntityState(this, (byte)3)} becomes
 *       {@code level().broadcastEntityEvent(this, (byte)3)} and {@code setDead()} becomes
 *       {@code discard()}; {@code BOOM.$(world, dimension, ...)} loses the now-derivable dimension id
 *       (see {@code BOOM}).</li>
 *   <li>{@code getItem()} returned the {@link Item} the projectile renders as, overriding
 *       {@code EntityThrowable#getItem()}. In 1.21.1 that name is taken by
 *       {@code ThrowableItemProjectile#getItem()} returning an {@code ItemStack}, so the faithful
 *       counterpart is {@link #getDefaultItem()} -- the same hook the vanilla snowball uses to say which
 *       item it is.</li>
 *   <li>{@code isImmuneToExplosions()} has no 1.21.1 equivalent; the direct successor is
 *       {@link #ignoreExplosion(Explosion)} (see also {@link #isInvulnerableTo}), which is what
 *       {@code BOOM} consults to keep the mod's own projectiles out of its vacuum explosion.</li>
 * </ul>
 */
public class EntityThrow extends ThrowableItemProjectile {

    protected float explosionRadius = 4.0F;

    public EntityThrow(EntityType<? extends EntityThrow> type, Level level) {
        super(type, level);
    }

    public EntityThrow(EntityType<? extends EntityThrow> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    public EntityThrow(EntityType<? extends EntityThrow> type, LivingEntity thrower, Level level) {
        super(type, thrower, level);
    }

    /** 1.12.2 {@code EntityThrow(World, EntityLivingBase)}. */
    public EntityThrow(Level level, LivingEntity thrower) {
        this(OpticzEntities.THROW.get(), thrower, level);
    }

    /** 1.12.2 {@code EntityThrow(World, EntityLivingBase, Vec3d)}. */
    public EntityThrow(Level level, LivingEntity thrower, Vec3 pos) {
        this(OpticzEntities.THROW.get(), thrower, level);
        this.setPos(pos.x, pos.y, pos.z);
    }

    /** 1.12.2 {@code EntityThrow(World, Vec3d)}. */
    public EntityThrow(Level level, Vec3 pos) {
        this(OpticzEntities.THROW.get(), pos.x, pos.y, pos.z, level);
    }

    /** 1.12.2 {@code getItem()}: the item this projectile renders as (see the class notes). */
    @Override
    protected Item getDefaultItem() {
        return OpticzItems.THROW.get();
    }

    /** 1.12.2 {@code isImmuneToExplosions() == true}: skip this projectile in explosions. */
    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    /**
     * Companion of {@link #ignoreExplosion(Explosion)} for the manual (non-vanilla) explosion path in
     * {@code BOOM}, which has no {@code Explosion} object to hand and therefore tests the damage type.
     */
    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(source);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            Vec3 vec = this.getDeltaMovement();
            Vec3 ext = BOOM.vVec(vec).scale(this.getRandom().nextGaussian() * 0.05D);
            vec = vec.scale(-0.5).add(ext);
            this.level().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY() - 0.3D, this.getZ(),
                    vec.x, vec.y, vec.z);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            Vec3 pos = this.position();
            BOOM.$(this.level(), pos.x, pos.y + 0.5, pos.z, explosionRadius);
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

}
