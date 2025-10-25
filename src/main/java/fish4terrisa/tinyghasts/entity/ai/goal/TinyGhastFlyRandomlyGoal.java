package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;

import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

import net.minecraft.util.math.random.Random;

public class TinyGhastFlyRandomlyGoal extends Goal {
    private final TinyGhastEntity ghast;

    public TinyGhastFlyRandomlyGoal(TinyGhastEntity ghast) {
        this.ghast = ghast;
        // This goal controls the entity's movement.
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    /**
     * Determines if the goal should begin execution.
     */
    @Override
    public boolean canStart() {
        MoveControl moveControl = this.ghast.getMoveControl();

        // If the ghast is not already trying to move somewhere...
        if (!moveControl.isMoving()) {
            // ...then there is a small chance to start this goal.
            // This prevents the goal from firing constantly.
            return this.ghast.getRandom().nextInt(5) == 1;
        } else {
            double distanceX = moveControl.getTargetX() - this.ghast.getX();
            double distanceY = moveControl.getTargetY() - this.ghast.getY();
            double distanceZ = moveControl.getTargetZ() - this.ghast.getZ();

            double distance = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;
            return distance < 1.0D || distance > 72.0D;
        }
    }

    /**
     * Determines if the goal should continue to execute.
     *
     * @return false, because this is a "one-shot" goal. It sets a destination and immediately yields control.
     */
    @Override
    public boolean shouldContinue() {
        return false;
    }

    /**
     * Executes the goal's main logic.
     */
    @Override
    public void start() {
        Random random = this.ghast.getRandom();

        // Pick a random destination in a 16x8x16 area around the ghast.
        // The vertical range is smaller to prevent it from flying too high or low.
        double targetX = this.ghast.getX() + (random.nextFloat() * 2.0F - 1.0F) * 4.0F;
        double targetY = this.ghast.getY() + (random.nextFloat() * 2.0F - 1.0F) * 2.0F;
        double targetZ = this.ghast.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 4.0F;
        if (this.ghast.getOwner() != null) {
            targetX = this.ghast.getOwner().getX() + (random.nextFloat() * 2.0F - 1.0F) * 4.0F;
            targetY = this.ghast.getOwner().getY() + (random.nextFloat() * 2.0F - 1.0F) * 2.0F;
            targetZ = this.ghast.getOwner().getZ() + (random.nextFloat() * 2.0F - 1.0F) * 4.0F;
        }
        //this.ghast.getLookControl().lookAt(targetX, targetY, targetZ, 10.0F, 40.0F);
        // Tell the Ghast's move controller to move to the target destination with a speed of 1.0.
        this.ghast.getMoveControl().moveTo(targetX, targetY, targetZ, 1.0D);
    }
}
