package nyonio.common.network;

import nyonio.ShrunkGateway;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(ShrunkGateway.MODID);

    public static void init() {
        INSTANCE.registerMessage(PktSyncPrivateGateways.Handler.class, PktSyncPrivateGateways.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(PktShrunkGatewayTeleport.class, PktShrunkGatewayTeleport.class, 1, Side.SERVER);
        INSTANCE.registerMessage(PktTeleportEffect.class, PktTeleportEffect.class, 2, Side.CLIENT);
    }
}
