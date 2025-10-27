package fish4terrisa.tinyghasts.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Uuids;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.Item.Settings;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Optional;
import java.util.UUID;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import fish4terrisa.tinyghasts.TinyGhasts;

public class TinyGhastFireballEntity extends ThrownItemEntity {

    private static final TrackedData<Boolean> IS_SNOWBALL = DataTracker.registerData(TinyGhastFireballEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private LivingEntity target;
    private UUID targetUuid;
    private int cooldown;

    public TinyGhastFireballEntity(EntityType<? extends TinyGhastFireballEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public TinyGhastFireballEntity(World world, TinyGhastEntity owner, LivingEntity target, double X, double Y, double Z) {
        this(TinyGhasts.TINYGHAST_FIREBALL, world);
        this.setOwner(owner);
        this.target = target;
        this.targetUuid = target.getUuid();
        this.setVelocity(X, Y, Z, 1.0F, 1.0F);
        this.cooldown = 0;

        // 25% chance to be a snowball
        if (!world.isClient && world.random.nextFloat() < 0.25f) {
            this.dataTracker.set(IS_SNOWBALL, true);
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_SNOWBALL, false);
    }

    @Override
    public void tick() {
        super.tick();

        // Homing logic
        if (!this.getWorld().isClient) {
            if (this.target == null || !this.target.isAlive()) {
                // If target is dead or gone, stop homing and continue straight
                if (this.targetUuid != null && this.getWorld() instanceof ServerWorld) {
                    Entity entity = ((ServerWorld) this.getWorld()).getEntity(this.targetUuid);
                    if (entity instanceof LivingEntity && entity.isAlive()) {
                        this.target = (LivingEntity) entity;
                    } else {
                        this.target = null; // Target is confirmed dead/gone
                    }
                }
            }
        }

        if (this.target != null && this.target.isAlive() && this.cooldown <= 0) {
            Vec3d targetPos = this.target.getPos().add(0, this.target.getHeight() * 3 / 4, 0);
            Vec3d currentPos = this.getPos();
            Vec3d direction = targetPos.subtract(currentPos).normalize();
            this.setVelocity(direction.multiply(1).x, direction.multiply(1).y, direction.multiply(1).z, 1.0F, 1.0F); // Adjust speed here
            this.cooldown = 5;
        }
        if (this.cooldown < 0) {
            this.cooldown = 5;
        }
        this.cooldown--;

    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!(this.getOwner() instanceof TinyGhastEntity)) {
            this.createVisualExplosion();
            this.discard();
            return;
        }
        if (!(entityHitResult.getEntity() instanceof LivingEntity)) {
            this.createVisualExplosion();
            this.discard();
            return;
        }

        LivingEntity entity = (LivingEntity) entityHitResult.getEntity();
        TinyGhastEntity owner = (TinyGhastEntity) this.getOwner();

        if (entity == owner || entity == owner.getOwner() || (owner.getOwner().getScoreboardTeam() != null && entity.getScoreboardTeam() == owner.getOwner().getScoreboardTeam())) {
            this.createVisualExplosion();
            this.discard();
            return;
        }
        if (entity instanceof TinyGhastEntity && ((TinyGhastEntity) entity).getOwner() == owner.getOwner()) {
            this.createVisualExplosion();
            this.discard();
            return;
        }

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            boolean isSnowball = this.dataTracker.get(IS_SNOWBALL);
            DamageSource damageSource = this.getDamageSources().magic();/*.thrown(this, owner);*/

            if (isSnowball) {
                if (this.getWorld() instanceof ServerWorld) {
                    livingEntity.damage((ServerWorld) this.getWorld(), damageSource, 4.0f); // 2 heart
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1));
                }
                Vec3d knockbackVec = this.getPos().subtract(livingEntity.getPos()).normalize();
                livingEntity.takeKnockback(0.5, knockbackVec.x, knockbackVec.z);
            } else {
                if (this.getWorld() instanceof ServerWorld) {
                    livingEntity.damage((ServerWorld) this.getWorld(), damageSource, 1.0f); // Half a heart
                    livingEntity.setFireTicks(40); // set it on fire for 2 sec
                }
            }
        }
        this.createVisualExplosion();
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.createVisualExplosion();
        this.discard();
    }

    // This creates a purely visual explosion that does no damage and breaks no blocks.
    private void createVisualExplosion() {
        if (!this.getWorld().isClient) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 0.5f, false, World.ExplosionSourceType.NONE);
        }
    }

    // This determines what the FlyingItemEntityRenderer renders
    @Override
    public ItemStack getStack() {
        if (this.dataTracker.get(IS_SNOWBALL)) {
            return new ItemStack(Items.SNOWBALL);
        } else {
            return new ItemStack(Items.FIRE_CHARGE);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsSnowball", this.dataTracker.get(IS_SNOWBALL));
        if (this.targetUuid != null) {
            nbt.put("Target", Uuids.CODEC, this.targetUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(IS_SNOWBALL, nbt.getBoolean("IsSnowball").orElse(false));
        if (nbt.get("Target", Uuids.CODEC).isPresent()) {
            this.targetUuid = nbt.get("Target", Uuids.CODEC).orElse(null);
        }
    }
}
