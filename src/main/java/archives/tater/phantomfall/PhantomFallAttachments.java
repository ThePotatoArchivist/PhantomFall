package archives.tater.phantomfall;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class PhantomFallAttachments {
    public static final AttachmentType<NbtCompound> PHANTOM_DATA = AttachmentRegistry.create(PhantomFall.id("phantom_data"), builder -> builder
            .persistent(NbtCompound.CODEC)
            .syncWith(PacketCodecs.NBT_COMPOUND, AttachmentSyncPredicate.all())
    );

    public static final AttachmentType<Integer> PHANTOMS_SPAWNED = AttachmentRegistry.create(PhantomFall.id("phantoms_spawned"), builder -> builder
            .persistent(Codec.intRange(0, Integer.MAX_VALUE))
            .copyOnDeath()
    );
    public static final AttachmentType<Integer> PHANTOM_COOLDOWN = AttachmentRegistry.create(PhantomFall.id("phantom_cooldown"), builder -> builder
            .persistent(Codec.intRange(0, Integer.MAX_VALUE))
            .copyOnDeath()
    );

    public static final AttachmentType<PhantomEntity> CACHED_PHANTOM = AttachmentRegistry.create(PhantomFall.id("cached_phantom"));

    public static void setPhantom(AttachmentTarget target, PhantomEntity phantom) {
        try (var logging = new ErrorReporter.Logging(phantom.getErrorReporterContext(), PhantomFall.LOGGER)) {
            var writeView = NbtWriteView.create(logging, phantom.getRegistryManager());
            phantom.writeData(writeView);
            target.setAttached(PHANTOM_DATA, writeView.getNbt());
        }
    }

    private static @Nullable PhantomEntity createPhantom(NbtCompound data, World world) {
        var phantom = EntityType.PHANTOM.create(world, SpawnReason.NATURAL);
        if (phantom == null) return null;
        try (var logging = new ErrorReporter.Logging(phantom.getErrorReporterContext(), PhantomFall.LOGGER)) {
            phantom.readData(NbtReadView.create(logging, world.getRegistryManager(), data));
        }
        return phantom;
    }

    public static @Nullable PhantomEntity getPhantom(AttachmentTarget target, World world) {
        var phantomData = target.getAttached(PHANTOM_DATA);
        if (phantomData == null) return null;
        if (target.hasAttached(CACHED_PHANTOM)) return target.getAttached(CACHED_PHANTOM);
        var phantom = createPhantom(phantomData, world);
        target.setAttached(CACHED_PHANTOM, phantom);
        return phantom;
    }

    public static @Nullable PhantomEntity getPhantom(Entity entity) {
        return getPhantom(entity, entity.getWorld());
    }

    public static void increaseAmount(AttachmentTarget target) {
        target.setAttached(PHANTOMS_SPAWNED, target.getAttachedOrElse(PHANTOMS_SPAWNED, 0) + 1);
    }

    public static void setCooldown(AttachmentTarget target) {
        target.setAttached(PHANTOM_COOLDOWN, 20 * PhantomFall.CONFIG.server.spawnCooldown);
    }

    public static void tickAttachments(ServerPlayerEntity player) {
        int cooldown = player.getAttachedOrElse(PHANTOM_COOLDOWN, 0);
        if (cooldown > 0) {
            player.setAttached(PHANTOM_COOLDOWN, cooldown - 1);
        }

        var phantomData = player.getAttached(PHANTOM_DATA);
        if (!(player.getWorld() instanceof ServerWorld serverWorld) || phantomData == null || (player.isAlive() && player.isGliding() && PhantomFall.canWearPhantom(player))) return;
        player.removeAttached(PHANTOM_DATA);
        var phantom = createPhantom(phantomData, player.getWorld());
        if (phantom == null) return;
        phantom.setPosition(player.getPos());
        phantom.setVelocity(Vec3d.ZERO);
        player.getWorld().spawnEntity(phantom);
        phantom.damage(serverWorld, player.getDamageSources().playerAttack(player), Float.MAX_VALUE);
    }

    public static void register() {
    }
}
