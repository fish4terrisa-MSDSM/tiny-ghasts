package fish4terrisa.tinyghasts.renderer;

import fish4terrisa.tinyghasts.renderer.TinyGhastEntityRenderer;
import fish4terrisa.tinyghasts.block.entity.TinyGhastShelterBlockEntity;
import fish4terrisa.tinyghasts.TinyGhasts;
import fish4terrisa.tinyghasts.entity.TinyGhastEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.GhastEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.SpawnReason;

public class TinyGhastShelterBlockEntityRenderer implements BlockEntityRenderer<TinyGhastShelterBlockEntity> {
    private final EntityRenderDispatcher entityRenderDispatcher;


    public TinyGhastShelterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(TinyGhastShelterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d pos) {
        World world = entity.getWorld();
        if (world == null) {
            return;
        }
        Entity dummyGhast = TinyGhasts.TINYGHAST.create(entity.getWorld(), SpawnReason.EVENT);

        if (dummyGhast != null) {
            matrices.push();
            // Center and scale the ghast model inside the block
            matrices.translate(0.5, 0.5, 0.5);
            matrices.scale(0.4f, 0.4f, 0.4f);

            // Render the dummy entity
            entityRenderDispatcher.render(dummyGhast, 0.0, 0.0, 0.0, tickDelta, matrices, vertexConsumers, light);

            matrices.pop();
        }
    }
}
