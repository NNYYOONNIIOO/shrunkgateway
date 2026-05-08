package nyonio.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.*;

public class PktSyncPrivateGateways implements IMessage {

    private Map<Integer, List<PrivateGatewayNode>> gateways = new HashMap<>();

    public PktSyncPrivateGateways() {}

    public PktSyncPrivateGateways(Map<Integer, List<PrivateGatewayNode>> gateways) {
        this.gateways = gateways;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        if (compound != null) {
            for (String dimKey : compound.getKeySet()) {
                int dim = Integer.parseInt(dimKey);
                NBTTagList list = compound.getTagList(dimKey, Constants.NBT.TAG_COMPOUND);
                List<PrivateGatewayNode> nodes = new ArrayList<>();
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    BlockPos pos = BlockPos.fromLong(tag.getLong("pos"));
                    String display = tag.getString("display");
                    UUID owner = tag.hasUniqueId("owner") ? tag.getUniqueId("owner") : null;
                    
                    Set<UUID> authorizedPlayers = new HashSet<>();
                    if (tag.hasKey("authorizedPlayers")) {
                        NBTTagList authList = tag.getTagList("authorizedPlayers", Constants.NBT.TAG_COMPOUND);
                        for (int j = 0; j < authList.tagCount(); j++) {
                            NBTTagCompound playerTag = authList.getCompoundTagAt(j);
                            if (playerTag.hasUniqueId("player")) {
                                authorizedPlayers.add(playerTag.getUniqueId("player"));
                            }
                        }
                    }
                    
                    nodes.add(new PrivateGatewayNode(pos, display, owner, authorizedPlayers));
                }
                gateways.put(dim, nodes);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound compound = new NBTTagCompound();
        for (Map.Entry<Integer, List<PrivateGatewayNode>> entry : gateways.entrySet()) {
            NBTTagList list = new NBTTagList();
            for (PrivateGatewayNode node : entry.getValue()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setLong("pos", node.pos.toLong());
                tag.setString("display", node.display);
                if (node.owner != null) {
                    tag.setUniqueId("owner", node.owner);
                }
                
                if (node.authorizedPlayers != null && !node.authorizedPlayers.isEmpty()) {
                    NBTTagList authList = new NBTTagList();
                    for (UUID playerUUID : node.authorizedPlayers) {
                        NBTTagCompound playerTag = new NBTTagCompound();
                        playerTag.setUniqueId("player", playerUUID);
                        authList.appendTag(playerTag);
                    }
                    tag.setTag("authorizedPlayers", authList);
                }
                
                list.appendTag(tag);
            }
            compound.setTag(String.valueOf(entry.getKey()), list);
        }
        ByteBufUtils.writeTag(buf, compound);
    }

    public Map<Integer, List<PrivateGatewayNode>> getGateways() {
        return gateways;
    }

    public static class Handler implements IMessageHandler<PktSyncPrivateGateways, IMessage> {
        @Override
        public IMessage onMessage(PktSyncPrivateGateways message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                PrivateGatewayCache.instance.updateClientCache(message.getGateways());
            });
            return null;
        }
    }

    public static class PrivateGatewayNode {
        public final BlockPos pos;
        public final String display;
        public final UUID owner;
        public final Set<UUID> authorizedPlayers;

        public PrivateGatewayNode(BlockPos pos, String display, UUID owner) {
            this.pos = pos;
            this.display = display;
            this.owner = owner;
            this.authorizedPlayers = new HashSet<>();
        }

        public PrivateGatewayNode(BlockPos pos, String display, UUID owner, Set<UUID> authorizedPlayers) {
            this.pos = pos;
            this.display = display;
            this.owner = owner;
            this.authorizedPlayers = authorizedPlayers != null ? new HashSet<>(authorizedPlayers) : new HashSet<>();
        }
    }
}
