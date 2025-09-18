package archives.tater.phantomfall.mixin;

import archives.tater.phantomfall.PhantomFall;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @ModifyExpressionValue(
            method = "onUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z")
    )
    private boolean allowInsomniaOmen(boolean original, @Local(argsOnly = true) PlayerEntity player) {
        return original || PhantomFall.hasInsomniaOrOmen(player);
    }
}
