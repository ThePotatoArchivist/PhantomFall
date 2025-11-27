package archives.tater.phantomfall.mixin.client;

import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PhantomModel.class)
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
