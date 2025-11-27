package archives.tater.phantomfall.mixin.bettercombat;

import net.bettercombat.logic.TargetHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetHelper.class)
public class TargetHelperMixin {
    @Inject(
            method = "isAttackableMount",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void checkPhantom(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Phantom)
            cir.setReturnValue(true);
    }
}
