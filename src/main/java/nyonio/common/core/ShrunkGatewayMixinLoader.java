package nyonio.common.core;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("nyonio.common.core")
@IFMLLoadingPlugin.SortingIndex(1001)
public class ShrunkGatewayMixinLoader implements IFMLLoadingPlugin {

    public ShrunkGatewayMixinLoader() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.shrunkgateway.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
