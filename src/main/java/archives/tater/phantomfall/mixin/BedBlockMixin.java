package archives.tater.phantomfall.mixin;

import archives.tater.phantomfall.PhantomFall;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @ModifyExpressionValue(
            method = "useWithoutItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;explodes()Z")
    )
    private boolean allowInsomniaOmen(boolean original, @Local(argsOnly = true) Player player) {
        return original && !PhantomFall.hasInsomniaOrOmen(player);
    }
}
