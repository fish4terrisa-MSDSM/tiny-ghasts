package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.TargetPredicate;

import java.util.EnumSet;

public class OwnerHurtByTargetGoal extends TrackTargetGoal {
    private final TinyGhastEntity ghast;
    private LivingEntity attacker;
    private int lastAttackedTime;

    public OwnerHurtByTargetGoal(TinyGhastEntity ghast) {
        // set mustSee to false so the Ghast will help its owner even if it didn't witness the attack directly.
        super(ghast, false);
        this.ghast = ghast;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!this.ghast.isTamed()) {
            return false;
        }
        LivingEntity owner = this.ghast.getOwner();
        if (owner == null) {
            return false;
        }
        // Get the entity that attacked the owner
        this.attacker = owner.getLastAttacker();
        // Check if the time the owner was last attacked is more recent than our last check
        int i = owner.getLastAttackedTime();
        if (i != this.lastAttackedTime /*&& this.canTrack(this.attacker, TargetPredicate.DEFAULT )*/) {
            // Check if the attacker is a valid target using the custom logic in TinyGhastEntity
            return this.ghast.canTarget(this.attacker);
        }
        return false;
    }

    @Override
    public void start() {
        // Set the ghast's target to the owner's attacker
        this.mob.setTarget(this.attacker);
        LivingEntity owner = this.ghast.getOwner();
        if (owner != null) {
            // Store the current time of the attack to prevent re-triggering for the same event
            this.lastAttackedTime = owner.getLastAttackedTime();
        }
        super.start();
    }
}
