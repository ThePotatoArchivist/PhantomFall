package archives.tater.phantomfall;

import archives.tater.phantomfall.render.PhantomBodyFeatureRenderer;
import archives.tater.phantomfall.render.PhantomBodyModel;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import static archives.tater.phantomfall.PhantomFallAttachments.PHANTOM_DATA;

@SuppressWarnings("UnstableApiUsage")
public class PhantomFallClient implements ClientModInitializer {

	private static boolean perspectiveChanged = false;
	private static @Nullable CameraType savedPerspective = null;

	public static ModelLayerLocation PHANTOM_BODY_LAYER = new ModelLayerLocation(PhantomFall.id("phantom_body"), "main");

    public static RenderStateDataKey<PhantomBodyRenderState> PHANTOM_BODY = RenderStateDataKey.create(() -> "phantom_body");

    public static AttachmentType<PhantomBodyRenderState> PHANTOM_BODY_LAST = AttachmentRegistry.create(PhantomFall.id("phantom_body_last"));

	public static void savePerspective() {
		var options = Minecraft.getInstance().options;
		savedPerspective = options.getCameraType();
		options.setCameraType(CameraType.THIRD_PERSON_BACK);
	}

	public static void restorePerspective() {
		if (savedPerspective == null) return;
		var options = Minecraft.getInstance().options;
		if (options.getCameraType() == CameraType.THIRD_PERSON_BACK)
			options.setCameraType(savedPerspective);
		savedPerspective = null;
	}

	public static void clearPerspective() {
		savedPerspective = null;
	}

	@SuppressWarnings("unchecked")
    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityModelLayerRegistry.registerModelLayer(PHANTOM_BODY_LAYER, PhantomBodyModel::getTexturedModelData);

        ParticleFactoryRegistry.getInstance().register(PhantomFall.INSOMNIA_OMEN_PARTICLE, SpellParticle.Provider::new);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.PLAYER)
                registrationHelper.register(new PhantomBodyFeatureRenderer((RenderLayerParent<AvatarRenderState, PlayerModel>) entityRenderer, context.getModelSet()));
		});
		LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(player ->
				player.getData(PHANTOM_BODY) == null);
		ClientTickEvents.END_WORLD_TICK.register(clientWorld -> {
			if (!PhantomFall.CONFIG.client.changePerspective) return;
			var clientPlayer = Minecraft.getInstance().player;
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
