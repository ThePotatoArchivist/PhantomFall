package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;

import static archives.tater.phantomfall.PhantomFallAttachments.PHANTOM_DATA;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isGliding()Z")
    )
    private boolean checkPhantom(PlayerEntity instance, Operation<Boolean> original) {
        return original.call(instance) && instance.hasAttached(PHANTOM_DATA);
    }
}
