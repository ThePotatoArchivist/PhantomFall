package archives.tater.phantomfall.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BipedEntityRenderer.class)
public class LivingEntityRendererMixin {
	@WrapOperation(
			method = "updateBipedRenderState",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasVehicle()Z", ordinal = 0)
	)
	private static boolean preventSittingPose(LivingEntity instance, Operation<Boolean> original) {
		return original.call(instance) && !(instance.getVehicle() instanceof PhantomEntity);
	}
}
