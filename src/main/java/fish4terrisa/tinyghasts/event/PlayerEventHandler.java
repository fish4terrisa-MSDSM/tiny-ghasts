package fish4terrisa.tinyghasts.event;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.Entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;
public class PlayerEventHandler {

    public static void register() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            // 'player' is the ServerPlayerEntity that changed worlds.
            // 'destination' is the ServerWorld they arrived in.
            teleportPetsToPlayer(player, destination);
        });

        // Event for when a player respawns
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // After respawn, the player entity is recreated. We use the new instance.
            teleportPetsToPlayer(newPlayer, (ServerWorld) newPlayer.getWorld());
        });

        // This event can help catch logins and some teleports
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            teleportPetsToPlayer(player, (ServerWorld) player.getWorld());
        });
    }

    /**
     * Iterates through all TinyGhastEntity instances across all server worlds
     * and teleports any owned by the specified player to their location.
     *
     * @param player The player to check for pet ownership.
     * @param targetWorld The world the player is now in.
     */
    private static void teleportPetsToPlayer(ServerPlayerEntity owner, ServerWorld targetWorld) {
        if (owner.getServer() == null) return;
        UUID ownerUuid = owner.getUuid();

        // Iterate through all loaded worlds on the server.
        for (ServerWorld world : owner.getServer().getWorlds()) {
            // CORRECTED LOGIC: Iterate through all entities in the world.
            for (Entity entity : world.iterateEntities()) {
                // Check if the entity is an instance of our TinyGhastEntity.
                if (entity instanceof TinyGhastEntity) {
                    TinyGhastEntity ghast = (TinyGhastEntity) entity;

                    // Check if its owner's UUID matches the player who triggered the event.
                    if (ghast.getOwnerUuid().isPresent() && ghast.getOwnerUuid().get().equals(ownerUuid)) {
                        // We found a pet! Teleport it to the owner's new world.
                        ghast.teleportToOwner(targetWorld);
                    }
                }
            }
        }
    }
}
