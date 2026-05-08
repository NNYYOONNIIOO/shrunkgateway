package nyonio.common.mixin;

import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import nyonio.common.tile.TileResonanceGateway;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileCelestialGateway.class)
public class MixinTileCelestialGateway {
    
    @Inject(
        method = "playEffects",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void onPlayEffects(CallbackInfo ci) {
        TileCelestialGateway gateway = (TileCelestialGateway) (Object) this;
        
        if (gateway instanceof TileResonanceGateway) {
            ci.cancel();
        }
    }
}
