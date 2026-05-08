package nyonio.client.event;

import hellfirepvp.astralsorcery.client.effect.EffectHandler;
import hellfirepvp.astralsorcery.client.util.UIGateway;
import nyonio.client.PrivateGatewayUIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.List;

public class ClientPrivateGatewayHandler {

    private static Field gatewayEntriesField = null;

    static {
        try {
            gatewayEntriesField = UIGateway.class.getDeclaredField("gatewayEntries");
            gatewayEntriesField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().player == null) return;

        UIGateway ui = EffectHandler.getInstance().getUiGateway();
        if (ui == null) return;

        World world = Minecraft.getMinecraft().player.world;
        if (world == null) return;

        try {
            if (gatewayEntriesField != null) {
                @SuppressWarnings("unchecked")
                List<UIGateway.GatewayEntry> entries = (List<UIGateway.GatewayEntry>) gatewayEntriesField.get(ui);
                if (entries != null) {
                    BlockPos gatewayPos = getGatewayPos(ui);
                    double radius = ui.getRadius();
                    
                    List<UIGateway.GatewayEntry> privateEntries = PrivateGatewayUIHelper.gatherPrivateGateways(
                            world, gatewayPos, ui.getPos(), radius);
                    
                    for (UIGateway.GatewayEntry entry : privateEntries) {
                        if (!entries.contains(entry)) {
                            entries.add(entry);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private BlockPos getGatewayPos(UIGateway ui) {
        try {
            Field gatewayPosField = UIGateway.class.getDeclaredField("gatewayPos");
            gatewayPosField.setAccessible(true);
            return (BlockPos) gatewayPosField.get(ui);
        } catch (Exception e) {
            return BlockPos.ORIGIN;
        }
    }
}
