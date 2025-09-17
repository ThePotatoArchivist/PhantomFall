package archives.tater.phantomfall.model;

import archives.tater.phantomfall.mixin.client.ModelPartDataAccessor;
import archives.tater.phantomfall.mixin.client.PhantomEntityModelAccessor;
import archives.tater.phantomfall.mixin.client.TextureModelDataAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.render.entity.state.PhantomEntityRenderState;

public class PhantomBodyModel extends PhantomEntityModel {
    public PhantomBodyModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setAngles(PhantomEntityRenderState phantomEntityRenderState) {
        resetTransforms();

        var accessor = (PhantomEntityModelAccessor) this;
        // hijacking this field
        var yaw = phantomEntityRenderState.wingFlapProgress;

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
