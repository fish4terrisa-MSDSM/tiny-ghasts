package fish4terrisa.tinyghasts;

import net.fabricmc.api.ClientModInitializer;
import fish4terrisa.tinyghasts.renderer.TinyGhastEntityRenderer;
import fish4terrisa.tinyghasts.renderer.TinyGhastShelterBlockEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.GhastEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class TinyGhastsClient implements ClientModInitializer {
	public static final EntityModelLayer TINYGHAST_LAYER = new EntityModelLayer(Identifier.of("tiny-ghasts", "tinyghast"), "main");
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(TinyGhasts.TINYGHAST, (context) -> {
            return new TinyGhastEntityRenderer(context);
		});
		EntityRendererRegistry.register(TinyGhasts.TINYGHAST_FIREBALL, (context) -> {
						return new FlyingItemEntityRenderer(context);
		});
		EntityModelLayerRegistry.registerModelLayer(TINYGHAST_LAYER, GhastEntityModel::getTexturedModelData);

		BlockEntityRendererFactories.register(TinyGhasts.TINYGHAST_SHELTER_BE, TinyGhastShelterBlockEntityRenderer::new);

	}
}
