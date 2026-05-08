package nyonio.common.event;

import hellfirepvp.astralsorcery.common.item.ItemCraftingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nyonio.common.entity.EntityStardust;

public class StardustEntityHandler {

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityItem)) return;
        
        if (entity instanceof EntityStardust) return;
        
        EntityItem entityItem = (EntityItem) entity;
        ItemStack stack = entityItem.getItem();
        
        if (!stack.isEmpty() && stack.getItem() instanceof ItemCraftingComponent) {
            if (stack.getMetadata() == ItemCraftingComponent.MetaType.STARMETAL_INGOT.getMeta()) {
                EntityStardust newEntity = new EntityStardust(
                    event.getWorld(),
                    entityItem.posX,
                    entityItem.posY,
                    entityItem.posZ,
                    stack
                );
                newEntity.motionX = entityItem.motionX;
                newEntity.motionY = entityItem.motionY;
                newEntity.motionZ = entityItem.motionZ;
                newEntity.setPickupDelay(60);
                newEntity.setThrower(entityItem.getThrower());
                
                event.setCanceled(true);
                event.getWorld().spawnEntity(newEntity);
            }
        }
    }
}
