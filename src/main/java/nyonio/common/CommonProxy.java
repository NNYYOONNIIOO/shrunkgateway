package nyonio.common;

import nyonio.common.event.KnowledgeScrollBindingHandler;
import nyonio.common.event.PlayerJoinHandler;
import nyonio.common.event.StardustEntityHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new PlayerJoinHandler());
        MinecraftForge.EVENT_BUS.register(new KnowledgeScrollBindingHandler());
        MinecraftForge.EVENT_BUS.register(new StardustEntityHandler());
    }
}
