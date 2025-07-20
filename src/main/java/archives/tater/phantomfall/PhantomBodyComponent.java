package archives.tater.phantomfall;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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
    public void readData(ReadView readView) {
        var phantomData = readView.getOptionalReadView(PHANTOM_KEY);
        if (phantomData.isEmpty()) {
            phantom = null;
            return;
        }
        if (phantom == null)
            phantom = EntityType.PHANTOM.create(owner.getWorld(), SpawnReason.NATURAL);
        if (phantom != null)
            phantom.readData(phantomData.get());
    }

    @Override
    public void writeData(WriteView writeView) {
        if (phantom == null) return;
        phantom.writeData(writeView.get(PHANTOM_KEY));
    }

    public static final ComponentKey<PhantomBodyComponent> KEY = ComponentRegistry.getOrCreate(PhantomFall.id("phantom_body"), PhantomBodyComponent.class);

    @Override
    public void serverTick() {
        var phantom = this.phantom;
        if (!(owner.getWorld() instanceof ServerWorld world) || phantom == null || (owner.isAlive() && owner.isGliding() && PhantomFall.canWearPhantom(owner))) return;
        clearPhantom();
        phantom.setPosition(owner.getPos());
        phantom.setVelocity(Vec3d.ZERO);
        owner.getWorld().spawnEntity(phantom);
        phantom.damage(world, owner.getDamageSources().playerAttack(owner), Float.MAX_VALUE);
    }
}
