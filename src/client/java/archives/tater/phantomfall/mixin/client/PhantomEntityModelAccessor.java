package archives.tater.phantomfall.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PhantomEntityModel.class)
public interface PhantomEntityModelAccessor {
    @Accessor
    ModelPart getLeftWingBase();
    @Accessor
    ModelPart getLeftWingTip();
    @Accessor
    ModelPart getRightWingBase();
    @Accessor
    ModelPart getRightWingTip();
}
