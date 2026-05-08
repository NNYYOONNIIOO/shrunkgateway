package nyonio.client.event;

import nyonio.common.network.PacketHandler;
import nyonio.common.network.PktShrunkGatewayTeleport;
import nyonio.common.network.PktSyncPrivateGateways;
import nyonio.common.registry.RegistryItems;
import hellfirepvp.astralsorcery.client.effect.EffectHandler;
import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.fx.EntityFXFacingParticle;
import hellfirepvp.astralsorcery.client.util.UIGateway;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import nyonio.common.data.PrivateGatewayDataManager;
import nyonio.common.network.PrivateGatewayCache;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientShrunkGatewayHandler {

    public static UIGateway.GatewayEntry focusingEntry = null;
    public static int focusTicks = 0;
    private float fovPre = 70F;
    private boolean fovModified = false;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null || mc.world == null) {
            resetState();
            return;
        }

        EntityPlayer player = mc.player;
        ItemStack heldItem = player.getHeldItemMainhand();

        boolean isHoldingGateway = !heldItem.isEmpty() && heldItem.getItem() == RegistryItems.shrunkGateway;

        if(isHoldingGateway) {
            Vector3 playerPos = Vector3.atEntityCorner(player).addY(1.62);
            BlockPos gatewayPos = playerPos.toBlockPos();
            EffectHandler.getInstance().requestGatewayUIFor(player.world, gatewayPos, playerPos, 5.5);
            
            UIGateway ui = EffectHandler.getInstance().getUiGateway();
            if(ui != null) {
                // 添加私人天辉星门到 UIGateway
                addPrivateGatewaysToUI(ui, player, 5.5);
                
                handleGatewayInteraction(player, ui);
            }
        } else {
            resetState();
        }
    }

    private void handleGatewayInteraction(EntityPlayer player, UIGateway ui) {
        Minecraft mc = Minecraft.getMinecraft();
        
        UIGateway.GatewayEntry entry = ui.findMatchingEntry(
            MathHelper.wrapDegrees(player.rotationYaw),
            MathHelper.wrapDegrees(player.rotationPitch)
        );

        if(entry == null) {
            focusingEntry = null;
            focusTicks = 0;
        } else {
            if(!mc.gameSettings.keyBindUseItem.isKeyDown() && !player.isSneaking()) {
                focusTicks = 0;
                focusingEntry = null;
            } else {
                if(focusingEntry != null) {
                    if(!entry.equals(focusingEntry)) {
                        focusingEntry = null;
                        focusTicks = 0;
                    } else {
                        focusTicks++;
                    }
                } else {
                    focusingEntry = entry;
                    focusTicks = 0;
                }
            }
        }

        if(focusingEntry != null && focusTicks > 40) {
            Vector3 dir = focusingEntry.relativePos.clone().add(ui.getPos()).subtract(Vector3.atEntityCorner(player).addY(1.62));
            Vector3 mov = dir.clone().normalize().multiply(0.25F).negate();
            Vector3 pos = focusingEntry.relativePos.clone().add(ui.getPos());

            for(Vector3 v : MiscUtils.getCirclePositions(pos, dir, EffectHandler.STATIC_EFFECT_RAND.nextFloat() * 0.3 + 0.2, EffectHandler.STATIC_EFFECT_RAND.nextInt(20) + 30)) {
                EntityFXFacingParticle p = EffectHelper.genericGatewayFlareParticle(v.getX(), v.getY(), v.getZ());
                Vector3 m = mov.clone().multiply(0.5 + EffectHandler.STATIC_EFFECT_RAND.nextFloat() * 0.5);
                p.gravity(0.004).scale(0.1F).motion(m.getX(), m.getY(), m.getZ());
                switch(EffectHandler.STATIC_EFFECT_RAND.nextInt(4)) {
                    case 0:
                        p.setColor(Color.WHITE);
                        break;
                    case 1:
                        p.setColor(new Color(0x69B5FF));
                        break;
                    case 2:
                        p.setColor(new Color(0x0078FF));
                        break;
                    default:
                        break;
                }
            }

            if(focusTicks > 95) {
                player.setSneaking(false);
                PacketHandler.INSTANCE.sendToServer(new PktShrunkGatewayTeleport(focusingEntry.originalDimId, focusingEntry.originalBlockPos));
                focusTicks = 0;
                focusingEntry = null;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void onRenderTransform(TickEvent.RenderTickEvent event) {
        UIGateway ui = EffectHandler.getInstance().getUiGateway();
        if(ui != null) {
            if(event.phase == TickEvent.Phase.START) {
                if(focusTicks < 80) {
                    if(fovModified) {
                        Minecraft.getMinecraft().gameSettings.fovSetting = fovPre;
                        fovModified = false;
                    }
                    return;
                }
                if(!fovModified) {
                    fovPre = Minecraft.getMinecraft().gameSettings.fovSetting;
                    fovModified = true;
                }
                float percDone = 1F - ((focusTicks - 80F + event.renderTickTime) / 15F);
                float targetFov = 10F;
                float diff = fovPre - targetFov;
                Minecraft.getMinecraft().gameSettings.fovSetting = Math.max(targetFov, targetFov + diff * percDone);
            } else {
                if(fovModified) {
                    Minecraft.getMinecraft().gameSettings.fovSetting = fovPre;
                }
            }
        }
    }

    private void resetState() {
        focusingEntry = null;
        focusTicks = 0;
    }
    
    private void addPrivateGatewaysToUI(UIGateway ui, EntityPlayer player, double sphereRadius) {
        if(PrivateGatewayCache.instance == null) return;
        
        Map<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> privateGateways = 
            PrivateGatewayCache.instance.getGatewayCache(Side.CLIENT);
        
        if(privateGateways == null) return;
        
        Vector3 gatePosition = ui.getPos();
        int dimId = player.world.provider.getDimension();
        
        // 使用反射获取 gatewayEntries
        List<UIGateway.GatewayEntry> gatewayEntries = null;
        try {
            Field field = UIGateway.class.getDeclaredField("gatewayEntries");
            field.setAccessible(true);
            gatewayEntries = (List<UIGateway.GatewayEntry>) field.get(ui);
        } catch (Exception e) {
            return;
        }
        
        if(gatewayEntries == null) return;
        
        for(Map.Entry<Integer, List<PktSyncPrivateGateways.PrivateGatewayNode>> entry : privateGateways.entrySet()) {
            int nodeDim = entry.getKey();
            
            for(PktSyncPrivateGateways.PrivateGatewayNode node : entry.getValue()) {
                Vector3 otherPos = new Vector3(node.pos);
                
                // 跳过太近的星门（同维度且距离小于16）
                if(nodeDim == dimId && otherPos.distance(gatePosition) < 16) continue;
                
                Vector3 direction = otherPos.subtract(gatePosition).normalize().multiply(sphereRadius);
                
                UIGateway.GatewayEntry gatewayEntry = null;
                try {
                    java.lang.reflect.Constructor<UIGateway.GatewayEntry> constructor = 
                        UIGateway.GatewayEntry.class.getDeclaredConstructor(
                            GatewayCache.GatewayNode.class, int.class, Vector3.class
                        );
                    constructor.setAccessible(true);
                    gatewayEntry = constructor.newInstance(
                        new GatewayCache.GatewayNode(node.pos, node.display),
                        nodeDim,
                        direction
                    );
                } catch (Exception e) {
                    continue;
                }
                
                if(gatewayEntry == null) continue;
                
                // 检查是否已经有相同位置的条目
                boolean exists = false;
                for(UIGateway.GatewayEntry existing : gatewayEntries) {
                    if(existing.originalBlockPos.equals(node.pos)) {
                        exists = true;
                        break;
                    }
                }
                
                if(!exists) {
                    gatewayEntries.add(gatewayEntry);
                }
            }
        }
    }
}
