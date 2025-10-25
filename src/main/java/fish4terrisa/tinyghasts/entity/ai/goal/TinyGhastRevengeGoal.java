package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;

import java.util.EnumSet;

public class TinyGhastRevengeGoal extends TrackTargetGoal {
    private final TinyGhastEntity ghast;
    private LivingEntity lastAttacker;
    private int lastAttackedTime;

    public TinyGhastRevengeGoal(TinyGhastEntity ghast) {
        // We set checkVisibility to false because a ghast can attack what it can't "see" through walls briefly.
        super(ghast, false);
        this.ghast = ghast;
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    /**
     * Returns whether an AI task is interruptible by a higher priority task.
     */
    @Override
    public boolean canStart() {
        LivingEntity attacker = this.ghast.getAttacker();
        if (attacker == null || !attacker.isAlive()) {
            return false;
        }

        // Use the custom canTarget check from TinyGhastEntity
        if (!this.ghast.canTarget(attacker)) {
            return false;
        }

        lastAttackedTime = this.ghast.getLastAttackedTime();
        // Check if the ghast was attacked recently
        return this.ghast.age > lastAttackedTime + 100;
    }

    /**
     * Execute a one-off task
     */
    @Override
    public void start() {
        // Set the ghast's target to the attacker
        this.ghast.setTarget(this.ghast.getAttacker());
        this.lastAttacker = this.ghast.getAttacker();
        this.lastAttackedTime = this.ghast.getLastAttackedTime();

        // Let other tiny ghasts owned by the same player know about the attacker
        LivingEntity owner = this.ghast.getOwner();
        if (owner instanceof PlayerEntity) {
            this.ghast.getWorld().getEntitiesByClass(TinyGhastEntity.class, this.ghast.getBoundingBox().expand(40.0D, 20.0D, 40.0D), (otherGhast) -> {
                return otherGhast != this.ghast && otherGhast.getOwner() == owner;
            }).forEach((allyGhast) -> {
                allyGhast.setTarget(this.ghast.getAttacker());
            });
        }

        super.start();
    }
}
