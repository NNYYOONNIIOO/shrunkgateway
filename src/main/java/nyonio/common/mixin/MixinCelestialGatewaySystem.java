package nyonio.common.mixin;

import hellfirepvp.astralsorcery.common.auxiliary.CelestialGatewaySystem;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.client.event.PrivateGatewayClientInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = CelestialGatewaySystem.class, remap = false)
public class MixinCelestialGatewaySystem {

    @Inject(method = "updateClientCache", at = @At("RETURN"), remap = false)
    @SideOnly(Side.CLIENT)
    private void onUpdateClientCache(Map<Integer, List<GatewayCache.GatewayNode>> positions, CallbackInfo ci) {
        PrivateGatewayClientInjector.onCelestialCacheUpdated();
    }
}
