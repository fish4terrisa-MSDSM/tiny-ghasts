package fish4terrisa.tinyghasts.mixin;

import fish4terrisa.tinyghasts.mixin.TinyGhastsMobEntityMixin;
import fish4terrisa.tinyghasts.TinyGhasts;
import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.passive.IronGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolemEntity.class)
public abstract class TinyGhastsIronGolemMixin extends TinyGhastsMobEntityMixin {
    @Inject(at = @At("HEAD"), method = "canTarget(Lnet/minecraft/entity/EntityType;)Z", cancellable = true)
    protected void IronGolemcanTarget(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (type == TinyGhasts.TINYGHAST) {
            cir.setReturnValue(false);
        }
    }
    @Override
    protected void MobEntitysetTarget(LivingEntity target, CallbackInfo ci) {
        if (target instanceof TinyGhastEntity) {
            ci.cancel();
        }
    }
}
