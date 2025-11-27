package archives.tater.phantomfall;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class PhantomFallAttachments {
    public static final AttachmentType<CompoundTag> PHANTOM_DATA = AttachmentRegistry.create(PhantomFall.id("phantom_data"), builder -> builder
            .persistent(CompoundTag.CODEC)
            .syncWith(ByteBufCodecs.COMPOUND_TAG, AttachmentSyncPredicate.all())
    );

    public static final AttachmentType<Integer> PHANTOMS_SPAWNED = AttachmentRegistry.create(PhantomFall.id("phantoms_spawned"), builder -> builder
            .persistent(Codec.intRange(0, Integer.MAX_VALUE))
            .copyOnDeath()
    );
    public static final AttachmentType<Integer> PHANTOM_COOLDOWN = AttachmentRegistry.create(PhantomFall.id("phantom_cooldown"), builder -> builder
            .persistent(Codec.intRange(0, Integer.MAX_VALUE))
            .copyOnDeath()
    );

    public static final AttachmentType<Phantom> CACHED_PHANTOM = AttachmentRegistry.create(PhantomFall.id("cached_phantom"));

    public static void setPhantom(AttachmentTarget target, Phantom phantom) {
        target.setAttached(PHANTOM_DATA, phantom.saveWithoutId(new CompoundTag()));
    }

    public static @Nullable Phantom getPhantom(AttachmentTarget target, Level world) {
        var phantomData = target.getAttached(PHANTOM_DATA);
        if (phantomData == null) return null;
        if (target.hasAttached(CACHED_PHANTOM)) return target.getAttached(CACHED_PHANTOM);
        var phantom = EntityType.PHANTOM.create(world);
        if (phantom == null) return null;
        phantom.load(phantomData);
        target.setAttached(CACHED_PHANTOM, phantom);
        return phantom;
    }

    public static @Nullable Phantom getPhantom(Entity entity) {
        return getPhantom(entity, entity.level());
    }

    public static void increaseAmount(AttachmentTarget target) {
        target.setAttached(PHANTOMS_SPAWNED, target.getAttachedOrElse(PHANTOMS_SPAWNED, 0) + 1);
    }

    public static void setCooldown(AttachmentTarget target) {
        target.setAttached(PHANTOM_COOLDOWN, 20 * PhantomFall.CONFIG.server.spawnCooldown);
    }

    public static void tickAttachments(ServerPlayer player) {
        int cooldown = player.getAttachedOrElse(PHANTOM_COOLDOWN, 0);
        if (cooldown > 0) {
            player.setAttached(PHANTOM_COOLDOWN, cooldown - 1);
        }

        var phantomData = player.getAttached(PHANTOM_DATA);
        if (player.level().isClientSide || phantomData == null || (player.isAlive() && player.isFallFlying() && PhantomFall.canWearPhantom(player))) return;
        player.removeAttached(PHANTOM_DATA);
        var phantom = EntityType.PHANTOM.create(player.level());
        if (phantom == null) return;
        phantom.load(phantomData);
        phantom.setPos(player.position());
        phantom.setDeltaMovement(Vec3.ZERO);
        player.level().addFreshEntity(phantom);
        phantom.hurt(player.damageSources().playerAttack(player), Float.MAX_VALUE);
    }

    public static void register() {
    }
}
