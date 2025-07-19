package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;
import java.util.List;

public class TinyGhastLookGoal extends Goal {
    private final TinyGhastEntity ghast;

    @Nullable
    private LivingEntity target;
    private int lookTime;
    private int cooldown;
    private float randomYaw;
    private float randomPitch; 
    private boolean isLookingAtRandomDirection;

    public TinyGhastLookGoal(TinyGhastEntity ghast) {
        this.ghast = ghast;
        // Start with a random cooldown so they don't all sync up
        this.cooldown = 20 + this.ghast.getRandom().nextInt(20);
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (this.ghast.getTarget() != null) {
            return false; // Don't look around if actively targeting something to attack.
        }

        // Reset cooldown for the next attempt (30 seconds)
        if (this.cooldown <= 0) {
            this.cooldown = 20 + this.ghast.getRandom().nextInt(20);
        }

        LivingEntity owner = this.ghast.getOwner();

        // 50% chance to look at the owner if they exist
        if (owner != null && this.ghast.getRandom().nextFloat() < 0.5f) {
            this.target = owner;
            this.isLookingAtRandomDirection = false;
            return true;
        }

        // Find a random nearby entity to look at
        List<LivingEntity> nearbyEntities = this.ghast.getWorld().getEntitiesByClass(
                LivingEntity.class,
                this.ghast.getBoundingBox().expand(20.0), // 20 block search radius
                (entity) -> entity.isAlive() && entity != this.ghast && entity != owner
        );

        if (!nearbyEntities.isEmpty()) {
            this.target = nearbyEntities.get(this.ghast.getRandom().nextInt(nearbyEntities.size()));
            this.isLookingAtRandomDirection = false;
            return true;
        }

        // If no entity was found, default to looking in a random direction
        this.target = null;
        this.isLookingAtRandomDirection = true;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        // If we have a target entity, stop if it's dead or invalid
        if (this.target != null && !this.target.isAlive()) {
            this.target = null;
            return false;
        }
        if (this.lookTime <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        // Set look time to a random duration between 10s (200 ticks) and 20s (400 ticks)
        this.lookTime = this.ghast.getRandom().nextInt(101) + 100;

            // Pick a random direction and set the ghast's rotation
        if (this.isLookingAtRandomDirection) {
            this.randomYaw = this.ghast.getRandom().nextFloat() * 360.0F;
            this.randomPitch = this.ghast.getRandom().nextFloat() * 180.0F - 90.0F; // -90 to +90
                                                                                            }
    }

    @Override
    public void stop() {
        // Reset state for the next run
        this.target = null;
        this.isLookingAtRandomDirection = false;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        // If we have an entity target, keep looking at it
        if (this.target != null) {
            double deltaX = this.target.getX() - this.ghast.getX();
            double deltaY = this.target.getEyeY() - this.ghast.getEyeY();
            double deltaZ = this.target.getZ() - this.ghast.getZ();
            
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            this.randomYaw = (float) (MathHelper.atan2(deltaZ, deltaX) * (180.0D / Math.PI)) - 90.0F;
            this.randomPitch = (float) (-(MathHelper.atan2(deltaY, horizontalDistance) * (180.0D / Math.PI)));


            this.isLookingAtRandomDirection = false;
        }
        // For random direction looking, the direction was already set in start().
        // We just let the timer run down.
       //if (this.isLookingAtRandomDirection) { 
        this.ghast.setYaw(this.randomYaw);
        this.ghast.setPitch(this.randomPitch);
        this.ghast.setHeadYaw(this.randomYaw);
        //}
        --this.lookTime;
    }
}
