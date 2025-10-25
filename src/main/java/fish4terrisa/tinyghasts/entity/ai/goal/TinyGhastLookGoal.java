package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;

public class TinyGhastLookGoal extends Goal {
    private final TinyGhastEntity ghast;

    @Nullable
    private LivingEntity target;
    private int lookTime;
    private float randomYaw;
    private float randomPitch; 
    private boolean isLookingAtRandomDirection;

    public TinyGhastLookGoal(TinyGhastEntity ghast) {
        this.ghast = ghast;
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity owner = this.ghast.getOwner();

        // 50% chance to look at the owner if they exist
        if (owner != null /*&& this.ghast.getRandom().nextFloat() < 0.5f*/) {
            this.target = owner;
            this.isLookingAtRandomDirection = false;
            return true;
        }

        // Find a random nearby entity to look at
        //List<LivingEntity> nearbyEntities = this.ghast.getWorld().getEntitiesByClass(
        //        LivingEntity.class,
        //        this.ghast.getBoundingBox().expand(20.0), // 20 block search radius
        //        (entity) -> entity.isAlive() && entity != this.ghast && entity != owner
        //);

        //if (!nearbyEntities.isEmpty() && this.ghast.getRandom().nextFloat() < 0.5f) {
        //    this.target = nearbyEntities.get(this.ghast.getRandom().nextInt(nearbyEntities.size()));
        //    this.isLookingAtRandomDirection = false;
        //    return true;
        //}

        // If no entity was found, default to looking in a random direction
        this.target = null;
        this.isLookingAtRandomDirection = true;
        return true;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        // Reset state for the next run
        this.target = null;
        this.isLookingAtRandomDirection = true;
    }

    @Override
    public void tick() {
        // If we have an entity target, keep looking at it
        if (this.ghast.getTarget() != null && this.ghast.getTarget().isAlive()) {
            LivingEntity livingEntity = this.ghast.getTarget();
            double d = 64.0;
            double e = livingEntity.getX() - this.ghast.getX();
            double f = livingEntity.getZ() - this.ghast.getZ();
            this.ghast.setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776f);
            this.ghast.bodyYaw = this.ghast.getYaw();
        }
        else if (this.target != null) {
            double deltaX = this.target.getX() - this.ghast.getX();
            double deltaY = this.target.getEyeY() - this.ghast.getEyeY();
            double deltaZ = this.target.getZ() - this.ghast.getZ();
            
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            this.randomYaw = (float) (MathHelper.atan2(deltaZ, deltaX) * (180.0D / Math.PI)) - 90.0F;
            this.randomPitch = (float) (-(MathHelper.atan2(deltaY, horizontalDistance) * (180.0D / Math.PI)));

            this.ghast.setYaw(this.randomYaw);
            this.ghast.setPitch(this.randomPitch);
            this.ghast.setHeadYaw(this.randomYaw);

            this.isLookingAtRandomDirection = false;
        } else {
            Vec3d vec3d = this.ghast.getVelocity();
            this.ghast.setYaw(-((float)MathHelper.atan2(vec3d.x, vec3d.z)) * 57.295776f);
            this.ghast.bodyYaw = this.ghast.getYaw();
                                                                                
        }
    }
}
