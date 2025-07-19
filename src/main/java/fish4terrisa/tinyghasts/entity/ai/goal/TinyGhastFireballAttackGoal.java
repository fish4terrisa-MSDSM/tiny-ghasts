package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import fish4terrisa.tinyghasts.entity.TinyGhastFireballEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class TinyGhastFireballAttackGoal extends Goal {
    private final TinyGhastEntity ghast;
    private int attackCooldown;

    public TinyGhastFireballAttackGoal(TinyGhastEntity ghast) {
        this.ghast = ghast;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = this.ghast.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        super.start();
        this.ghast.setShooting(true);
        this.attackCooldown = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.ghast.setShooting(false);
        this.ghast.setTarget(null);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }


    @Override
    public void tick() {
        LivingEntity target = this.ghast.getTarget();
        if (target == null || !target.isAlive()) {
            this.ghast.setTarget(null);
            this.ghast.setShooting(false);
            return;
        }

        if (this.ghast.canSee(target)) {
            if (this.attackCooldown <= 0) {
                this.attackCooldown = 20; // Fire every 20 ticks
                World world = this.ghast.getWorld();
                if (!world.isClient) {
                    Vec3d rotation = this.ghast.getRotationVec(1.0F);
                    double d = target.getX() - this.ghast.getX();
                    double e = target.getBodyY(0.5) - this.ghast.getBodyY(0.5);
                    double f = target.getZ() - this.ghast.getZ();

                    TinyGhastFireballEntity fireballEntity = new TinyGhastFireballEntity(world, this.ghast, target, d, e, f);
                    fireballEntity.setPosition(this.ghast.getX() + rotation.x * 2.0D, this.ghast.getBodyY(0.5D) + 0.1D, this.ghast.getZ() + rotation.z * 2.0D);
                    world.spawnEntity(fireballEntity);
                }
            }
        }
        //this.ghast.getLookControl().lookAt(target, 10.0F, 10.0F);
        double deltaX = target.getX() - this.ghast.getX();
        double deltaY = target.getEyeY() - this.ghast.getEyeY();
        double deltaZ = target.getZ() - this.ghast.getZ();

        // Calculate the horizontal distance
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Calculate the required pitch (up/down rotation)
        // atan2 gives the angle in radians, which we convert to degrees
        float pitch = (float) (-(MathHelper.atan2(deltaY, horizontalDistance) * (180.0D / Math.PI)));

        // Calculate the required yaw (left/right rotation)
        float yaw = (float) (MathHelper.atan2(deltaZ, deltaX) * (180.0D / Math.PI)) - 90.0F;

        // Set the ghast's rotation directly.
        // Note: This results in an instantaneous, snappy turn.
        this.ghast.setPitch(pitch);
        this.ghast.setYaw(yaw);
        this.ghast.setHeadYaw(yaw); // Ensure the head model also turns correctly
        this.ghast.setShooting(this.attackCooldown < 10);
        this.attackCooldown--;
    }
}
