package fish4terrisa.tinyghasts.mixin;

import fish4terrisa.tinyghasts.mixin.TinyGhastsMobEntityMixin;
import fish4terrisa.tinyghasts.TinyGhasts;
import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.passive.SnowGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowGolemEntity.class)
public abstract class TinyGhastsSnowGolemMixin extends TinyGhastsMobEntityMixin {
    @Override
    protected void MobEntitysetTarget(LivingEntity target, CallbackInfo ci) {
        if (target instanceof TinyGhastEntity) {
            ci.cancel();
        }
    }
}
