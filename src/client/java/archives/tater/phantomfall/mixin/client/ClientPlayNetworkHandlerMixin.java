package archives.tater.phantomfall.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @WrapWithCondition(
            method = "onEntityPassengersSet",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V")
    )
    private boolean checkPhantom(InGameHud instance, Text message, boolean tinted, @Local(ordinal = 0) Entity entity) {
        return !(entity instanceof PhantomEntity);
    }

    @WrapWithCondition(
            method = "onEntityPassengersSet",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/NarratorManager;narrate(Lnet/minecraft/text/Text;)V")
    )
    private boolean checkPhantom(NarratorManager instance, Text text, @Local(ordinal = 0) Entity entity) {
        return !(entity instanceof PhantomEntity);
    }
}
