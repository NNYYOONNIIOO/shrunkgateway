package nyonio.common.registry;

import hellfirepvp.astralsorcery.common.structure.StructureRegistry;
import hellfirepvp.astralsorcery.common.structure.StructureMatcherRegistry;
import hellfirepvp.astralsorcery.common.structure.match.StructureMatcherPatternArray;
import nyonio.common.multiblock.MultiblockPrivateGateway;
import nyonio.common.multiblock.MultiblockResonanceGateway;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class RegistryStructures {

    public static MultiblockPrivateGateway patternPrivateGateway;
    public static MultiblockResonanceGateway structureResonanceGateway;

    public static void init(FMLInitializationEvent event) {
        patternPrivateGateway = new MultiblockPrivateGateway();
        StructureRegistry.INSTANCE.register(patternPrivateGateway);
        StructureMatcherRegistry.INSTANCE.register(() -> 
            new StructureMatcherPatternArray(new ResourceLocation("shrunkgateway", "pattern_private_gateway")));
        
        structureResonanceGateway = new MultiblockResonanceGateway();
        StructureRegistry.INSTANCE.register(structureResonanceGateway);
        StructureMatcherRegistry.INSTANCE.register(() -> 
            new StructureMatcherPatternArray(new ResourceLocation("shrunkgateway", "pattern_resonance_gateway")));
    }
}
