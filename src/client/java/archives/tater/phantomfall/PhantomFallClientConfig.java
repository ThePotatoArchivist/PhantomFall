package archives.tater.phantomfall;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class PhantomFallClientConfig extends WrappedConfig {
    @Comment("Whether the player should be set to third person upon gaining phantom wings")
    public boolean changePerspective = true;
}
