package fish4terrisa.tinyghasts.renderer;

import fish4terrisa.tinyghasts.entity.TinyGhastEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.GhastEntityRenderer;
import net.minecraft.client.render.entity.state.GhastEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.util.Identifier;

public class TinyGhastEntityRenderer extends GhastEntityRenderer {

    public TinyGhastEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        // Adjust the shadow size here to be smaller than a normal ghast.
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(GhastEntityRenderState ghastEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        // scale the entity down
        matrixStack.push();
        matrixStack.scale(0.20f, 0.20f, 0.20f); // Makes the ghast model half the size

        super.render(ghastEntityRenderState, matrixStack, vertexConsumerProvider, i);

        matrixStack.pop();
    }
}
