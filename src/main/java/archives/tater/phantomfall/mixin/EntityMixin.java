package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract double getY();

    @SuppressWarnings("ConstantValue")
    @ModifyArg(
            method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity$PositionUpdater;accept(Lnet/minecraft/entity/Entity;DDD)V"),
            index = 2
    )
    protected double updatePassengerPosition(double y, @Local(argsOnly = true, ordinal = 1) Entity passenger) {
        if (!((Object) this instanceof PhantomEntity)) return y;
        return getY() - passenger.getHeight();
    }
}
