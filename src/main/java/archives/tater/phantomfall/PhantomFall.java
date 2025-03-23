package archives.tater.phantomfall;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhantomFall implements ModInitializer {
	public static final String MOD_ID = "phantomfall";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			if (entity instanceof PhantomEntity phantom && entity.getFirstPassenger() instanceof PlayerEntity player) {
				phantom.setHealth(1);
				PhantomBodyComponent.KEY.get(player).setPhantomFrom(phantom);
				phantom.discard();
				player.startFallFlying();
				return false;
			}
			return true;
		});
		EntityElytraEvents.CUSTOM.register((entity, tickElytra) ->
				entity instanceof PlayerEntity player && PhantomBodyComponent.KEY.get(player).getPhantom() != null);
		// TODO prevent dismount
	}
}
