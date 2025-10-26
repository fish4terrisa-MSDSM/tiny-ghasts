package fish4terrisa.tinyghasts.mixin;

import fish4terrisa.tinyghasts.TinyGhasts;
import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.passive.IronGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class TinyGhastsMobEntityMixin {
    @Inject(at = @At("HEAD"), method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", cancellable = true)
    protected void MobEntitysetTarget(LivingEntity target, CallbackInfo ci) {
    }
}
