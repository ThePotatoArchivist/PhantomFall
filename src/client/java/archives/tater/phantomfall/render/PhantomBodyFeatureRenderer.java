package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomFallAttachments;
import archives.tater.phantomfall.PhantomFallClient;
import archives.tater.phantomfall.mixin.client.ModelPartDataAccessor;
import archives.tater.phantomfall.mixin.client.PhantomEntityModelAccessor;
import archives.tater.phantomfall.mixin.client.TextureModelDataAccessor;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class PhantomBodyFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final PhantomEntityModel model;

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/phantom.png");

    public PhantomBodyFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, LoadedEntityModels loader) {
        super(context);
        model = new PhantomEntityModel(loader.getModelPart(PhantomFallClient.PHANTOM_BODY));
        model.getRootPart().getChild(EntityModelPartNames.BODY).pitch = 0;
    }

    public static void updateState(AbstractClientPlayerEntity player, PlayerEntityRenderState state) {
        var phantomState = ((PhantomBodyRenderState.Holder) state).phantomfall$getPhantomBodyData();
        var phantom = PhantomFallAttachments.getPhantom(player);

        if (phantom == null) {
            phantomState.hasPhantom = false;
            phantomState.pitch = 0f;
            phantomState.yaw = 0f;
            return;
        }

        phantomState.hasPhantom = true;

        var velocityNormY = player.getVelocity().normalize().y;
        var value = velocityNormY > 0 ? 0f : Math.pow(-velocityNormY, 1.5);

        var pitch = (float) (value * (Math.PI / 18) + value * Math.PI / 12);
        var yaw = (float) (value * (-Math.PI / 4) + value * Math.PI / 12);

        phantomState.pitch = phantomState.pitch + (pitch - phantomState.pitch) * 0.1f;
        phantomState.yaw = phantomState.yaw + (yaw - phantomState.yaw) * 0.1f;
        phantomState.size = phantom.getPhantomSize();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        var phantomState = ((PhantomBodyRenderState.Holder) state).phantomfall$getPhantomBodyData();
        if (!phantomState.hasPhantom) return;
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

        setAngles(model, pitch, yaw);
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(TEXTURE)), light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    private static void setAngles(PhantomEntityModel model, float pitch, float yaw) {
        var accessor = (PhantomEntityModelAccessor) model;

        accessor.getLeftWingBase().yaw = yaw;
        accessor.getLeftWingTip().yaw = yaw;
        accessor.getRightWingBase().yaw = -yaw;
        accessor.getRightWingTip().yaw = -yaw;
    }

    public static TexturedModelData getTexturedModelData() {
        var data = PhantomEntityModel.getTexturedModelData();
        ((ModelPartDataAccessor) ((TextureModelDataAccessor) data).getData().getRoot().getChild(EntityModelPartNames.BODY)).phantomfall$getChildren().remove(EntityModelPartNames.HEAD);
        return data;
    }
}
