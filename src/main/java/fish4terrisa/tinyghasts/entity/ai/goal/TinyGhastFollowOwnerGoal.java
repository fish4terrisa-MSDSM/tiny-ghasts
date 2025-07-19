package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.world.WorldView;

import java.util.EnumSet;

public class TinyGhastFollowOwnerGoal extends Goal {
    /* 
     * UNUSED
     */

    private final TinyGhastEntity mob;
    private LivingEntity owner;
    private final WorldView world;
    private final double speed;
    private final EntityNavigation navigation;
    private int timeToRecalculatePath;

    // The mob will stop trying to move towards the owner when closer than this distance.
    private final float stopDistance;

    // The mob will start trying to move towards the owner when farther than this distance.
    private final float startDistance;
    private float oldWaterCost;

    public TinyGhastFollowOwnerGoal(TinyGhastEntity mob, double speed, float startDistance, float stopDistance) {
        this.mob = mob;
        this.world = mob.getWorld();
        this.speed = speed;
        this.navigation = mob.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));

    }

    /**
     * Returns whether the goal should begin executing.
     * It will begin if the mob is tamed, has an owner, and is farther away
     * from the owner than the startDistance.
     */
    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.mob.getOwner();
        if (livingEntity == null || !this.mob.isTamed() || livingEntity.isSpectator() || this.mob.isLeashed()) {
            return false;
        }

        // Don't start if we are already closer than the minimum distance.
        if (this.mob.squaredDistanceTo(livingEntity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        }

        this.owner = livingEntity;
        return true;
    }

    /**
     * Returns whether the goal should continue to execute.
     * It will continue as long as the mob is not idle, has a valid owner,
     * and is farther away than the stopDistance.
     */
    @Override
    public boolean shouldContinue() {
        if (this.navigation.isIdle() || this.owner == null || !this.owner.isAlive()) {
            return false;
        }

        // Stop executing if we get too close to the owner.
        return this.mob.squaredDistanceTo(this.owner) > (double) (this.stopDistance * this.stopDistance) && !this.mob.isLeashed();
    }

    /**
     * Execute a one-off task when the goal first starts.
     */
    @Override
    public void start() {
        this.timeToRecalculatePath = 0;
        // Allow the mob to pathfind over water, since it's flying.
        this.oldWaterCost = this.mob.getPathfindingPenalty(PathNodeType.WATER);
        this.mob.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
    }

    /**
     * Execute a one-off task when the goal stops executing.
     */
    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        // Restore the original water pathing penalty.
        this.mob.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterCost);
    }

    /**
     * Keep executing the goal's task.
     */
    @Override
    public void tick() {
        if (this.owner == null || !this.owner.isAlive()) {
            return;
        }

        // Always look at the owner while following.
        this.mob.getLookControl().lookAt(this.owner, 10.0f, (float) this.mob.getMaxLookPitchChange());

        // Recalculate the path to the owner every 10 ticks to reduce performance impact.
        if (--this.timeToRecalculatePath > 0) {
            return;
        }
        this.timeToRecalculatePath = this.getTickCount(10);

        // Start moving towards the owner.
        this.navigation.startMovingTo(this.owner, this.speed);
    }
}
