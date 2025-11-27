package archives.tater.phantomfall.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidMobRenderer.class)
public class LivingEntityRendererMixin {
	@WrapOperation(
			method = "extractHumanoidRenderState",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isPassenger()Z", ordinal = 0)
	)
	private static boolean preventSittingPose(LivingEntity instance, Operation<Boolean> original) {
		return original.call(instance) && !(instance.getVehicle() instanceof Phantom);
	}
}
