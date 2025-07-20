package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static java.lang.Math.max;

@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends MobEntity {
	protected PhantomEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public abstract int getPhantomSize();

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
	private int reduceDamageIncrease(int original) {
		return max(original - 1, 0);
	}

	@SuppressWarnings("DataFlowIssue")
    @Inject(
			method = "onSizeChanged",
			at = @At("TAIL")
	)
	private void increaseHealth(CallbackInfo ci) {
		getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(16 + 4 * getPhantomSize());
	}
}
