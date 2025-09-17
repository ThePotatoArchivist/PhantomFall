package archives.tater.phantomfall.render;

import archives.tater.phantomfall.PhantomBodyComponent;
import archives.tater.phantomfall.PhantomFallClient;
import archives.tater.phantomfall.model.PhantomBodyModel;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PhantomEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class PhantomBodyFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final PhantomBodyModel model;

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/phantom.png");

    public PhantomBodyFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, LoadedEntityModels loader) {
        super(context);
        model = new PhantomBodyModel(loader.getModelPart(PhantomFallClient.PHANTOM_BODY));
        model.getRootPart().getChild(EntityModelPartNames.BODY).pitch = 0;
    }

    public static void updateState(PlayerLikeEntity player, PlayerEntityRenderState state) {
        var phantomState = ((PhantomBodyRenderState.Holder) state).phantomfall$getPhantomBodyData();
        var phantomComponent = PhantomBodyComponent.KEY.get(player);
        var phantom = phantomComponent.getPhantom();

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

        phantomState.pitch = phantomComponent.lastPitch = phantomComponent.lastPitch + (pitch - phantomComponent.lastPitch) * 0.1f;
        phantomState.yaw = phantomComponent.lastYaw = phantomComponent.lastYaw + (yaw - phantomComponent.lastYaw) * 0.1f;
        phantomState.size = phantom.getPhantomSize();
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
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

        var renderState = new PhantomEntityRenderState();
        renderState.entityType = EntityType.PHANTOM;
        renderState.wingFlapProgress = yaw;
        queue.submitModel(model, renderState, matrices, model.getLayer(TEXTURE), light, OverlayTexture.DEFAULT_UV, 0, null);

        matrices.pop();
    }

}
