package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    @WrapOperation(
            method = "raycast",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRootVehicle()Lnet/minecraft/entity/Entity;", ordinal = 1)
    )
    private static Entity allowHitPhantom(Entity instance, Operation<Entity> original) {
        var vehicle = original.call(instance);
        if (vehicle instanceof PhantomEntity) {
            return instance;
        }
        return vehicle;
    }
}
