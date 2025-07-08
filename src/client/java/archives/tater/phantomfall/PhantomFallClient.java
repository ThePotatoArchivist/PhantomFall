package archives.tater.phantomfall;

import archives.tater.phantomfall.render.PhantomBodyFeatureRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PhantomFallClient implements ClientModInitializer {

	public static final PhantomFallClientConfig CONFIG = PhantomFallClientConfig.createToml(
			FabricLoader.getInstance().getConfigDir(),
			PhantomFall.MOD_ID,
			PhantomFall.MOD_ID,
			PhantomFallClientConfig.class
	);

	private static boolean perspectiveChanged = false;
	private static @Nullable Perspective savedPerspective = null;

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
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.PLAYER)
                registrationHelper.register(new PhantomBodyFeatureRenderer((FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>>) entityRenderer, context.getModelLoader()));
		});
		LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(player ->
				PhantomBodyComponent.KEY.get(player).getPhantom() == null);
		ClientTickEvents.END_WORLD_TICK.register(clientWorld -> {
			if (!CONFIG.changePerspective) return;
			var clientPlayer = MinecraftClient.getInstance().player;
			if (clientPlayer == null) return;
			if (PhantomBodyComponent.KEY.get(clientPlayer).getPhantom() != null) {
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
