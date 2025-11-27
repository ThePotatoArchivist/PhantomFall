package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomFallAttachments;
import archives.tater.phantomfall.mixin.client.PhantomEntityModelAccessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class PhantomBodyFeatureRenderer extends RenderLayer<Player, PlayerModel<Player>> {
    private final PhantomModel<Phantom> model;

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/phantom.png");

    public PhantomBodyFeatureRenderer(RenderLayerParent<Player, PlayerModel<Player>> context, EntityModelSet loader) {
        super(context);
        model = new PhantomModel<>(loader.bakeLayer(ModelLayers.PHANTOM));
        model.getAnyDescendantWithName(PartNames.HEAD).ifPresent(modelPart -> modelPart.visible = false);
        model.getAnyDescendantWithName(PartNames.BODY).ifPresent(modelPart -> modelPart.xRot = 0);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, Player entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        var phantom = PhantomFallAttachments.getPhantom(entity);
        if (phantom == null) return;

        var velocityNormY = entity.getDeltaMovement().normalize().y;
        var value = velocityNormY > 0 ? 0f : Math.pow(-velocityNormY, 1.5);

        var pitch = (float) (value * (Math.PI / 18) + value * Math.PI / 12);
        var yaw = (float) (value * (-Math.PI / 4) + value * Math.PI / 12);

        if (entity instanceof AbstractClientPlayer clientPlayer) {
            pitch = clientPlayer.elytraRotX = clientPlayer.elytraRotX + (pitch - clientPlayer.elytraRotX) * 0.1F;
            yaw = clientPlayer.elytraRotY = clientPlayer.elytraRotY + (yaw - clientPlayer.elytraRotY) * 0.1F;
        }

        matrices.pushPose();
        matrices.translate(0, 0, 2 / 16.0); // Pivot point

        matrices.mulPose(Axis.XP.rotationDegrees(-90));
        matrices.mulPose(Axis.XP.rotation(pitch));

        matrices.translate(0, 0, -1 / 16.0); // Scaling center

        var scale = 1f + 0.15f * phantom.getPhantomSize();
        matrices.scale(scale, scale, scale);

        matrices.translate(1 / 32.0, -1 / 16.0, 8 / 16.0); // Model center

        setAngles(model, pitch, yaw);
        model.renderToBuffer(matrices, vertexConsumers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY);

        matrices.popPose();
    }

    private static void setAngles(PhantomModel<Phantom> model, float pitch, float yaw) {
        var accessor = (PhantomEntityModelAccessor) model;

        accessor.getLeftWingBase().yRot = yaw;
        accessor.getLeftWingTip().yRot = yaw;
        accessor.getRightWingBase().yRot = -yaw;
        accessor.getRightWingTip().yRot = -yaw;
    }
}
