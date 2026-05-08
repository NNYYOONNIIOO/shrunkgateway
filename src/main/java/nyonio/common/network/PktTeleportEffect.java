package nyonio.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktTeleportEffect implements IMessage, IMessageHandler<PktTeleportEffect, IMessage> {

    private BlockPos pos;

    public PktTeleportEffect() {}

    public PktTeleportEffect(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
    }

    @Override
    public IMessage onMessage(PktTeleportEffect message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            handleEffect(message.pos);
        });
        return null;
    }

    @SideOnly(Side.CLIENT)
    private void handleEffect(BlockPos pos) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;
        
        // 在目标位置生成传送特效
        nyonio.client.event.ClientResonanceGatewayHandler.spawnTeleportEffect(pos);
    }
}
