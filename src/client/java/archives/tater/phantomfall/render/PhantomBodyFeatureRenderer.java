package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomBodyComponent;
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

    private static final Identifier TEXTURE = new Identifier("textures/entity/phantom.png");

    public PhantomBodyFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> context, EntityModelLoader loader) {
        super(context);
        model = new PhantomEntityModel<>(loader.getModelPart(EntityModelLayers.PHANTOM));
        model.getChild(EntityModelPartNames.HEAD).ifPresent(modelPart -> modelPart.visible = false);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        var phantom = PhantomBodyComponent.KEY.get(entity).getPhantom();
        if (phantom == null) return;
        matrices.push();
        matrices.translate(0, 7 / 16.0, 3 / 16.0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        model.setAngles(phantom, limbAngle, limbDistance, entity.age + tickDelta, headYaw, headPitch);
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(TEXTURE)), light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrices.pop();
    }
}
