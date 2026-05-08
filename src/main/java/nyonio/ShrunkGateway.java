package nyonio;

import nyonio.client.ClientProxy;
import nyonio.common.CommonProxy;
import nyonio.common.network.PacketHandler;
import nyonio.common.registry.RegistryEntities;
import nyonio.common.registry.RegistryStructures;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ShrunkGateway.MODID, name = ShrunkGateway.NAME, version = ShrunkGateway.VERSION, dependencies = ShrunkGateway.DEPENDENCIES)
public class ShrunkGateway
{
    public static final String MODID = "shrunkgateway";
    public static final String NAME = "Shrunk Gateway";
    public static final String VERSION = "1.1.0";
    public static final String DEPENDENCIES = "required-after:astralsorcery";

    @SidedProxy(
        clientSide = "nyonio.client.ClientProxy",
        serverSide = "nyonio.common.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static ShrunkGateway instance;

    public static Logger LOGGER;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
        PacketHandler.init();
        RegistryEntities.registerEntities();
        proxy.preInit(event);
        LOGGER.info("Shrunk Gateway preInit");
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        RegistryStructures.init(event);
        LOGGER.info("Shrunk Gateway initialized");
    }
}
