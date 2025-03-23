package archives.tater.phantomfall;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PhantomBodyComponent implements Component, AutoSyncedComponent, ServerTickingComponent {
    private final PlayerEntity owner;
    private @Nullable PhantomEntity phantom;

    public static final String PHANTOM_KEY = "Phantom";

    public PhantomBodyComponent(PlayerEntity owner) {
        this.owner = owner;
    }

    public @Nullable PhantomEntity getPhantom() {
        return phantom;
    }

    public void setPhantomFrom(@NotNull PhantomEntity phantom) {
        this.phantom = new PhantomEntity(EntityType.PHANTOM, phantom.getWorld());
        this.phantom.copyFrom(phantom);
        KEY.sync(owner);
    }

    public void clearPhantom() {
        this.phantom = null;
        KEY.sync(owner);
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (nbtCompound.contains(PHANTOM_KEY, NbtElement.COMPOUND_TYPE)) {
            if (phantom == null)
                phantom = EntityType.PHANTOM.create(owner.getWorld());
            if (phantom != null) // Yes this is goofy
                phantom.readNbt(nbtCompound);
        } else if (phantom != null)
            phantom = null;
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (phantom == null) return;
        var entityTag = new NbtCompound();
        phantom.writeNbt(entityTag);
        nbtCompound.put(PHANTOM_KEY, entityTag);
    }

    public static final ComponentKey<PhantomBodyComponent> KEY = ComponentRegistry.getOrCreate(PhantomFall.id("phantom_body"), PhantomBodyComponent.class);

    @Override
    public void serverTick() {
        var phantom = this.phantom;
        if (owner.getWorld().isClient || phantom == null || (owner.isFallFlying() && PhantomFall.canWearPhantom(owner))) return;
        clearPhantom();
        phantom.setPosition(owner.getPos());
        phantom.setVelocity(Vec3d.ZERO);
        owner.getWorld().spawnEntity(phantom);
        phantom.damage(owner.getDamageSources().playerAttack(owner), Float.MAX_VALUE);
    }
}
