package fish4terrisa.tinyghasts;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import fish4terrisa.tinyghasts.event.PlayerEventHandler;
import fish4terrisa.tinyghasts.entity.TinyGhastFireballEntity;
import fish4terrisa.tinyghasts.block.TinyGhastShelterBlock;
import fish4terrisa.tinyghasts.block.entity.TinyGhastShelterBlockEntity;

import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.item.v1.FabricItem.Settings;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.block.entity.BlockEntityType;
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
//    private static Item registerBlockItem(String name, Block block) {
//        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name),
//                new BlockItem(block, new Item.Settings()));
//    }
//    private static Block registerBlock(String name, Block block) {
//        registerBlockItem(name, block);
//        return Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block);
//    }
//    public static final Block TINYGHAST_SHELTER = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "tinyghast_shelter"),
//            new TinyGhastShelterBlock(AbstractBlock.Settings.copy(Blocks.GLASS).registryKey(TINYGHAST_SHELTER_KEY)));
    public static final Block TINYGHAST_SHELTER = Blocks.register(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "tinyghast_shelter")), Block::new, AbstractBlock.Settings.copy(Blocks.GLASS).strength(-1));
    //Items.register(TINYGHAST_SHELTER);

    public static final BlockEntityType<TinyGhastShelterBlockEntity> TINYGHAST_SHELTER_BE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(MOD_ID, "tinyghast_shelter_be"),
          FabricBlockEntityTypeBuilder.create(TinyGhastShelterBlockEntity::new,
          TINYGHAST_SHELTER).build());


    @Override
    public void onInitialize() {
			FabricDefaultAttributeRegistry.register(TINYGHAST, TinyGhastEntity.createMobAttributes());
      PlayerEventHandler.register();
    }

}
