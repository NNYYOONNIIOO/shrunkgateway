package nyonio.common.multiblock;

import hellfirepvp.astralsorcery.common.lib.MultiBlockArrays;
import hellfirepvp.astralsorcery.common.structure.array.PatternBlockArray;
import nyonio.common.registry.RegistryBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class MultiblockResonanceGateway extends PatternBlockArray {

    public MultiblockResonanceGateway() {
        super(new ResourceLocation("shrunkgateway", "pattern_resonance_gateway"));
        loadFromOriginal();
    }

    private void loadFromOriginal() {
        PatternBlockArray original = MultiBlockArrays.patternCelestialGateway;
        
        addAll(original);
        
        addBlock(BlockPos.ORIGIN, RegistryBlocks.resonanceGateway.getDefaultState());
    }
}
