package archives.tater.phantomfall.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GameRules.class)
public class GameRulesMixin {
    @ModifyArg(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=doInsomnia")
            ),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules$BooleanRule;create(Z)Lnet/minecraft/world/GameRules$Type;", ordinal = 0),
            index = 0
    )
    private static boolean insomniaOffDefault(boolean initialValue) {
        return false;
    }
}
