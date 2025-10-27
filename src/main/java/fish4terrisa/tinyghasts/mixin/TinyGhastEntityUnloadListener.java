package fish4terrisa.tinyghasts.mixin;

import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.util.math.ChunkPos;
import fish4terrisa.tinyghasts.TinyGhasts;
import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingStatus;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ServerEntityManager.class)
public abstract class TinyGhastEntityUnloadListener {
    @Shadow @Final
    SectionedEntityCache<EntityLike> cache;

    @Inject(method = "updateTrackingStatus(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/entity/EntityTrackingStatus;)V", at = @At("HEAD"))
    private void onUnload(ChunkPos chunkPos, EntityTrackingStatus trackingStatus, CallbackInfo ci) {
        try {
            if (!trackingStatus.shouldTick()) {
                var l = chunkPos.toLong();
                var sections = this.cache.getTrackingSections(l);
                var tinyghastsToCheck = new ArrayList<TinyGhastEntity>();
                sections.forEach(section -> {
                    section.stream().forEach(e -> {
                        if (e instanceof TinyGhastEntity tinyghast) {
                            tinyghastsToCheck.add(tinyghast);
                        }
                    });
                });

                for (var tinyghast : tinyghastsToCheck) {
                    if (tinyghast.getWorld() instanceof ServerWorld) {
                        tinyghast.teleportToOwner((ServerWorld) (tinyghast.getWorld()));
                    }
                }
            }
        } catch (Exception e) {
            TinyGhasts.LOGGER.error("Error processing chunk unload", e);
        }
    }
}
