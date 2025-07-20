package archives.tater.phantomfall.mixin.client;

import archives.tater.phantomfall.render.state.PhantomBodyRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements PhantomBodyRenderState.Holder {
    @Unique
    private final PhantomBodyRenderState phantomBodyRenderState = new PhantomBodyRenderState();

    @Override
    public PhantomBodyRenderState phantomfall$getPhantomBodyData() {
        return phantomBodyRenderState;
    }
}
