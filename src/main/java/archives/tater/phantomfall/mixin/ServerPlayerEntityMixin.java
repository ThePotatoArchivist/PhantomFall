package archives.tater.phantomfall.mixin;

import archives.tater.phantomfall.PhantomFall;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @ModifyExpressionValue(
            method = "trySleep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;natural()Z")
    )
    private boolean allowInsomniaOmen(boolean original) {
        return original || PhantomFall.hasInsomniaOrOmen(this);
    }
}
