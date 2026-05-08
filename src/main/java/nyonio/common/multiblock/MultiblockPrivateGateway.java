package nyonio.common.multiblock;

import hellfirepvp.astralsorcery.common.lib.MultiBlockArrays;
import hellfirepvp.astralsorcery.common.structure.array.PatternBlockArray;
import nyonio.common.registry.RegistryBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class MultiblockPrivateGateway extends PatternBlockArray {

    public MultiblockPrivateGateway() {
        super(new ResourceLocation("shrunkgateway", "pattern_private_gateway"));
        loadFromOriginal();
    }

    private void loadFromOriginal() {
        PatternBlockArray original = MultiBlockArrays.patternCelestialGateway;
        
        addAll(original);
        
        addBlock(BlockPos.ORIGIN, RegistryBlocks.privateGateway.getDefaultState());
    }
}
