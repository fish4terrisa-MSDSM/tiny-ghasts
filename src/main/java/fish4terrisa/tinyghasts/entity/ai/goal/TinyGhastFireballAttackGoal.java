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
            this.attackCooldown--;
            World world = this.ghast.getWorld();
            if (this.attackCooldown == 20 && !this.ghast.isSilent()) {
                world.syncWorldEvent(null, 1015, this.ghast.getBlockPos(), 0);
            }
            if (this.attackCooldown <= 0) {
                this.attackCooldown = 40; // Fire every 40 ticks
                if (!world.isClient) {
                    double e = 4.0;
                    Vec3d vec3d = this.ghast.getRotationVec(1.0f);
                    double f = target.getX() - this.ghast.getX();
                    double g = target.getBodyY(0.5) - this.ghast.getBodyY(0.5);
                    double h = target.getZ() - this.ghast.getZ();
                    if (!this.ghast.isSilent()) {
                        world.syncWorldEvent(null, 1016, this.ghast.getBlockPos(), 0);
                    }

                    TinyGhastFireballEntity fireballEntity = new TinyGhastFireballEntity(world, this.ghast, target, f, g, h);
                    fireballEntity.setPosition(this.ghast.getX() + vec3d.x * 2.0D, this.ghast.getBodyY(0.5D) + 0.1D, this.ghast.getZ() + vec3d.z * 2.0D);
                    world.spawnEntity(fireballEntity);
                }
            }
        }
        this.ghast.setShooting(this.attackCooldown < 10);
    }
}
