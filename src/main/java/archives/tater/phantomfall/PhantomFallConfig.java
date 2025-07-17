package archives.tater.phantomfall;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class PhantomFallConfig extends WrappedConfig {
    public final Client client = new Client();
    public final Server server = new Server();

    public static class Client implements Section {
        @Comment("Whether the player should be set to third person upon gaining phantom wings")
        public boolean changePerspective = true;
    }

    public static class Server implements Section {
        @Comment("Largest phantom size that can spawn naturally")
        public int maxPhantomSize = 4;
        @Comment("Chance of phantoms spawning when a player is on 0 previous spawns")
        public float baseSpawnChance = 0.5f;
        @Comment("How much the spawn chance should increase per previous spawn")
        public float spawnChanceIncrease = 0.05f;
        @Comment("The cap for the \"spawning score\" used to spawn a group of phantoms")
        public int maxSpawnScore = 8;
    }
}
