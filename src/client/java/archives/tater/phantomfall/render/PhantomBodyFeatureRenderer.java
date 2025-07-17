package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomBodyComponent;
import archives.tater.phantomfall.mixin.client.PhantomEntityModelAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class PhantomBodyFeatureRenderer extends FeatureRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {
    private final PhantomEntityModel<PhantomEntity> model;

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/phantom.png");

    public PhantomBodyFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> context, EntityModelLoader loader) {
        super(context);
        model = new PhantomEntityModel<>(loader.getModelPart(EntityModelLayers.PHANTOM));
        model.getChild(EntityModelPartNames.HEAD).ifPresent(modelPart -> modelPart.visible = false);
        model.getChild(EntityModelPartNames.BODY).ifPresent(modelPart -> modelPart.pitch = 0);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        var phantom = PhantomBodyComponent.KEY.get(entity).getPhantom();
        if (phantom == null) return;

        var velocityNormY = entity.getVelocity().normalize().y;
        var value = velocityNormY > 0 ? 0f : Math.pow(-velocityNormY, 1.5);

        var pitch = (float) (value * (Math.PI / 18) + value * Math.PI / 12);
        var yaw = (float) (value * (-Math.PI / 4) + value * Math.PI / 12);

        matrices.push();
        matrices.translate(0, 0, 2 / 16.0); // Pivot point

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(pitch));

        matrices.translate(0, 0, -1 / 16.0); // Scaling center

        var scale = 1f + 0.15f * phantom.getPhantomSize();
        matrices.scale(scale, scale, scale);

        matrices.translate(1 / 32.0, -1 / 16.0, 8 / 16.0); // Model center

        setAngles(model, pitch, yaw);
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(TEXTURE)), light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    private static void setAngles(PhantomEntityModel<PhantomEntity> model, float pitch, float yaw) {
        var accessor = (PhantomEntityModelAccessor) model;

        accessor.getLeftWingBase().yaw = yaw;
        accessor.getLeftWingTip().yaw = yaw;
        accessor.getRightWingBase().yaw = -yaw;
        accessor.getRightWingTip().yaw = -yaw;
    }
}
