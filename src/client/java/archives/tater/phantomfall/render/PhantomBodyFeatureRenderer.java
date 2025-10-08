package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomFallAttachments;
import archives.tater.phantomfall.PhantomFallClient;
import archives.tater.phantomfall.mixin.client.ModelPartDataAccessor;
import archives.tater.phantomfall.mixin.client.PhantomEntityModelAccessor;
import archives.tater.phantomfall.mixin.client.TextureModelDataAccessor;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
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

        phantomState.bodyPitch = lastState.bodyPitch = lastState.bodyPitch + (pitch - lastState.bodyPitch) * 0.1f;
        phantomState.wingYaw = lastState.wingYaw = lastState.wingYaw + (yaw - lastState.wingYaw) * 0.1f;
        phantomState.size = phantom.getPhantomSize();

        state.setData(PhantomFallClient.PHANTOM_BODY, phantomState);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        var phantomState = state.getData(PhantomFallClient.PHANTOM_BODY);
        if (phantomState == null) return;
        var pitch = phantomState.bodyPitch;
        var yaw = phantomState.wingYaw;
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

    static class PhantomBodyModel extends Model<PhantomBodyRenderState> {
        private final ModelPart leftWingBase;
        private final ModelPart leftWingTip;
        private final ModelPart rightWingBase;
        private final ModelPart rightWingTip;

        public PhantomBodyModel(ModelPart modelPart) {
            super(modelPart, RenderLayer::getEntityCutoutNoCull);
            var delegate = (PhantomEntityModelAccessor) new PhantomEntityModel(modelPart);
            leftWingBase = delegate.getLeftWingBase();
            leftWingTip = delegate.getLeftWingTip();
            rightWingBase = delegate.getRightWingBase();
            rightWingTip = delegate.getRightWingTip();
        }

        @Override
        public void setAngles(PhantomBodyRenderState state) {
            super.setAngles(state);
            var yaw = state.wingYaw;
            leftWingBase.yaw = yaw;
            leftWingTip.yaw = yaw;
            rightWingBase.yaw = -yaw;
            rightWingTip.yaw = -yaw;
        }
    }

    public static TexturedModelData getTexturedModelData() {
        var data = PhantomEntityModel.getTexturedModelData();
        ((ModelPartDataAccessor) ((TextureModelDataAccessor) data).getData().getRoot().getChild(EntityModelPartNames.BODY)).phantomfall$getChildren().remove(EntityModelPartNames.HEAD);
        return data;
    }
}
