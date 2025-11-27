package archives.tater.phantomfall.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobEntityMixin {
    @Inject(
            method = "getControllingPassenger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void noPhantomController(CallbackInfoReturnable<LivingEntity> cir) {
        if ((Object) this instanceof Phantom) cir.setReturnValue(null);
    }
}
