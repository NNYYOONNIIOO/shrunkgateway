package nyonio.common.entity;

import hellfirepvp.astralsorcery.common.item.ItemCraftingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import nyonio.common.registry.RegistryItems;

import java.util.List;
import java.util.UUID;

public class EntityStardust extends EntityItem {

    public EntityStardust(World world) {
        super(world);
    }

    public EntityStardust(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    @Override
    public boolean attackEntityFrom(DamageSource src, float dmg) {
        if (src.isExplosion()) {
            doExplosion();
            return false;
        }
        return super.attackEntityFrom(src, dmg);
    }

    private void doExplosion() {
        if (world.isRemote) return;
        
        ItemStack item = this.getItem();
        if (item.isEmpty() || !(item.getItem() instanceof ItemCraftingComponent)) return;
        if (item.getMetadata() != ItemCraftingComponent.MetaType.STARMETAL_INGOT.getMeta()) return;
        
        int count = item.getCount();
        
        if (count >= 2) {
            int pairs = count / 2;
            
            for (int i = 0; i < pairs; i++) {
                UUID pairId = UUID.randomUUID();
                
                ItemStack hub1 = new ItemStack(RegistryItems.resonantStarwheel);
                NBTTagCompound nbt1 = new NBTTagCompound();
                nbt1.setUniqueId("pairId", pairId);
                hub1.setTagCompound(nbt1);
                
                ItemStack hub2 = new ItemStack(RegistryItems.resonantStarwheel);
                NBTTagCompound nbt2 = new NBTTagCompound();
                nbt2.setUniqueId("pairId", pairId);
                hub2.setTagCompound(nbt2);
                
                EntityItem entity1 = new EntityItem(world, posX, posY, posZ, hub1);
                EntityItem entity2 = new EntityItem(world, posX, posY, posZ, hub2);
                
                world.spawnEntity(entity1);
                world.spawnEntity(entity2);
            }
            
            int remaining = count % 2;
            if (remaining > 0) {
                ItemStack remainingStack = item.copy();
                remainingStack.setCount(remaining);
                EntityItem remainingEntity = new EntityItem(world, posX, posY, posZ, remainingStack);
                world.spawnEntity(remainingEntity);
            }
            
            this.setDead();
        } else {
            AxisAlignedBB region = new AxisAlignedBB(
                posX - 4, posY - 4, posZ - 4,
                posX + 4, posY + 4, posZ + 4
            );
            
            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, region);
            
            for (Entity e : entities) {
                if (e instanceof EntityItem) {
                    ItemStack other = ((EntityItem) e).getItem();
                    if (!other.isEmpty() && other.getItem() instanceof ItemCraftingComponent) {
                        if (other.getMetadata() == ItemCraftingComponent.MetaType.STARMETAL_INGOT.getMeta()) {
                            UUID pairId = UUID.randomUUID();
                            
                            ItemStack hub1 = new ItemStack(RegistryItems.resonantStarwheel);
                            NBTTagCompound nbt1 = new NBTTagCompound();
                            nbt1.setUniqueId("pairId", pairId);
                            hub1.setTagCompound(nbt1);
                            
                            ItemStack hub2 = new ItemStack(RegistryItems.resonantStarwheel);
                            NBTTagCompound nbt2 = new NBTTagCompound();
                            nbt2.setUniqueId("pairId", pairId);
                            hub2.setTagCompound(nbt2);
                            
                            EntityItem entity1 = new EntityItem(world, posX, posY, posZ, hub1);
                            EntityItem entity2 = new EntityItem(world, posX, posY, posZ, hub2);
                            
                            world.spawnEntity(entity1);
                            world.spawnEntity(entity2);
                            
                            other.grow(-1);
                            if (other.getCount() <= 0) {
                                e.setDead();
                            }
                            
                            this.setDead();
                            break;
                        }
                    }
                }
            }
        }
    }
}
