package fish4terrisa.tinyghasts.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface TinyGhastEntityAccessor {
    @Invoker
    void invokeSetWorld(World world);
}
