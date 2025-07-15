package archives.tater.phantomfall.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Inject(
            method = "getControllingPassenger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void noPhantomController(CallbackInfoReturnable<LivingEntity> cir) {
        if ((Object) this instanceof PhantomEntity) cir.setReturnValue(null);
    }
}
