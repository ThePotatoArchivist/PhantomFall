package archives.tater.phantomfall.mixin.client;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LayerDefinition.class)
public interface TextureModelDataAccessor {
    @Accessor
    MeshDefinition getMesh();
}
