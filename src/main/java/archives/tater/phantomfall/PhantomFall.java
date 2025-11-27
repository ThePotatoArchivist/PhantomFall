package archives.tater.phantomfall;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static archives.tater.phantomfall.PhantomFallAttachments.*;
import static java.lang.Math.min;
import static net.minecraft.world.entity.LivingEntity.canGlideUsing;

@SuppressWarnings("UnstableApiUsage")
public class PhantomFall implements ModInitializer {
	public static final String MOD_ID = "phantomfall";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static SoundEvent registerSound(Identifier id) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

	public static final PhantomFallConfig CONFIG = PhantomFallConfig.createToml(
			FabricLoader.getInstance().getConfigDir(),
			MOD_ID,
			MOD_ID,
			PhantomFallConfig.class
	);

	public static final TagKey<DamageType> PHANTOM_PICKUP = TagKey.create(Registries.DAMAGE_TYPE, id("phantom_pickup"));

    public static final SimpleParticleType INSOMNIA_OMEN_PARTICLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            id("insomnia_omen"),
            FabricParticleTypes.simple()
    );

    public static final SoundEvent EVENT_MOB_EFFECT_INSOMNIA_OMEN = registerSound(id("event.mob_effect.insomnia_omen"));

    public static final Holder<MobEffect> INSOMNIA_OMEN = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            id("insomnia_omen"),
            new MobEffect(MobEffectCategory.NEUTRAL, 0x5061A4FF, INSOMNIA_OMEN_PARTICLE) {}.withSoundOnAdded(EVENT_MOB_EFFECT_INSOMNIA_OMEN)
    );

	public static boolean canWearPhantom(Player player) {
		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES)
            if (canGlideUsing(player.getItemBySlot(equipmentSlot), equipmentSlot))
				return false;
		return true;
	}

	public static List<Integer> distributeSizes(int value, RandomSource random) {
		var sizes = new ArrayList<Integer>();
		var remaining = value;
		while (remaining > 0) {
			var size = min(random.nextIntBetweenInclusive(1, CONFIG.server.maxPhantomSize), remaining);
			sizes.add(size); // Phantom size starts at 0
			remaining -= size;
		}
		return sizes;
	}

    public static boolean hasInsomniaOrOmen(LivingEntity entity) {
        return entity.hasEffect(MobEffects.BAD_OMEN) || entity.hasEffect(INSOMNIA_OMEN);
    }

    private static void playSoundTo(ServerPlayer player, SoundEvent sound, SoundSource source, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                source,
                player.getX(),
                player.getY(),
                player.getZ(),
                volume,
                pitch,
                player.getRandom().nextLong()
        ));
    }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        PhantomFallAttachments.register();

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (blocked) return;
			if (entity.isFallFlying()) return;
            if (!source.is(PHANTOM_PICKUP)) return;
			var attacker = source.getEntity();
			if (!(attacker instanceof Phantom)) return;
			if (entity.getVehicle() instanceof Phantom) return;
			if ((entity instanceof Player) && entity.hasAttached(PHANTOM_DATA)) return;
            entity.startRiding(attacker);
        });
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof Phantom phantom) || !(entity.getFirstPassenger() instanceof Player player)) return true;

			player.removeVehicle();

            if (damageSource.getEntity() != player || !canWearPhantom(player) || EntityElytraEvents.CUSTOM.invoker().useCustomElytra(entity, false)) return true;

            phantom.setHealth(1);
            PhantomFallAttachments.setPhantom(player, phantom);
            phantom.discard();
            player.startFallFlying();

            return false;
        });
		EntityElytraEvents.CUSTOM.register((entity, tickElytra) ->
				entity instanceof Player player && player.hasAttached(PHANTOM_DATA));

        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            var badOmenInstance = entity.getEffect(MobEffects.BAD_OMEN);
            if (badOmenInstance == null) return;
            var amplifier = badOmenInstance.getAmplifier();
            entity.removeEffect(MobEffects.BAD_OMEN);
            entity.addEffect(new MobEffectInstance(INSOMNIA_OMEN, (amplifier + 1) * 20 * 60 * 20));
        });

        EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) -> !hasInsomniaOrOmen(player));
        EntitySleepEvents.ALLOW_RESETTING_TIME.register(player -> !hasInsomniaOrOmen(player));
//        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player,  sleepingPos, vanillaResult) -> hasInsomniaOrOmen(player) && player.level().dimensionType().hasFixedTime() ? InteractionResult.SUCCESS : InteractionResult.PASS);
        EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, sleepingPos, vanillaResult) -> hasInsomniaOrOmen(player) ? InteractionResult.SUCCESS : InteractionResult.PASS);

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			var world = entity.level();
			if (!(entity instanceof ServerPlayer player)) return;
            var serverWorld = player.level();
			int cooldown = player.getAttachedOrElse(PHANTOM_COOLDOWN, 0);
			if (cooldown > 0) return;
			world.updateSkyBrightness(); // day/night is based on ambient light which normally only updates every tick
			if (!world.isDarkOutside() && !hasInsomniaOrOmen(player)) {
				player.removeAttached(PHANTOMS_SPAWNED);
				return;
			}
			if (world.dimensionType().hasSkyLight() && !world.canSeeSky(entity.blockPosition())) return;

			int spawnedPhantoms = player.getAttachedOrElse(PHANTOMS_SPAWNED, 0);
			var random = world.getRandom();

			if (!hasInsomniaOrOmen(player) && random.nextFloat() > CONFIG.server.baseSpawnChance + CONFIG.server.spawnChanceIncrease * spawnedPhantoms) return;

			var success = false;

			for (var size : distributeSizes(min(spawnedPhantoms + 1, CONFIG.server.maxSpawnScore), random)) {
				var blockPos = entity.blockPosition().above(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
				if (!NaturalSpawner.isValidEmptySpawnBlock(world, blockPos, world.getBlockState(blockPos), world.getFluidState(blockPos), EntityType.PHANTOM)) continue;
				var phantom = EntityType.PHANTOM.create(world, EntitySpawnReason.NATURAL);
				if (phantom == null) continue;
				phantom.snapTo(blockPos, 0.0F, 0.0F);
				phantom.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.NATURAL, null);
				phantom.setPhantomSize(size);
				phantom.setHealth(phantom.getMaxHealth());
				serverWorld.addFreshEntityWithPassengers(phantom);
				success = true;
			}

			if (success) {
				playSoundTo(player, SoundEvents.PHANTOM_AMBIENT, player.getSoundSource(), 1f, 0.6f);
                PhantomFallAttachments.increaseAmount(player);
                PhantomFallAttachments.setCooldown(player);
			}
		});
	}
}
