package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static archives.tater.phantomfall.PhantomFallAttachments.PHANTOM_DATA;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isFallFlying()Z")
    )
    private boolean checkPhantom(Player instance, Operation<Boolean> original) {
        return original.call(instance) && instance.hasAttached(PHANTOM_DATA);
    }
}
