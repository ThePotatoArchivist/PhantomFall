package archives.tater.phantomfall.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.level.gamerules.GameRules;

@Mixin(GameRules.class)
public class GameRulesMixin {
    @Definition(id = "SPAWN_PHANTOMS", field = "Lnet/minecraft/world/level/gamerules/GameRules;SPAWN_PHANTOMS:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Definition(id = "registerBoolean", method = "Lnet/minecraft/world/level/gamerules/GameRules;registerBoolean(Ljava/lang/String;Lnet/minecraft/world/level/gamerules/GameRuleCategory;Z)Lnet/minecraft/world/level/gamerules/GameRule;")
    @Expression("SPAWN_PHANTOMS = @(registerBoolean(?, ?, ?))")
    @ModifyArg(
            method = "<clinit>",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            index = 2
    )
    private static boolean insomniaOffDefault(boolean initialValue) {
        return false;
    }
}
