package fish4terrisa.tinyghasts.entity.ai.goal;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.world.TeleportTarget;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class TeleportToOwnerGoal extends Goal {
    private final TinyGhastEntity ghast;
    private final double teleportDistance;

    public TeleportToOwnerGoal(TinyGhastEntity ghast, double teleportDistance) {
        this.ghast = ghast;
        this.teleportDistance = teleportDistance;
    }

    @Override
    public boolean canStart() {
        LivingEntity owner = this.ghast.getOwner();
        if (owner == null) {
            return false;
        }
        if (owner.getWorld() != this.ghast.getWorld()) {
            return true;
        }
        return this.ghast.squaredDistanceTo(owner) > teleportDistance * teleportDistance;
    }

    @Override
    public void start() {
        LivingEntity owner = this.ghast.getOwner();
        if (owner != null) {
            if (owner.getWorld() instanceof ServerWorld) {
                if (owner.getWorld() != this.ghast.getWorld()) {
                    TeleportTarget target = new TeleportTarget((ServerWorld) owner.getWorld(), new Vec3d(owner.getBlockPos().getX() + 0.5, owner.getBlockPos().getY(), owner.getBlockPos().getZ() + 0.5), Vec3d.ZERO, this.ghast.getYaw(), this.ghast.getPitch(), TeleportTarget.ADD_PORTAL_CHUNK_TICKET);
                    this.ghast.teleportTo(target);
                    return;
                }
            }
            BlockPos ownerPos = owner.getBlockPos();
            for (int i = 0; i < 10; ++i) {
                int j = this.ghast.getRandom().nextBetween(-4, 4);
                int k = this.ghast.getRandom().nextBetween(-4, 4);
                if (Math.abs(j) < 2 && Math.abs(k) < 2) continue;
                int l = this.ghast.getRandom().nextBetween(-5, 5);
                BlockPos targetPos = new BlockPos(ownerPos.getX() + j, ownerPos.getY() + l, ownerPos.getZ() + k);
                if (this.ghast.isLineOfSightClear(targetPos)) {
                    if (this.ghast.getWorld().isAir(targetPos)) {
                        this.ghast.teleport(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, false);
                        this.ghast.getNavigation().stop();
                        return;
                    }
                }
            }

            for (int i = 0; i < 10; ++i) {
                int j = this.ghast.getRandom().nextBetween(-4, 4);
                int k = this.ghast.getRandom().nextBetween(-4, 4);
                if (Math.abs(j) < 2 && Math.abs(k) < 2) continue;
                int l = this.ghast.getRandom().nextBetween(-5, 5);
                BlockPos targetPos = new BlockPos(ownerPos.getX() + j, ownerPos.getY() + l, ownerPos.getZ() + k);
                if (this.ghast.getWorld().isAir(targetPos)) {
                    this.ghast.teleport(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, false);
                    this.ghast.getNavigation().stop();
                    return;
                }
            }
        }
    }
}
