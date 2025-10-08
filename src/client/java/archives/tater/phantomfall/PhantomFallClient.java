package archives.tater.phantomfall;

import archives.tater.phantomfall.render.PhantomBodyFeatureRenderer;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import static archives.tater.phantomfall.PhantomFallAttachments.PHANTOM_DATA;

@SuppressWarnings("UnstableApiUsage")
public class PhantomFallClient implements ClientModInitializer {

	private static boolean perspectiveChanged = false;
	private static @Nullable Perspective savedPerspective = null;

	public static EntityModelLayer PHANTOM_BODY = new EntityModelLayer(PhantomFall.id("phantom_body"), "main");

	public static void savePerspective() {
		var options = MinecraftClient.getInstance().options;
		savedPerspective = options.getPerspective();
		options.setPerspective(Perspective.THIRD_PERSON_BACK);
	}

	public static void restorePerspective() {
		if (savedPerspective == null) return;
		var options = MinecraftClient.getInstance().options;
		if (options.getPerspective() == Perspective.THIRD_PERSON_BACK)
			options.setPerspective(savedPerspective);
		savedPerspective = null;
	}

	public static void clearPerspective() {
		savedPerspective = null;
	}

	@SuppressWarnings("unchecked")
    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityModelLayerRegistry.registerModelLayer(PHANTOM_BODY, PhantomBodyFeatureRenderer::getTexturedModelData);

        ParticleFactoryRegistry.getInstance().register(PhantomFall.INSOMNIA_OMEN_PARTICLE, SpellParticle.DefaultFactory::new);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.PLAYER)
                registrationHelper.register(new PhantomBodyFeatureRenderer((FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>) entityRenderer, context.getEntityModels()));
		});
		LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(player ->
				((PhantomBodyRenderState.Holder) player).phantomfall$getPhantomBodyData().hasPhantom);
		ClientTickEvents.END_WORLD_TICK.register(clientWorld -> {
			if (!PhantomFall.CONFIG.client.changePerspective) return;
			var clientPlayer = MinecraftClient.getInstance().player;
			if (clientPlayer == null) return;
			if (clientPlayer.hasAttached(PHANTOM_DATA)) {
				if (!perspectiveChanged) {
					savePerspective();
					perspectiveChanged = true;
				}
			} else {
				if (perspectiveChanged) {
					restorePerspective();
					perspectiveChanged = false;
				}
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			clearPerspective();
			perspectiveChanged = false;
		});
	}
}
