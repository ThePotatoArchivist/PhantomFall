package archives.tater.phantomfall;

import archives.tater.phantomfall.render.PhantomBodyFeatureRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

public class PhantomFallClient implements ClientModInitializer {
	@SuppressWarnings("unchecked")
    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.PLAYER)
                registrationHelper.register(new PhantomBodyFeatureRenderer((FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>>) entityRenderer, context.getModelLoader()));
		});
		LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(player ->
				PhantomBodyComponent.KEY.get(player).getPhantom() == null);

	}
}
