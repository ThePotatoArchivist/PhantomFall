package archives.tater.phantomfall.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Mixin(PartDefinition.class)
public interface ModelPartDataAccessor {
    @Accessor("children")
    Map<String, PartDefinition> phantomfall$getChildren();
}
