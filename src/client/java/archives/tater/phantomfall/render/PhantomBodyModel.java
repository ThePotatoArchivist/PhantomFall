package archives.tater.phantomfall.render;

import archives.tater.phantomfall.mixin.client.ModelPartDataAccessor;
import archives.tater.phantomfall.mixin.client.TextureModelDataAccessor;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class PhantomBodyModel extends Model<PhantomBodyRenderState> {
    private static final String TAIL_BASE = "tail_base";
    private static final String TAIL_TIP = "tail_tip";
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;

    public PhantomBodyModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entityCutoutNoCull);
        var body = modelPart.getChild(PartNames.BODY);
        var tailBase = body.getChild(TAIL_BASE);
        var tailTip = tailBase.getChild(TAIL_TIP);
        leftWingBase = body.getChild(PartNames.LEFT_WING_BASE);
        leftWingTip = leftWingBase.getChild(PartNames.LEFT_WING_TIP);
        rightWingBase = body.getChild(PartNames.RIGHT_WING_BASE);
        rightWingTip = rightWingBase.getChild(PartNames.RIGHT_WING_TIP);
    }

    public static LayerDefinition getTexturedModelData() {
        var data = PhantomModel.createBodyLayer();
        ((ModelPartDataAccessor) ((TextureModelDataAccessor) data).getMesh().getRoot().getChild(PartNames.BODY)).phantomfall$getChildren().remove(PartNames.HEAD);
        return data;
    }

    @Override
    public void setupAnim(PhantomBodyRenderState state) {
        super.setupAnim(state);
        var yaw = state.yaw;
        leftWingBase.yRot = yaw;
        leftWingTip.yRot = yaw;
        rightWingBase.yRot = -yaw;
        rightWingTip.yRot = -yaw;
    }
}
