package archives.tater.phantomfall.mixin;

import archives.tater.phantomfall.PhantomBodyComponent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isGliding()Z")
    )
    private boolean checkPhantom(PlayerEntity instance, Operation<Boolean> original) {
        return original.call(instance) && PhantomBodyComponent.KEY.get(instance).getPhantom() == null;
    }
}
