package fish4terrisa.tinyghasts.entity;

import fish4terrisa.tinyghasts.TinyGhasts;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.world.World;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Uuids;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.TeleportTarget;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import java.util.function.Predicate;

import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;

import fish4terrisa.tinyghasts.entity.ai.goal.TinyGhastFireballAttackGoal;
import fish4terrisa.tinyghasts.entity.ai.goal.TeleportToOwnerGoal;
import fish4terrisa.tinyghasts.entity.ai.goal.TinyGhastFlyRandomlyGoal;
import fish4terrisa.tinyghasts.entity.ai.goal.TinyGhastRevengeGoal;
import fish4terrisa.tinyghasts.entity.ai.goal.TinyGhastLookGoal;
import fish4terrisa.tinyghasts.entity.ai.goal.OwnerHurtByTargetGoal;
import fish4terrisa.tinyghasts.entity.ai.goal.OwnerHurtTargetGoal;
import fish4terrisa.tinyghasts.entity.ai.control.TinyGhastMoveControl;

public class TinyGhastEntity extends GhastEntity {
    protected static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> OWNER_UUID = DataTracker.registerData(TinyGhastEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);
    protected static final TrackedData<Boolean> IS_TAMED = DataTracker.registerData(TinyGhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<Boolean> IS_DOWNED = DataTracker.registerData(TinyGhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int ticksSinceLastHit = 0;

    public TinyGhastEntity(EntityType<? extends GhastEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new TinyGhastMoveControl(this);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_TAMED, false);
        builder.add(OWNER_UUID, Optional.empty());
        builder.add(IS_DOWNED, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.clear(goal -> true);
        this.targetSelector.clear(goal -> true);

        // AI Goals from lowest priority (bottom) to highest (top)
        this.targetSelector.add(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.add(2, new OwnerHurtByTargetGoal(this));
        this.targetSelector.add(3, new TinyGhastRevengeGoal(this));
        this.targetSelector.add(4, new ActiveTargetGoal<MobEntity>(this, MobEntity.class, 5, false, false, this::shouldAttack));
        this.goalSelector.add(5, new TeleportToOwnerGoal(this, 15));
        this.goalSelector.add(6, new TinyGhastFlyRandomlyGoal(this));
        this.goalSelector.add(7, new TinyGhastLookGoal(this));
        this.goalSelector.add(7, new TinyGhastFireballAttackGoal(this));

    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        this.ticksSinceLastHit = 0;
        // Prevent any damage if the entity is in the downed state
        if (this.isDowned()) {
            return false;
        }

        // Intercept what would be a fatal blow for a tamed Ghast
        if (this.isTamed() && (this.getHealth() - amount <= 0)) {
            // Instead of dying, enter the downed state
            this.setHealth(this.getMaxHealth());
            this.setDowned(true);
            this.getWorld().playSound(null, this.getBlockPos(), this.getDeathSound(), this.getSoundCategory(), 1.0f, 1.0f);
            this.setTarget(null); // Clear any active target
            return false; // Prevents the damage and subsequent death
        }

        return super.damage(world, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isDowned()) {
            this.setTarget(null);
            this.setDownStatus(true);
        } else {
            this.setDownStatus(false);
        }

        // Only add counter if it's below 100ticks(5secs), prevent overflowing
        if (this.ticksSinceLastHit <= 100) {
            this.ticksSinceLastHit++;
        }

        // Regeneration logic should only run on the server
        if (!this.getWorld().isClient()) {
            // Conditions: Tamed, not downed, and out of combat for 5 seconds (100 ticks)
            if (!this.isDowned() && this.ticksSinceLastHit >= 100) {
                // Check if health is below max
                if (this.getHealth() < this.getMaxHealth()) {
                    // Every 40 ticks (2 seconds), heal 1 heart (2 health points)
                    if (this.age % 40 == 0) {
                        this.heal(2.0f);
                    }
                }
            }
        }
    }

    @Nullable
    public LazyEntityReference<LivingEntity> getOwnerReference() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (this.isDowned() && this.isOwner(player) && itemStack.getItem() == Items.LAVA_BUCKET) {
            if (!this.getWorld().isClient) {
                this.setDowned(false);
            }

            if (!player.getAbilities().creativeMode) {
                player.setStackInHand(hand, new ItemStack(Items.BUCKET));
            }

            return ActionResult.SUCCESS;
        }
        if (!this.isTamed() && itemStack.getItem() == Items.CAKE) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            if (!this.getWorld().isClient) {
                if (this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.setTamed(true);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setPersistent();
                    if (this.getWorld() instanceof ServerWorld) {
                        ((ServerWorld) this.getWorld()).spawnParticles(
                                ParticleTypes.HEART,
                                this.getX(),
                                this.getBodyY(0.5D),
                                this.getZ(),
                                7, // particle count
                                this.random.nextGaussian() * 0.02D,
                                this.random.nextGaussian() * 0.02D,
                                this.random.nextGaussian() * 0.02D,
                                0.1D // particle speed
                        );
                    }
                }

            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() || this.isDowned();
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        // Makes the entity invulnerable while downed.
        return this.isDowned() || super.isInvulnerableTo(world, source);
    }

    @Override
    protected boolean isDisallowedInPeaceful() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        if (this.isTamed()) {
            return true;
        }
        return super.shouldRender(distance);
    }

    public boolean isDowned() {
        return this.dataTracker.get(IS_DOWNED);
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    public void setDowned(boolean downed) {
        this.dataTracker.set(IS_DOWNED, downed);
        this.setDownStatus(downed);
    }

    public void setDownStatus(boolean status) {
        this.setInvisible(status);
        this.setAiDisabled(status);
        this.setGlowing(status);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        LazyEntityReference<LivingEntity> lazyEntityReference = this.getOwnerReference();
        nbt.putBoolean("IsTamed", this.isTamed());
        nbt.putBoolean("IsDowned", this.isDowned());
        if (lazyEntityReference != null) {
            lazyEntityReference.writeNbt(nbt, "Owner");
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        LazyEntityReference lazyEntityReference = LazyEntityReference.fromNbtOrPlayerName(nbt, "Owner", this.getWorld());
        if (lazyEntityReference != null) {
            try {
                this.dataTracker.set(OWNER_UUID, Optional.of(lazyEntityReference));
                this.setTamed(true);
            } catch (Throwable throwable) {
                this.setTamed(false);
            }
        } else {
            this.dataTracker.set(OWNER_UUID, Optional.empty());
            this.setTamed(false);
        }
        this.setTamed(nbt.getBoolean("IsTamed").orElse(false));
        this.setDowned(nbt.getBoolean("IsDowned").orElse(false));
    }
    
    public boolean shouldAttack(LivingEntity entity, ServerWorld world) {
        if (!this.canTarget(entity)) {
            return false;
        }
        if (entity instanceof TameableEntity) {
            if (((TameableEntity) entity).isTamed()) {
                if (this.getOwner() == ((TameableEntity) entity).getOwner()) {
                    return false;
                }
            }
        }
        if (entity instanceof Monster && !(entity instanceof TinyGhastEntity) && !(entity instanceof EndermanEntity) && !(entity instanceof TameableEntity) && !(entity instanceof PiglinEntity) && !(entity instanceof ZombifiedPiglinEntity)) {
            return true;
        }
        return false;
    }

    public boolean isTamed() {
        return this.dataTracker.get(IS_TAMED);
    }

    public boolean isOwner(LivingEntity entity) {
        if (!this.isTamed()) {
            return false;
        }
        return entity == this.getOwner();
    }

    public void setTamed(boolean tamed) {
        this.dataTracker.set(IS_TAMED, tamed);
    }

    public LivingEntity getOwner() {
        return LazyEntityReference.resolve(this.getOwnerReference(), this.getWorld(), LivingEntity.class);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(owner).map(LazyEntityReference::new));
    }

    public void setOwner(@Nullable LazyEntityReference<LivingEntity> owner) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(owner));
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (this.getOwner() != null) {
            // Don't target the owner, their pets, or team members
            if (target == this.getOwner() || (this.getOwner().getScoreboardTeam() != null && this.getOwner().getScoreboardTeam() == target.getScoreboardTeam())) {
                return false;
            }
            if (target instanceof TinyGhastEntity && ((TinyGhastEntity) target).getOwner() == this.getOwner()) {
                return false;
            }
        }
        return true;
    }

    public void teleportToOwner(ServerWorld newWorld) {
        LivingEntity owner = this.getOwner();
        if (owner == null) {
            return;
        }
        // Find a safe position near the owner in the new world.
        BlockPos targetPos = findSafeTeleportPosition(newWorld, owner.getBlockPos());

        // If no safe spot is found, teleport directly to the owner's position as a fallback.
        if (targetPos == null) {
            targetPos = owner.getBlockPos();
        }

        // This method handles everything, including detaching from the old world
        // and attaching to the new one.
        TeleportTarget target = new TeleportTarget(newWorld, new Vec3d(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5), Vec3d.ZERO, this.getYaw(), this.getPitch(), TeleportTarget.ADD_PORTAL_CHUNK_TICKET);
        this.teleportTo(target);
        this.getNavigation().stop(); // Stop any current pathing.
    }

    /**
     * Helper method to find a safe, non-solid block to teleport to near a target position.
     *
     * @param world  The world to search in.
     * @param center The central position to search around.
     * @return A safe BlockPos, or null if none is found.
     */
    @Nullable
    private BlockPos findSafeTeleportPosition(ServerWorld world, BlockPos center) {
        Predicate<BlockPos> spotValidator;
        spotValidator = (pos) -> world.getBlockState(pos).isAir() &&
                        !world.getBlockState(pos.down()).getCollisionShape(world, pos.down()).isEmpty();
        for (int i = 0; i < 8; ++i) {
            // Search in a 7x7x5 area around the player
            int x = center.getX() + this.random.nextInt(7) - 3;
            int z = center.getZ() + this.random.nextInt(7) - 3;
            int y = center.getY() + this.random.nextInt(5) - 1; // Check slightly above and below

            BlockPos.Mutable testPos = new BlockPos.Mutable(x, y, z);

            if (spotValidator.test(testPos)) {
                return testPos.toImmutable();
            }
        }
        // Return null if no suitable position was found after 16 attempts.
        return null;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.isTamed() && !this.getWorld().isClient()) {
            // Instead of dying, enter the downed state
            this.setHealth(this.getMaxHealth()); // Heal to full
            this.setDowned(true);
            this.getWorld().playSound(null, this.getBlockPos(), this.getDeathSound(), this.getSoundCategory(), 1.0f, 1.0f);
            this.setTarget(null); // Clear any active target
            return; // Escape death
        }
        super.onDeath(damageSource);
    }

}
