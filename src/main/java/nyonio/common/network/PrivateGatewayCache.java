package nyonio.common.network;

import nyonio.common.data.PrivateGatewayDataManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

public class PrivateGatewayCache {

    public static PrivateGatewayCache instance = new PrivateGatewayCache();

    private Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> serverCache = new HashMap<>();
    private Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> clientCache = new HashMap<>();

    public void addPosition(World world, BlockPos pos, String display, UUID owner) {
        addPosition(world, pos, display, owner, null);
    }

    public void addPosition(World world, BlockPos pos, String display, UUID owner, Set<UUID> authorizedPlayers) {
        if (world.isRemote) return;

        int dim = world.provider.getDimension();
        addPositionToCache(dim, pos, display, owner, authorizedPlayers);
    }

    public void addPositionToCache(int dim, BlockPos pos, String display, UUID owner, Set<UUID> authorizedPlayers) {
        if (!serverCache.containsKey(dim)) {
            serverCache.put(dim, new ArrayList<>());
        }

        List<PktSyncPrivateGateways.PrivateGatewayNode> nodes = serverCache.get(dim);
        
        boolean exists = false;
        for (int i = 0; i < nodes.size(); i++) {
            PktSyncPrivateGateways.PrivateGatewayNode existing = nodes.get(i);
            if (existing.pos.equals(pos)) {
                nodes.set(i, new PktSyncPrivateGateways.PrivateGatewayNode(pos, display, owner, authorizedPlayers));
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            PktSyncPrivateGateways.PrivateGatewayNode node = 
                new PktSyncPrivateGateways.PrivateGatewayNode(pos, display, owner, authorizedPlayers);
            nodes.add(node);
        }
    }

    public void removePosition(World world, BlockPos pos, UUID owner) {
        if (world.isRemote) return;

        int dim = world.provider.getDimension();
        if (!serverCache.containsKey(dim)) return;

        List<PktSyncPrivateGateways.PrivateGatewayNode> nodes = serverCache.get(dim);
        nodes.removeIf(node -> node.pos.equals(pos));
        
        if (nodes.isEmpty()) {
            serverCache.remove(dim);
        }
    }

    private Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> filterForPlayer(UUID playerUUID) {
        Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> filtered = new HashMap<>();
        
        for (Map.Entry<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> entry : serverCache.entrySet()) {
            List<PktSyncPrivateGateways.PrivateGatewayNode> playerNodes = new ArrayList<>();
            
            for (PktSyncPrivateGateways.PrivateGatewayNode node : entry.getValue()) {
                boolean canAccess = false;
                
                if (node.owner != null && node.owner.equals(playerUUID)) {
                    canAccess = true;
                } else if (node.authorizedPlayers != null && node.authorizedPlayers.contains(playerUUID)) {
                    canAccess = true;
                }
                
                if (canAccess) {
                    playerNodes.add(node);
                }
            }
            
            if (!playerNodes.isEmpty()) {
                filtered.put(entry.getKey(), playerNodes);
            }
        }
        
        return filtered;
    }

    public void syncTo(EntityPlayerMP player) {
        Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> playerCache = 
            filterForPlayer(player.getUniqueID());
        PktSyncPrivateGateways pkt = new PktSyncPrivateGateways(playerCache);
        PacketHandler.INSTANCE.sendTo(pkt, player);
    }

    public void syncToOwner(UUID ownerUUID) {
        if (ownerUUID == null) return;
        
        net.minecraft.server.management.PlayerList playerList = 
            net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        EntityPlayerMP player = playerList.getPlayerByUUID(ownerUUID);
        
        if (player != null) {
            syncTo(player);
        }
    }

    public void updateClientCache(Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> positions) {
        this.clientCache = positions;
    }

    public List<PktSyncPrivateGateways.PrivateGatewayNode> getGatewaysForWorld(World world, Side side) {
        Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> cache = side == Side.SERVER ? serverCache : clientCache;
        int dim = world.provider.getDimension();
        return cache.getOrDefault(dim, Collections.emptyList());
    }

    public Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> getGatewayCache(Side side) {
        return Collections.unmodifiableMap(side == Side.SERVER ? serverCache : clientCache);
    }

    public boolean isPlayerAuthorized(World world, BlockPos pos, UUID owner, UUID playerUUID) {
        int dim = world.provider.getDimension();
        List<PktSyncPrivateGateways.PrivateGatewayNode> nodes = clientCache.get(dim);
        
        if (nodes == null) return false;
        
        for (PktSyncPrivateGateways.PrivateGatewayNode node : nodes) {
            if (node.pos.equals(pos)) {
                if (node.owner != null && node.owner.equals(playerUUID)) {
                    return true;
                }
                if (node.authorizedPlayers != null && node.authorizedPlayers.contains(playerUUID)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
