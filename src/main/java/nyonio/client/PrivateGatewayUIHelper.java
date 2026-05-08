package nyonio.client;

import hellfirepvp.astralsorcery.client.util.UIGateway;
import hellfirepvp.astralsorcery.common.auxiliary.CelestialGatewaySystem;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import nyonio.common.network.PktSyncPrivateGateways;
import nyonio.common.network.PrivateGatewayCache;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrivateGatewayUIHelper {

    private static Constructor<?> gatewayEntryConstructor = null;
    private static Field pitchField = null;
    private static Field yawField = null;

    static {
        try {
            gatewayEntryConstructor = UIGateway.GatewayEntry.class.getDeclaredConstructor(
                    GatewayCache.GatewayNode.class, int.class, Vector3.class);
            gatewayEntryConstructor.setAccessible(true);

            pitchField = UIGateway.GatewayEntry.class.getDeclaredField("pitch");
            pitchField.setAccessible(true);

            yawField = UIGateway.GatewayEntry.class.getDeclaredField("yaw");
            yawField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<UIGateway.GatewayEntry> gatherPrivateGateways(World world, BlockPos gatewayPos, Vector3 source, double sphereRadius) {
        List<UIGateway.GatewayEntry> entries = new ArrayList<>();
        
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return entries;
        
        UUID playerUUID = player.getUniqueID();
        int dimid = world.provider.getDimension();
        
        PrivateGatewayCache privateCache = PrivateGatewayCache.instance;
        
        List<PktSyncPrivateGateways.PrivateGatewayNode> sameDimensionPrivate = privateCache.getGatewaysForWorld(world, Side.CLIENT);
        
        if (sameDimensionPrivate != null) {
            gatherPrivateStars(entries, dimid, sameDimensionPrivate, true, sphereRadius, source, playerUUID);
        }
        
        for (Map.Entry<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> entry : privateCache.getGatewayCache(Side.CLIENT).entrySet()) {
            if (entry.getKey() == dimid) continue;
            gatherPrivateStars(entries, entry.getKey(), entry.getValue(), false, sphereRadius, source, playerUUID);
        }
        
        return entries;
    }

    private static void gatherPrivateStars(List<UIGateway.GatewayEntry> entries, int dimId, List<PktSyncPrivateGateways.PrivateGatewayNode> positions, boolean sameWorld, double sphereRadius, Vector3 gatePosition, UUID playerUUID) {
        for (PktSyncPrivateGateways.PrivateGatewayNode node : positions) {
            if (node.owner == null || !node.owner.equals(playerUUID)) continue;
            
            Vector3 otherPos = new Vector3(node.pos);
            if (sameWorld && otherPos.distance(gatePosition) < 16) continue;
            
            Vector3 direction = otherPos.subtract(gatePosition).normalize().multiply(sphereRadius);
            GatewayCache.GatewayNode gatewayNode = new GatewayCache.GatewayNode(node.pos, node.display);
            
            try {
                UIGateway.GatewayEntry potentialEntry = (UIGateway.GatewayEntry) gatewayEntryConstructor.newInstance(gatewayNode, dimId, direction);
                
                if (sameWorld) {
                    float potentialPitch = pitchField.getFloat(potentialEntry);
                    float potentialYaw = yawField.getFloat(potentialEntry);
                    
                    UIGateway.GatewayEntry closest = null;
                    for (UIGateway.GatewayEntry otherEntry : entries) {
                        float otherPitch = pitchField.getFloat(otherEntry);
                        float otherYaw = yawField.getFloat(otherEntry);
                        
                        if (Math.abs(otherPitch - potentialPitch) < 10 &&
                                (Math.abs(otherYaw - potentialYaw) <= 10 || Math.abs(otherYaw - potentialYaw - 360F) <= 10)) {
                            if (closest == null || gatePosition.distanceSquared(otherEntry.relativePos) < gatePosition.distanceSquared(closest.relativePos)) {
                                closest = otherEntry;
                            }
                        }
                    }
                    if (closest == null) {
                        entries.add(potentialEntry);
                    }
                } else {
                    entries.add(potentialEntry);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
