package fish4terrisa.tinyghasts;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import fish4terrisa.tinyghasts.event.PlayerEventHandler;
import fish4terrisa.tinyghasts.entity.TinyGhastFireballEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public class TinyGhasts implements ModInitializer {

    /*
     * Registers our Tiny Ghast Entity under the ID "tiny-ghasts:tinyghast".
     *
     * The entity is registered under the SpawnGroup#CREATURE category.
     */
		public static final String MOD_ID = "tiny-ghasts";
		public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final EntityType<TinyGhastEntity> TINYGHAST = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("tiny-ghasts", "tinyghast"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TinyGhastEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.9f)).build(RegistryKey.
of(RegistryKeys.ENTITY_TYPE, Identifier.of("tiny-ghasts", "tinyghast")))
    );
    public static final EntityType<TinyGhastFireballEntity> TINYGHAST_FIREBALL = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("tiny-ghasts", "tinyghast_fireball"),
            FabricEntityTypeBuilder.<TinyGhastFireballEntity>create(SpawnGroup.MISC, TinyGhastFireballEntity::new).dimensions(EntityDimensions.fixed(0.25f, 0.25f)).trackRangeBlocks(4).trackedUpdateRate(10).build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("tiny-ghasts", "tinyghast_fireball")))
    );

    @Override
    public void onInitialize() {
			FabricDefaultAttributeRegistry.register(TINYGHAST, TinyGhastEntity.createMobAttributes());
      AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
        if (!world.isClient() && player instanceof PlayerEntity) {
            LastAttackedManager.lastAttacked.put(player.getUuid(), entity);
        }
            return ActionResult.PASS;
      });
      PlayerEventHandler.register();
    }

}
