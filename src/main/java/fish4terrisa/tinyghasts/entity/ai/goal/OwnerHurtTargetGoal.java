package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.TargetPredicate;

import java.util.EnumSet;

public class OwnerHurtTargetGoal extends TrackTargetGoal {
    private final TinyGhastEntity ghast;
    private LivingEntity ownerLastTarget;
    private int ownerLastAttackedTime;

    public OwnerHurtTargetGoal(TinyGhastEntity ghast) {
        // The mob, whether to check for line of sight (false, it should react regardless)
        super(ghast, false);
        this.ghast = ghast;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!this.ghast.isTamed() || this.ghast.getOwner() == null) {
            return false;
        }

        LivingEntity owner = this.ghast.getOwner();
        if (!(owner instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity playerOwner = (PlayerEntity) owner;
        LivingEntity lastAttacked = playerOwner.getAttacking();
        if (lastAttacked == null || !lastAttacked.isAlive() || lastAttacked == this.ghast) {
            return false;
        }
        this.ownerLastTarget = lastAttacked;

        // Check if the owner has a target and if the target is different from the last one we tracked.
        // The time check ensures we only react to recent attacks.
        if (this.ownerLastTarget != null && this.ownerLastAttackedTime != owner.getLastAttackTime() && this.ownerLastTarget.isAlive()) {
            return true; /*this.canTrack(this.ownerLastTarget, TargetPredicate.DEFAULT);*/
        }

        return false;
    }

    @Override
    public void start() {
        // Set the ghast's target to the entity its owner attacked.
        this.ghast.setTarget(this.ownerLastTarget);
        LivingEntity owner = this.ghast.getOwner();
        this.ownerLastAttackedTime = owner.getLastAttackTime();
        super.start();
    }
}
