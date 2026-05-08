package nyonio.common.registry;

import nyonio.ShrunkGateway;
import nyonio.common.block.BlockPrivateGateway;
import nyonio.common.block.BlockResonanceGateway;
import nyonio.common.item.ItemBlockPrivateGateway;
import nyonio.common.item.ItemBlockResonanceGateway;
import nyonio.common.tile.TilePrivateGateway;
import nyonio.common.tile.TileResonanceGateway;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ShrunkGateway.MODID)
public class RegistryBlocks {

    public static Block privateGateway;
    public static Block resonanceGateway;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        privateGateway = new BlockPrivateGateway();
        privateGateway.setUnlocalizedName("shrunkgateway.private_gateway");
        privateGateway.setRegistryName(new ResourceLocation(ShrunkGateway.MODID, "private_gateway"));
        registry.register(privateGateway);

        GameRegistry.registerTileEntity(TilePrivateGateway.class, new ResourceLocation(ShrunkGateway.MODID, "private_gateway"));

        resonanceGateway = new BlockResonanceGateway();
        resonanceGateway.setUnlocalizedName("shrunkgateway.resonance_gateway");
        resonanceGateway.setRegistryName(new ResourceLocation(ShrunkGateway.MODID, "resonance_gateway"));
        registry.register(resonanceGateway);

        GameRegistry.registerTileEntity(TileResonanceGateway.class, new ResourceLocation(ShrunkGateway.MODID, "resonance_gateway"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        ItemBlockPrivateGateway privateGatewayItem = new ItemBlockPrivateGateway(privateGateway);
        privateGatewayItem.setUnlocalizedName("shrunkgateway.private_gateway");
        privateGatewayItem.setRegistryName(new ResourceLocation(ShrunkGateway.MODID, "private_gateway"));
        registry.register(privateGatewayItem);

        ItemBlockResonanceGateway resonanceGatewayItem = new ItemBlockResonanceGateway(resonanceGateway);
        resonanceGatewayItem.setUnlocalizedName("shrunkgateway.resonance_gateway");
        resonanceGatewayItem.setRegistryName(new ResourceLocation(ShrunkGateway.MODID, "resonance_gateway"));
        registry.register(resonanceGatewayItem);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(privateGateway), 0,
            new ModelResourceLocation(privateGateway.getRegistryName(), "inventory"));
        
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(resonanceGateway), 0,
            new ModelResourceLocation(resonanceGateway.getRegistryName(), "inventory"));
    }
}
