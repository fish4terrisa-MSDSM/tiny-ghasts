package fish4terrisa.tinyghasts.entity.ai.control;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;

import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TinyGhastMoveControl extends MoveControl {
    private final TinyGhastEntity ghast;
    private int collisionCheckCooldown;

    public TinyGhastMoveControl(TinyGhastEntity ghast) {
        super(ghast);
        this.ghast = ghast;
    }

    @Override
    public void tick() {
        if (this.state != MoveControl.State.MOVE_TO) {
            return;
        }
        if (this.collisionCheckCooldown-- <= 0) {
            this.collisionCheckCooldown += this.ghast.getRandom().nextInt(5) + 2;
            Vec3d targetVec = new Vec3d(this.targetX - this.ghast.getX(), this.targetY - this.ghast.getY(), this.targetZ - this.ghast.getZ());
            double distance = targetVec.length();
            targetVec = targetVec.normalize();
            if (this.isCollision(targetVec, MathHelper.ceil(distance))) {
                this.ghast.setVelocity(this.ghast.getVelocity().add(targetVec.multiply(0.1)));
            } else {
                this.state = MoveControl.State.WAIT;
            }
        }
    }

    private boolean isCollision(Vec3d direction, int steps) {
        Box box = this.ghast.getBoundingBox();
        for (int i = 1; i < steps; ++i) {
            box = box.offset(direction);
            if (!this.ghast.getWorld().isSpaceEmpty(this.ghast, box)) {
                return false;
            }
        }
        return true;
    }
}
