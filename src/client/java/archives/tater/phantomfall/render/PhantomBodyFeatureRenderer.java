package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomFallAttachments;
import archives.tater.phantomfall.PhantomFallClient;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Avatar;

public class PhantomBodyFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final PhantomBodyModel model;

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/phantom.png");

    public PhantomBodyFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> context, EntityModelSet loader) {
        super(context);
        model = new PhantomBodyModel(loader.bakeLayer(PhantomFallClient.PHANTOM_BODY_LAYER));
        model.root().getChild(PartNames.BODY).xRot = 0;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void updateState(Avatar player, AvatarRenderState state) {
        var phantom = PhantomFallAttachments.getPhantom(player);

        if (phantom == null) return;

        var phantomState = new PhantomBodyRenderState();

        var velocityNormY = player.getDeltaMovement().normalize().y;
        var value = velocityNormY > 0 ? 0f : Math.pow(-velocityNormY, 1.5);

        var pitch = (float) (value * (Math.PI / 18) + value * Math.PI / 12);
        var yaw = (float) (value * (-Math.PI / 4) + value * Math.PI / 12);

        var lastState = player.getAttachedOrCreate(PhantomFallClient.PHANTOM_BODY_LAST, PhantomBodyRenderState::new);

        phantomState.pitch = lastState.pitch = lastState.pitch + (pitch - lastState.pitch) * 0.1f;
        phantomState.yaw = lastState.yaw = lastState.yaw + (yaw - lastState.yaw) * 0.1f;
        phantomState.size = phantom.getPhantomSize();

        state.setData(PhantomFallClient.PHANTOM_BODY, phantomState);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
        var phantomState = state.getData(PhantomFallClient.PHANTOM_BODY);
        if (phantomState == null) return;
        var pitch = phantomState.pitch;
        var yaw = phantomState.yaw;
        var size = phantomState.size;

        matrices.pushPose();
        matrices.translate(0, 0, 2 / 16.0); // Pivot point

        matrices.mulPose(Axis.XP.rotationDegrees(-90));
        matrices.mulPose(Axis.XP.rotation(pitch));

        matrices.translate(0, 0, -1 / 16.0); // Scaling center

        var scale = 1f + 0.15f * size;
        matrices.scale(scale, scale, scale);

        matrices.translate(1 / 32.0, -1 / 16.0, 8 / 16.0); // Model center

        queue.submitModel(model, phantomState, matrices, model.renderType(TEXTURE), light, OverlayTexture.NO_OVERLAY, 0, null);

        matrices.popPose();
    }

}
