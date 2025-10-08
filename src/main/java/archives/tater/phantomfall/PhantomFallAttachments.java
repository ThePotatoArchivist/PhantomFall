package archives.tater.phantomfall;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
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
        target.setAttached(PHANTOM_DATA, phantom.writeNbt(new NbtCompound()));
    }

    public static @Nullable PhantomEntity getPhantom(AttachmentTarget target, World world) {
        var phantomData = target.getAttached(PHANTOM_DATA);
        if (phantomData == null) return null;
        if (target.hasAttached(CACHED_PHANTOM)) return target.getAttached(CACHED_PHANTOM);
        var phantom = EntityType.PHANTOM.create(world);
        if (phantom == null) return null;
        phantom.readNbt(phantomData);
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
        if (player.getWorld().isClient || phantomData == null || (player.isAlive() && player.isFallFlying() && PhantomFall.canWearPhantom(player))) return;
        player.removeAttached(PHANTOM_DATA);
        var phantom = EntityType.PHANTOM.create(player.getWorld());
        if (phantom == null) return;
        phantom.readNbt(phantomData);
        phantom.setPosition(player.getPos());
        phantom.setVelocity(Vec3d.ZERO);
        player.getWorld().spawnEntity(phantom);
        phantom.damage(player.getDamageSources().playerAttack(player), Float.MAX_VALUE);
    }

    public static void register() {
    }
}
