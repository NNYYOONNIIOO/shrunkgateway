package nyonio.common.event;

import nyonio.common.data.PrivateGatewayDataManager;
import nyonio.common.network.PrivateGatewayCache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerJoinHandler {

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            syncAllDimensions(player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            syncAllDimensions(player);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            syncAllDimensions(player);
        }
    }

    private void syncAllDimensions(EntityPlayerMP player) {
        syncAllDimensionsToPlayer(player.getUniqueID());
    }

    public static void syncAllDimensionsToPlayer(UUID playerUUID) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;
        
        PrivateGatewayDataManager.loadAllDimensions(server);
        
        Map<Integer, PrivateGatewayDataManager.PrivateGatewayWorldData> allData = 
            PrivateGatewayDataManager.getAllWorldData();
        
        for (Map.Entry<Integer, PrivateGatewayDataManager.PrivateGatewayWorldData> entry : allData.entrySet()) {
            int dim = entry.getKey();
            PrivateGatewayDataManager.PrivateGatewayWorldData data = entry.getValue();
            
            World world = server.getWorld(dim);
            
            List<PrivateGatewayDataManager.PrivateGatewayNode> playerGateways = 
                data.getGatewaysForOwner(playerUUID);
            
            for (PrivateGatewayDataManager.PrivateGatewayNode node : playerGateways) {
                if (world != null && !world.isRemote) {
                    PrivateGatewayCache.instance.addPosition(world, node, node.display, node.owner, node.getAuthorizedPlayers());
                } else {
                    PrivateGatewayCache.instance.addPositionToCache(dim, node, node.display, node.owner, node.getAuthorizedPlayers());
                }
            }
            
            List<PrivateGatewayDataManager.PrivateGatewayNode> authorizedGateways = 
                data.getGatewaysForAuthorizedPlayer(playerUUID);
            
            for (PrivateGatewayDataManager.PrivateGatewayNode node : authorizedGateways) {
                if (world != null && !world.isRemote) {
                    PrivateGatewayCache.instance.addPosition(world, node, node.display, node.owner, node.getAuthorizedPlayers());
                } else {
                    PrivateGatewayCache.instance.addPositionToCache(dim, node, node.display, node.owner, node.getAuthorizedPlayers());
                }
            }
        }
        
        PrivateGatewayCache.instance.syncToOwner(playerUUID);
    }
}
