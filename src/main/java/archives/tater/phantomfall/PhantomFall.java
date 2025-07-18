package archives.tater.phantomfall;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.SpawnHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class PhantomFall implements ModInitializer {
	public static final String MOD_ID = "phantomfall";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final PhantomFallConfig CONFIG = PhantomFallConfig.createToml(
			FabricLoader.getInstance().getConfigDir(),
			MOD_ID,
			MOD_ID,
			PhantomFallConfig.class
	);

	public static final TagKey<DamageType> PHANTOM_PICKUP = TagKey.of(RegistryKeys.DAMAGE_TYPE, id("phantom_pickup"));

	public static boolean canWearPhantom(PlayerEntity player) {
		var chestEquipment = player.getEquippedStack(EquipmentSlot.CHEST).getItem();
		return !(chestEquipment instanceof ElytraItem) && !(chestEquipment instanceof FabricElytraItem);
	}

	public static List<Integer> distributeSizes(int value, Random random) {
		var sizes = new ArrayList<Integer>();
		var remaining = value;
		while (remaining > 0) {
			var size = min(random.nextBetween(1, CONFIG.server.maxPhantomSize), remaining);
			sizes.add(size); // Phantom size starts at 0
			remaining -= size;
		}
		return sizes;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (entity.isFallFlying()) return;
            if (!source.isIn(PHANTOM_PICKUP)) return;
			var attacker = source.getAttacker();
			if (!(attacker instanceof PhantomEntity)) return;
			if (entity.getVehicle() instanceof PhantomEntity) return;
			if ((entity instanceof PlayerEntity) && PhantomBodyComponent.KEY.get(entity).getPhantom() != null) return;
            entity.startRiding(attacker);
        });
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof PhantomEntity phantom) || !(entity.getFirstPassenger() instanceof PlayerEntity player)) return true;

			player.dismountVehicle();

            if (damageSource.getAttacker() != player || !canWearPhantom(player) || EntityElytraEvents.CUSTOM.invoker().useCustomElytra(entity, false)) return true;

            phantom.setHealth(1);
            PhantomBodyComponent.KEY.get(player).setPhantomFrom(phantom);
            phantom.discard();
            player.startFallFlying();

            return false;
        });
		EntityElytraEvents.CUSTOM.register((entity, tickElytra) ->
				entity instanceof PlayerEntity player && PhantomBodyComponent.KEY.get(player).getPhantom() != null);

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			var world = entity.getWorld();
			if (!(world instanceof ServerWorld serverWorld)) return;
			if (!(entity instanceof PlayerEntity player)) return;
			var phantomsSpawned = PhantomsSpawnedComponent.KEY.get(player);
			if (!phantomsSpawned.canSpawnPhantoms()) return;
			world.calculateAmbientDarkness(); // day/night is based on ambient light which normally only updates every tick
			if (!world.isNight()) {
				phantomsSpawned.resetAmount();
				return;
			}
			if (!world.isSkyVisible(entity.getBlockPos())) return;

			var spawnedPhantoms = phantomsSpawned.getAmount();
			var random = world.getRandom();

			if (random.nextFloat() > CONFIG.server.baseSpawnChance + CONFIG.server.spawnChanceIncrease * spawnedPhantoms) return;

			var success = false;

			for (var size : distributeSizes(min(spawnedPhantoms + 1, CONFIG.server.maxSpawnScore), random)) {
				var blockPos = entity.getBlockPos().up(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
				if (!SpawnHelper.isClearForSpawn(world, blockPos, world.getBlockState(blockPos), world.getFluidState(blockPos), EntityType.PHANTOM)) continue;
				var phantom = EntityType.PHANTOM.create(world);
				if (phantom == null) continue;
				phantom.refreshPositionAndAngles(blockPos, 0.0F, 0.0F);
				phantom.initialize(serverWorld, world.getLocalDifficulty(entity.getBlockPos()), SpawnReason.NATURAL, null);
				phantom.setPhantomSize(size);
				phantom.setHealth(phantom.getMaxHealth());
				serverWorld.spawnEntityAndPassengers(phantom);
				success = true;
			}

			if (success) {
				player.playSoundToPlayer(SoundEvents.ENTITY_PHANTOM_AMBIENT, player.getSoundCategory(), 1f, 0.6f);
				phantomsSpawned.increaseAmount();
				phantomsSpawned.setCooldown();
			}
		});
	}
}
