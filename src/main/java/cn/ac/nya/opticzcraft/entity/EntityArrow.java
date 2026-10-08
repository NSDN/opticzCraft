package cn.ac.nya.opticzcraft.entity;

import cn.ac.nya.opticzcraft.OpticzEntities;
import cn.ac.nya.opticzcraft.OpticzItems;
import cn.ac.nya.opticzcraft.util.BOOM;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The NSF cluster missile: an arrow that trails firework sparks, and that splits into a ring of
 * {@link EntityThrow} pressure bombs when it has fallen far enough, otherwise detonating with an
 * 8-block vacuum explosion on impact.
 *
 * <p>Ported to Minecraft 1.21.1:</p>
 * <ul>
 *   <li>{@code extends net.minecraft.entity.projectile.EntityArrow} becomes
 *       {@code extends AbstractArrow}. The five-argument constructor of the modern class
 *       ({@code (EntityType, LivingEntity, Level, ItemStack, ItemStack)}) replaced the 1.12.2
 *       {@code (World, EntityLivingBase)} owner form, so the shooter constructor below supplies the
 *       pickup stack itself. Its 1.12.2 counterpart was {@code super(worldIn, shooter)}, which
 *       <i>copy-pasted</i> the stack out of the shooter's held item -- a value this class never read
 *       (it overrides the pickup stack) and which would have handed the bow a second arrow, so the
 *       duplicate is deliberately not reproduced.</li>
 *   <li>{@code onUpdate} becomes {@link #tick()}. {@code arrowShake} is now {@code shakeTime}, and
 *       {@code motionX/Y/Z} is {@code getDeltaMovement()}.</li>
 *   <li>{@code rand} becomes {@code getRandom()}; {@code getPositionVector()} becomes
 *       {@code position()}; {@code posX/posY/posZ} become {@code getX()/getY()/getZ()};
 *       {@code world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, ...)} becomes
 *       {@code level().addParticle(ParticleTypes.FIREWORK, ...)}.</li>
 *   <li>{@code world.getHeight(BlockPos)} becomes
 *       {@code level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z)}. The removed 1.12.2 overload
 *       answered with the first non-air block in the column, and the modern {@code WORLD_SURFACE}
 *       heightmap is defined by the same {@code Heightmap.NOT_AIR} predicate (verified with javap), so
 *       the distance test below keeps its meaning. Note the modern call returns a {@code y} value
 *       rather than a {@code BlockPos}, hence the direct {@code bottom - getY()} comparison.</li>
 *   <li>{@code world.spawnEntity(entityThrow)} becomes {@code level().addFreshEntity(entityThrow)} and
 *       {@code setDead()} becomes {@code discard()}.</li>
 *   <li>{@code getArrowStack()} becomes {@link #getDefaultPickupItem()} -- the modern hook that names
 *       the stack a player picks up, which is what {@code getArrowStack} fed in 1.12.2.</li>
 *   <li>{@code isImmuneToExplosions()} has no 1.21.1 equivalent; as in {@link EntityThrow}, the direct
 *       successor is {@link #ignoreExplosion(Explosion)}, with {@link #isInvulnerableTo} covering the
 *       manual {@code BOOM} path that never builds an {@code Explosion}.</li>
 *   <li>{@code onHit(RayTraceResult)} becomes {@link #onHit(HitResult)}. This is the same single
 *       dispatch point: {@code Projectile#hitTargetOrDeflectSelf} calls {@code onHit} for every hit
 *       type <i>before</i> splitting into {@code onHitEntity}/{@code onHitBlock}, and
 *       {@code AbstractArrow} does not override it. Overriding it here therefore keeps the original
 *       "explode on anything" behaviour instead of picking one of the two branches.</li>
 * </ul>
 */
public class EntityArrow extends AbstractArrow {

    protected float explosionRadius = 8.0F;

    protected double actionVSpeed = -0.4;
    protected int actionCount = 16;
    protected double spreadVel = 0.2;
    protected double minSpreadHeight = 16;

    public EntityArrow(EntityType<? extends EntityArrow> type, Level level) {
        super(type, level);
    }

    /** 1.12.2 {@code EntityArrow(World, EntityLivingBase)}; the pickup stack is set by the caller. */
    public EntityArrow(EntityType<? extends EntityArrow> type, LivingEntity shooter, Level level) {
        super(type, shooter, level, null);
    }

    /** 1.12.2 {@code EntityArrow(World, double, double, double)}. */
    public EntityArrow(EntityType<? extends EntityArrow> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level, new ItemStack(OpticzItems.ARROW.get()), null);
    }

    /**
     * 1.12.2 {@code EntityArrow(World, EntityLivingBase)} and
     * {@code EntityArrow(World, double x, double y, double z)} without the {@code EntityType} argument,
     * which the 1.12.2 call sites inside this class used. Kept so the cluster split below keeps its
     * original shape.
     */
    public EntityArrow(Level level, LivingEntity shooter) {
        this(OpticzEntities.ARROW.get(), shooter, level);
    }

    public EntityArrow(Level level, double x, double y, double z) {
        this(OpticzEntities.ARROW.get(), x, y, z, level);
    }

    /** 1.12.2 {@code isImmuneToExplosions() == true}: skip this missile in explosions. */
    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    /** See {@link EntityThrow#isInvulnerableTo}: the companion guard for the manual {@code BOOM} path. */
    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(source);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide && !this.inGround) {
            Vec3 vec = this.getDeltaMovement();
            Vec3 ext = BOOM.vVec(vec).scale(this.getRandom().nextGaussian() * 0.05D);
            vec = vec.scale(-0.5).add(ext);
            this.level().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY() - 0.3D, this.getZ(),
                    vec.x, vec.y, vec.z);
        }

        if (!this.level().isClientSide && !this.inGround) {
            Vec3 pos = this.position();
            Vec3 vec = this.getDeltaMovement();
            if (actionVSpeed != 0 && vec.y < actionVSpeed && actionCount > 1) {
                int bottom = this.level().getHeight(Heightmap.Types.WORLD_SURFACE,
                        this.blockPosition().getX(), this.blockPosition().getZ());
                if ((double)bottom - pos.y > minSpreadHeight) {
                    Vec3 ext = BOOM.vVec(vec);
                    for (int i = 0; i < actionCount; i++) {
                        EntityThrow entityThrow;
                        if (this.getOwner() instanceof LivingEntity owner)
                            entityThrow = new EntityThrow(this.level(), owner, pos);
                        else
                            entityThrow = new EntityThrow(this.level(), pos);
                        Vec3 v = vec.add(ext.scale(spreadVel - spreadVel / 2 * this.getRandom().nextDouble()));
                        entityThrow.shoot(v.x, v.y, v.z, (float) v.length(), 1.0F);
                        ext = BOOM.rotate(ext, vec, 360.0F / actionCount);
                        this.level().addFreshEntity(entityThrow);
                    }
                    this.level().broadcastEntityEvent(this, (byte)3);
                    this.discard();
                }
            }
        }
    }

    /** 1.12.2 {@code getArrowStack()}: the stack the player picks up. */
    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(OpticzItems.ARROW.get());
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
