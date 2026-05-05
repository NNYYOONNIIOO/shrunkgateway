package nyonio.common.network;

import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktShrunkGatewayTeleport implements IMessage, IMessageHandler<PktShrunkGatewayTeleport, IMessage> {

    private int dimId;
    private BlockPos pos;

    public PktShrunkGatewayTeleport() {}

    public PktShrunkGatewayTeleport(int dimId, BlockPos pos) {
        this.dimId = dimId;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dimId = buf.readInt();
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dimId);
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
    }

    @Override
    public IMessage onMessage(PktShrunkGatewayTeleport message, MessageContext ctx) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            EntityPlayer player = ctx.getServerHandler().player;
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
                World targetWorld = server.getWorld(message.dimId);
                if (targetWorld != null) {
                    GatewayCache gatewayCache = WorldCacheManager.getOrLoadData(targetWorld, WorldCacheManager.SaveKey.GATEWAY_DATA);
                    if (MiscUtils.contains(gatewayCache.getGatewayPositions(), node -> node.equals(message.pos))) {
                        MiscUtils.transferEntityTo(player, message.dimId, message.pos);
                    }
                }
            }
        });
        return null;
    }
}
