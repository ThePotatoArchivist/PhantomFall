package archives.tater.phantomfall;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PhantomsSpawnedComponent implements Component, ServerTickingComponent {
    private int amount = 0;
    private int cooldown = 0;

    public static final String AMOUNT_KEY = "amount";
    public static final String COOLDOWN_KEY = "cooldown";

    public int getAmount() {
        return amount;
    }

    public void increaseAmount() {
        amount++;
    }

    public void resetAmount() {
        amount = 0;
    }

    public boolean canSpawnPhantoms() {
        return cooldown <= 0;
    }

    public void setCooldown() {
        cooldown = 20 * PhantomFall.CONFIG.server.spawnCooldown;
    }

    @Override
    public void serverTick() {
        if (cooldown > 0)
            cooldown--;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        amount = nbtCompound.getInt(AMOUNT_KEY);
        cooldown = nbtCompound.getInt(COOLDOWN_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbtCompound.putInt(AMOUNT_KEY, amount);
        nbtCompound.putInt(COOLDOWN_KEY, cooldown);
    }

    public static final ComponentKey<PhantomsSpawnedComponent> KEY = ComponentRegistry.getOrCreate(PhantomFall.id("phantoms_spawned"), PhantomsSpawnedComponent.class);
}
