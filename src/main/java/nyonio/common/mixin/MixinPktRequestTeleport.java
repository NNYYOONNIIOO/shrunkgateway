package nyonio.common.mixin;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.network.packet.client.PktRequestTeleport;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import nyonio.common.data.PrivateGatewayDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(value = PktRequestTeleport.class, remap = false, priority = 999)
public class MixinPktRequestTeleport {

    private static Field dimIdField;
    private static Field posField;

    static {
        try {
            dimIdField = PktRequestTeleport.class.getDeclaredField("dimId");
            dimIdField.setAccessible(true);
            
            posField = PktRequestTeleport.class.getDeclaredField("pos");
            posField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "onMessage", at = @At("HEAD"), cancellable = true, remap = false)
    private void onMessageHead(PktRequestTeleport message, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraftforge.fml.common.network.simpleimpl.IMessage> cir) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            try {
                EntityPlayer request = ctx.getServerHandler().player;
                TileCelestialGateway gate = MiscUtils.getTileAt(request.world, Vector3.atEntityCorner(request).toBlockPos(), TileCelestialGateway.class, false);
                
                if(gate != null && gate.hasMultiblock() && gate.doesSeeSky()) {
                    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                    if (server != null) {
                        int dimId = dimIdField.getInt(message);
                        BlockPos pos = (BlockPos) posField.get(message);
                        
                        World to = server.getWorld(dimId);
                        if (to != null) {
                            GatewayCache data = WorldCacheManager.getOrLoadData(to, WorldCacheManager.SaveKey.GATEWAY_DATA);
                            
                            boolean canTeleport = MiscUtils.contains(data.getGatewayPositions(), gatewayNode -> gatewayNode.equals(pos));
                            
                            if (!canTeleport) {
                                PrivateGatewayDataManager.PrivateGatewayWorldData privateData = 
                                    PrivateGatewayDataManager.getOrCreate(to);
                                
                                for (java.util.Map.Entry<java.util.UUID, List<PrivateGatewayDataManager.PrivateGatewayNode>> entry : 
                                    privateData.getAllGateways().entrySet()) {
                                    
                                    for (PrivateGatewayDataManager.PrivateGatewayNode node : entry.getValue()) {
                                        if (node.equals(pos)) {
                                            if (entry.getKey().equals(request.getUniqueID()) || 
                                                node.isPlayerAuthorized(request.getUniqueID())) {
                                                canTeleport = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (canTeleport) break;
                                }
                            }
                            
                            if (canTeleport) {
                                AstralSorcery.proxy.scheduleDelayed(() -> MiscUtils.transferEntityTo(request, dimId, pos));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        cir.setReturnValue(null);
    }
}
