package nyonio.client;

import nyonio.ShrunkGateway;
import nyonio.client.event.ClientShrunkGatewayHandler;
import nyonio.common.registry.RegistryItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ShrunkGateway.MODID, value = Side.CLIENT)
public class ClientRegistry {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerItemModel(RegistryItems.shrunkGateway);
    }

    private static void registerItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, 
            new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    public static void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientShrunkGatewayHandler());
    }
}
