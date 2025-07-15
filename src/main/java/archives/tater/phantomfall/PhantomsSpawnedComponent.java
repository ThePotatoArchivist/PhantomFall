package archives.tater.phantomfall;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

public class PhantomsSpawnedComponent implements Component {
    private int amount = 0;

    public static final String AMOUNT_KEY = "amount";

    public int getAmount() {
        return amount;
    }

    public void increaseAmount() {
        amount++;
    }

    public void resetAmount() {
        amount = 0;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        amount = nbtCompound.getInt(AMOUNT_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbtCompound.putInt(AMOUNT_KEY, amount);
    }

    public static final ComponentKey<PhantomsSpawnedComponent> KEY = ComponentRegistry.getOrCreate(PhantomFall.id("phantoms_spawned"), PhantomsSpawnedComponent.class);
}
