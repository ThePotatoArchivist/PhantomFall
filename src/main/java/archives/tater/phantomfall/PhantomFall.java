package archives.tater.phantomfall;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.SpawnHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static archives.tater.phantomfall.PhantomFallAttachments.*;
import static java.lang.Math.min;
import static net.minecraft.entity.LivingEntity.canGlideWith;

@SuppressWarnings("UnstableApiUsage")
public class PhantomFall implements ModInitializer {
	public static final String MOD_ID = "phantomfall";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static SoundEvent registerSound(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

	public static final PhantomFallConfig CONFIG = PhantomFallConfig.createToml(
			FabricLoader.getInstance().getConfigDir(),
			MOD_ID,
			MOD_ID,
			PhantomFallConfig.class
	);

	public static final TagKey<DamageType> PHANTOM_PICKUP = TagKey.of(RegistryKeys.DAMAGE_TYPE, id("phantom_pickup"));

    public static final SimpleParticleType INSOMNIA_OMEN_PARTICLE = Registry.register(
            Registries.PARTICLE_TYPE,
            id("insomnia_omen"),
            FabricParticleTypes.simple()
    );

    public static final SoundEvent EVENT_MOB_EFFECT_INSOMNIA_OMEN = registerSound(id("event.mob_effect.insomnia_omen"));

    public static final RegistryEntry<StatusEffect> INSOMNIA_OMEN = Registry.registerReference(
            Registries.STATUS_EFFECT,
            id("insomnia_omen"),
            new StatusEffect(StatusEffectCategory.NEUTRAL, 0x5061A4FF, INSOMNIA_OMEN_PARTICLE) {}.applySound(EVENT_MOB_EFFECT_INSOMNIA_OMEN)
    );

	public static boolean canWearPhantom(PlayerEntity player) {
		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES)
            if (canGlideWith(player.getEquippedStack(equipmentSlot), equipmentSlot))
				return false;
		return true;
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

    public static boolean hasInsomniaOrOmen(LivingEntity entity) {
        return entity.hasStatusEffect(StatusEffects.BAD_OMEN) || entity.hasStatusEffect(INSOMNIA_OMEN);
    }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        PhantomFallAttachments.register();

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (blocked) return;
			if (entity.isGliding()) return;
            if (!source.isIn(PHANTOM_PICKUP)) return;
			var attacker = source.getAttacker();
			if (!(attacker instanceof PhantomEntity)) return;
			if (entity.getVehicle() instanceof PhantomEntity) return;
			if ((entity instanceof PlayerEntity) && entity.hasAttached(PHANTOM_DATA)) return;
            entity.startRiding(attacker);
        });
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof PhantomEntity phantom) || !(entity.getFirstPassenger() instanceof PlayerEntity player)) return true;

			player.dismountVehicle();

            if (damageSource.getAttacker() != player || !canWearPhantom(player) || EntityElytraEvents.CUSTOM.invoker().useCustomElytra(entity, false)) return true;

            phantom.setHealth(1);
            PhantomFallAttachments.setPhantom(player, phantom);
            phantom.discard();
            player.startGliding();

            return false;
        });
		EntityElytraEvents.CUSTOM.register((entity, tickElytra) ->
				entity instanceof PlayerEntity player && player.hasAttached(PHANTOM_DATA));

        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            var badOmenInstance = entity.getStatusEffect(StatusEffects.BAD_OMEN);
            if (badOmenInstance == null) return;
            var amplifier = badOmenInstance.getAmplifier();
            entity.removeStatusEffect(StatusEffects.BAD_OMEN);
            entity.addStatusEffect(new StatusEffectInstance(INSOMNIA_OMEN, (amplifier + 1) * 20 * 60 * 20));
        });

        EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) -> !hasInsomniaOrOmen(player));
        EntitySleepEvents.ALLOW_RESETTING_TIME.register(player -> !hasInsomniaOrOmen(player));
        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player,  sleepingPos, vanillaResult) -> hasInsomniaOrOmen(player) && player.getEntityWorld().getDimension().hasFixedTime() ? ActionResult.SUCCESS : ActionResult.PASS);
        EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, sleepingPos, vanillaResult) -> hasInsomniaOrOmen(player) ? ActionResult.SUCCESS : ActionResult.PASS);

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			var world = entity.getEntityWorld();
			if (!(world instanceof ServerWorld serverWorld)) return;
			if (!(entity instanceof PlayerEntity player)) return;
			int cooldown = player.getAttachedOrElse(PHANTOM_COOLDOWN, 0);
			if (cooldown > 0) return;
			world.calculateAmbientDarkness(); // day/night is based on ambient light which normally only updates every tick
			if (!world.isNight() && !hasInsomniaOrOmen(player)) {
				player.removeAttached(PHANTOMS_SPAWNED);
				return;
			}
			if (world.getDimension().hasSkyLight() && !world.isSkyVisible(entity.getBlockPos())) return;

			int spawnedPhantoms = player.getAttachedOrElse(PHANTOMS_SPAWNED, 0);
			var random = world.getRandom();

			if (!hasInsomniaOrOmen(player) && random.nextFloat() > CONFIG.server.baseSpawnChance + CONFIG.server.spawnChanceIncrease * spawnedPhantoms) return;

			var success = false;

			for (var size : distributeSizes(min(spawnedPhantoms + 1, CONFIG.server.maxSpawnScore), random)) {
				var blockPos = entity.getBlockPos().up(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
				if (!SpawnHelper.isClearForSpawn(world, blockPos, world.getBlockState(blockPos), world.getFluidState(blockPos), EntityType.PHANTOM)) continue;
				var phantom = EntityType.PHANTOM.create(world, SpawnReason.NATURAL);
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
                PhantomFallAttachments.increaseAmount(player);
                PhantomFallAttachments.setCooldown(player);
			}
		});
	}
}
