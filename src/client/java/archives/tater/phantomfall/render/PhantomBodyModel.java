package archives.tater.phantomfall.render;

import archives.tater.phantomfall.mixin.client.ModelPartDataAccessor;
import archives.tater.phantomfall.mixin.client.TextureModelDataAccessor;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PhantomEntityModel;

public class PhantomBodyModel extends Model<PhantomBodyRenderState> {
    private static final String TAIL_BASE = "tail_base";
    private static final String TAIL_TIP = "tail_tip";
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;

    public PhantomBodyModel(ModelPart modelPart) {
        super(modelPart, RenderLayer::getEntityCutoutNoCull);
        var body = modelPart.getChild(EntityModelPartNames.BODY);
        var tailBase = body.getChild(TAIL_BASE);
        var tailTip = tailBase.getChild(TAIL_TIP);
        leftWingBase = body.getChild(EntityModelPartNames.LEFT_WING_BASE);
        leftWingTip = leftWingBase.getChild(EntityModelPartNames.LEFT_WING_TIP);
        rightWingBase = body.getChild(EntityModelPartNames.RIGHT_WING_BASE);
        rightWingTip = rightWingBase.getChild(EntityModelPartNames.RIGHT_WING_TIP);
    }

    public static TexturedModelData getTexturedModelData() {
        var data = PhantomEntityModel.getTexturedModelData();
        ((ModelPartDataAccessor) ((TextureModelDataAccessor) data).getData().getRoot().getChild(EntityModelPartNames.BODY)).phantomfall$getChildren().remove(EntityModelPartNames.HEAD);
        return data;
    }

    @Override
    public void setAngles(PhantomBodyRenderState state) {
        super.setAngles(state);
        var yaw = state.yaw;
        leftWingBase.yaw = yaw;
        leftWingTip.yaw = yaw;
        rightWingBase.yaw = -yaw;
        rightWingTip.yaw = -yaw;
    }
}
