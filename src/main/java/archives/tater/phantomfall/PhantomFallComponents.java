package archives.tater.phantomfall;

import net.minecraft.entity.player.PlayerEntity;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class PhantomFallComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, PhantomBodyComponent.KEY, PhantomBodyComponent::new);
        registry.registerForPlayers(PhantomsSpawnedComponent.KEY, entity -> new PhantomsSpawnedComponent(), RespawnCopyStrategy.CHARACTER);
    }
}
