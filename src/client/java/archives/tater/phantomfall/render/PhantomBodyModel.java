package archives.tater.phantomfall.render;

import archives.tater.phantomfall.mixin.client.PhantomEntityModelAccessor;
import archives.tater.phantomfall.render.state.PhantomBodyRenderState;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.PhantomEntityModel;

class PhantomBodyModel extends Model<PhantomBodyRenderState> {
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
