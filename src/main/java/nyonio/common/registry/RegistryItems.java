package nyonio.common.registry;

import nyonio.ShrunkGateway;
import nyonio.common.item.ItemResonantStarwheel;
import nyonio.common.item.ItemShrunkGateway;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ShrunkGateway.MODID)
public class RegistryItems {

    public static Item shrunkGateway;
    public static Item resonantStarwheel;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        
        shrunkGateway = new ItemShrunkGateway();
        registry.register(shrunkGateway);
        
        resonantStarwheel = new ItemResonantStarwheel();
        registry.register(resonantStarwheel);
    }
}
