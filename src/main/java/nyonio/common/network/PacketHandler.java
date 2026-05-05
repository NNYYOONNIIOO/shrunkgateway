package nyonio.common.network;

import nyonio.ShrunkGateway;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static SimpleNetworkWrapper CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ShrunkGateway.MODID);
        CHANNEL.registerMessage(PktShrunkGatewayTeleport.class, PktShrunkGatewayTeleport.class, 0, Side.SERVER);
    }
}
