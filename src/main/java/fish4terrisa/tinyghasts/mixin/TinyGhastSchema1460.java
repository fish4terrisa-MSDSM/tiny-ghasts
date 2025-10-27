package fish4terrisa.tinyghasts.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

// Code "inspired" by https://github.com/TheEpicBlock/PPeTP/blob/main/src/main/java/nl/theepicblock/ppetp/mixin/AddNbtToSchema.java
@Mixin(Schema1460.class)
public abstract class TinyGhastSchema1460 extends IdentifierNormalizingSchema {
    @Unique
    private static Schema SCHEMA;

    public TinyGhastSchema1460(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Inject(method = "registerTypes", at = @At("HEAD"))
    private void captureSchema(Schema schemax, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes, CallbackInfo ci) {
        SCHEMA = schemax;
    }

    @ModifyReturnValue(method = "method_5260", at = @At("RETURN"))
    private static TypeTemplate onRegister(TypeTemplate original) {
        return DSL.allWithRemainder(
                DSL.optional(DSL.field("TinyGhasts", DSL.list(
                        DSL.or(
                                // This is the new way of storing things
                                DSL.field("data", TypeReferences.ENTITY_TREE.in(SCHEMA)),
                                // But in previous versions of the mod the type was inserted directly
                                TypeReferences.ENTITY_TREE.in(SCHEMA)
                        )
                ))),
                original
        );
    }
}
