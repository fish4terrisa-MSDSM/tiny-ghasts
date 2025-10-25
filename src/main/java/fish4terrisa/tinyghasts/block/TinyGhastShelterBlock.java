package fish4terrisa.tinyghasts.block;

import fish4terrisa.tinyghasts.block.entity.TinyGhastShelterBlockEntity;
import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import fish4terrisa.tinyghasts.TinyGhasts;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.Uuids;

import java.util.Optional;
import java.util.UUID;

public class TinyGhastShelterBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final MapCodec<TinyGhastShelterBlock> CODEC = TinyGhastShelterBlock.createCodec(TinyGhastShelterBlock::new);

    public TinyGhastShelterBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // We need this for the custom renderer to work and for transparency
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TinyGhastShelterBlockEntity(pos, state);
    }

    @Override
	  protected MapCodec<? extends BlockWithEntity> getCodec() {
		    return CODEC;
	  }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof TinyGhastShelterBlockEntity shelterEntity) {
                NbtCompound ghastData = shelterEntity.getGhastData();
                if (ghastData == null || ghastData.isEmpty()) return ActionResult.PASS;
                LazyEntityReference lazyEntityReference = LazyEntityReference.fromNbtOrPlayerName(ghastData, "Owner", world);
                // Check if the interacting player is the owner
                if (lazyEntityReference != null) {
                    LivingEntity owner = lazyEntityReference.resolve(lazyEntityReference, world, LivingEntity.class);
                    if (player != owner) {
                        return ActionResult.FAIL; // Not the owner
                    }
                }

                ItemStack playerStack = player.getStackInHand(Hand.MAIN_HAND);
                if (playerStack.getItem() == Items.TOTEM_OF_UNDYING) {
                    // Revive the Ghast
                    TinyGhastEntity ghast = TinyGhasts.TINYGHAST.create(world, SpawnReason.EVENT);
                    if (ghast != null) {
                        ghast.readCustomDataFromNbt(ghastData);
                        ghast.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, player.getYaw(), 0.0F);
                        world.spawnEntity(ghast);

                        // Clear the block and its entity
                        world.removeBlock(pos, false);

                        // Replace lava bucket with empty bucket
                        if (!player.getAbilities().creativeMode) {
                            player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        }
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return ActionResult.PASS;
    }
}
