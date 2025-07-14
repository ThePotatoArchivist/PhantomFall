package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends FlyingEntity {
	protected PhantomEntityMixin(EntityType<? extends FlyingEntity> entityType, World world) {
		super(entityType, world);
	}

	@ModifyArg(
			method = "initialize",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;setPhantomSize(I)V")
	)
	private int defaultSize(int size) {
		return size == 0 ? 1 : size;
	}

	@ModifyExpressionValue(
			method = "onSizeChanged",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;getPhantomSize()I")
	)
	private int preventDamageIncrease(int original) {
		return 0;
	}
}
