package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract double getY();

    @SuppressWarnings("ConstantValue")
    @ModifyArg(
            method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity$MoveFunction;accept(Lnet/minecraft/world/entity/Entity;DDD)V"),
            index = 2
    )
    protected double updatePassengerPosition(double y, @Local(argsOnly = true, ordinal = 1) Entity passenger) {
        if (!((Object) this instanceof Phantom)) return y;
        return getY() - passenger.getBbHeight();
    }
}
