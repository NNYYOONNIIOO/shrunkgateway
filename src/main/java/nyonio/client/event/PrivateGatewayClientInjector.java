package nyonio.client.event;

import hellfirepvp.astralsorcery.common.auxiliary.CelestialGatewaySystem;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.common.network.PktSyncPrivateGateways;
import nyonio.common.network.PrivateGatewayCache;

import java.util.*;

@SideOnly(Side.CLIENT)
public class PrivateGatewayClientInjector {

    private static Set<String> injectedGateways = new HashSet<>();
    private static boolean needReinject = false;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) {
            injectedGateways.clear();
            return;
        }

        World world = mc.world;
        EntityPlayer player = mc.player;
        
        injectPrivateGateways(world, player);
    }

    public static void onCelestialCacheUpdated() {
        needReinject = true;
    }

    private void injectPrivateGateways(World world, EntityPlayer player) {
        UUID playerUUID = player.getUniqueID();
        
        Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> allPrivateGateways = 
            PrivateGatewayCache.instance.getGatewayCache(Side.CLIENT);
        
        Map<Integer, List<GatewayCache.GatewayNode>> celestialCache = 
            new HashMap<>(CelestialGatewaySystem.instance.getGatewayCache(Side.CLIENT));
        
        boolean cacheUpdated = false;
        
        for (Map.Entry<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> entry : allPrivateGateways.entrySet()) {
            int dim = entry.getKey();
            List<PktSyncPrivateGateways.PrivateGatewayNode> privateGateways = entry.getValue();
            
            if (!celestialCache.containsKey(dim)) {
                celestialCache.put(dim, new ArrayList<>());
            }
            
            List<GatewayCache.GatewayNode> dimCache = celestialCache.get(dim);
            
            for (PktSyncPrivateGateways.PrivateGatewayNode node : privateGateways) {
                boolean canAccess = false;
                
                if (node.owner != null && node.owner.equals(playerUUID)) {
                    canAccess = true;
                } else if (node.authorizedPlayers != null && node.authorizedPlayers.contains(playerUUID)) {
                    canAccess = true;
                }
                
                if (canAccess) {
                    String key = dim + ":" + node.pos.toLong();
                    
                    boolean exists = false;
                    for (GatewayCache.GatewayNode existing : dimCache) {
                        if (existing.equals(node.pos)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (!exists || needReinject) {
                        if (!exists) {
                            GatewayCache.GatewayNode publicNode = new GatewayCache.GatewayNode(node.pos, node.display);
                            dimCache.add(publicNode);
                            cacheUpdated = true;
                        }
                        injectedGateways.add(key);
                    }
                }
            }
        }
        
        if (cacheUpdated || needReinject) {
            CelestialGatewaySystem.instance.updateClientCache(celestialCache);
            needReinject = false;
        }
    }
}
