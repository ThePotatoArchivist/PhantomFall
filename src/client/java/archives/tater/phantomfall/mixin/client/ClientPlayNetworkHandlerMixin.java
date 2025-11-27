package archives.tater.phantomfall.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @WrapWithCondition(
            method = "handleSetEntityPassengersPacket",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V")
    )
    private boolean checkPhantom(Gui instance, Component message, boolean tinted, @Local(ordinal = 0) Entity entity) {
        return !(entity instanceof Phantom);
    }

    @WrapWithCondition(
            method = "handleSetEntityPassengersPacket",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GameNarrator;sayNow(Lnet/minecraft/network/chat/Component;)V")
    )
    private boolean checkPhantom(GameNarrator instance, Component text, @Local(ordinal = 0) Entity entity) {
        return !(entity instanceof Phantom);
    }
}
