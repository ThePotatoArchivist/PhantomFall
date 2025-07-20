package archives.tater.phantomfall.mixin.client;

import net.minecraft.client.model.ModelPartData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelPartData.class)
public interface ModelPartDataAccessor {
    @Accessor
    Map<String, ModelPartData> getChildren();
}
