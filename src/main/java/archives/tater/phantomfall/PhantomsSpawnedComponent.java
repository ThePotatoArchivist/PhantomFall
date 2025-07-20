package archives.tater.phantomfall;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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
    public void readData(ReadView readView) {
        amount = readView.getInt(AMOUNT_KEY, 0);
        cooldown = readView.getInt(COOLDOWN_KEY, 0);
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.putInt(AMOUNT_KEY, amount);
        writeView.putInt(COOLDOWN_KEY, cooldown);
    }

    public static final ComponentKey<PhantomsSpawnedComponent> KEY = ComponentRegistry.getOrCreate(PhantomFall.id("phantoms_spawned"), PhantomsSpawnedComponent.class);
}
