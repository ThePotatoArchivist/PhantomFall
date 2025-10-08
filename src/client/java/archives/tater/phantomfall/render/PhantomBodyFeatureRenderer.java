package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomFallAttachments;
import archives.tater.phantomfall.PhantomFallClient;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PhantomBodyFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final PhantomBodyModel model;

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/phantom.png");

    public PhantomBodyFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, LoadedEntityModels loader) {
        super(context);
        model = new PhantomBodyModel(loader.getModelPart(PhantomFallClient.PHANTOM_BODY_LAYER));
        model.getRootPart().getChild(EntityModelPartNames.BODY).pitch = 0;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void updateState(PlayerLikeEntity player, PlayerEntityRenderState state) {
        var phantom = PhantomFallAttachments.getPhantom(player);

        if (phantom == null) return;

        var phantomState = new PhantomBodyRenderState();

        var velocityNormY = player.getVelocity().normalize().y;
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
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        var phantomState = state.getData(PhantomFallClient.PHANTOM_BODY);
        if (phantomState == null) return;
        var pitch = phantomState.pitch;
        var yaw = phantomState.yaw;
        var size = phantomState.size;

        matrices.push();
        matrices.translate(0, 0, 2 / 16.0); // Pivot point

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(pitch));

        matrices.translate(0, 0, -1 / 16.0); // Scaling center

        var scale = 1f + 0.15f * size;
        matrices.scale(scale, scale, scale);

        matrices.translate(1 / 32.0, -1 / 16.0, 8 / 16.0); // Model center

        queue.submitModel(model, phantomState, matrices, model.getLayer(TEXTURE), light, OverlayTexture.DEFAULT_UV, 0, null);

        matrices.pop();
    }

}
