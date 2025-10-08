package archives.tater.phantomfall.mixin;

import archives.tater.phantomfall.PhantomFall;
import archives.tater.phantomfall.PhantomFallAttachments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @ModifyExpressionValue(
            method = "trySleep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;natural()Z")
    )
    private boolean allowInsomniaOmen(boolean original) {
        return original || PhantomFall.hasInsomniaOrOmen(this);
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void tickAttachments(CallbackInfo ci) {
        PhantomFallAttachments.tickAttachments((ServerPlayerEntity) (PlayerEntity) this);
    }
}
