package archives.tater.phantomfall.mixin;

import archives.tater.phantomfall.PhantomFall;
import archives.tater.phantomfall.PhantomFallAttachments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {

    public ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @ModifyExpressionValue(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;natural()Z")
    )
    private boolean allowInsomniaOmen(boolean original) {
        return original || PhantomFall.hasInsomniaOrOmen(this);
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void tickAttachments(CallbackInfo ci) {
        PhantomFallAttachments.tickAttachments((ServerPlayer) (Player) this);
    }
}
