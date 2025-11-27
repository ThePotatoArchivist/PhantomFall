package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static java.lang.Math.max;

@Mixin(Phantom.class)
public abstract class PhantomEntityMixin extends Mob {
	protected PhantomEntityMixin(EntityType<? extends Mob> entityType, Level world) {
		super(entityType, world);
	}

	@Shadow public abstract int getPhantomSize();

	@ModifyArg(
			method = "finalizeSpawn",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;setPhantomSize(I)V")
	)
	private int defaultSize(int size) {
		return size == 0 ? 1 : size;
	}

	@ModifyExpressionValue(
			method = "updatePhantomSizeInfo",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;getPhantomSize()I")
	)
	private int reduceDamageIncrease(int original) {
		return max(original - 1, 0);
	}

	@SuppressWarnings("DataFlowIssue")
    @Inject(
			method = "updatePhantomSizeInfo",
			at = @At("TAIL")
	)
	private void increaseHealth(CallbackInfo ci) {
		getAttribute(Attributes.MAX_HEALTH).setBaseValue(16 + 4 * getPhantomSize());
	}
}
