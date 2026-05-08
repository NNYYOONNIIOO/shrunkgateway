package nyonio.common.registry;

import nyonio.ShrunkGateway;
import nyonio.common.entity.EntityStardust;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class RegistryEntities {

    private static int entityID = 0;

    public static void registerEntities() {
        registerEntity(EntityStardust.class, "entity_stardust");
    }

    private static void registerEntity(Class<? extends Entity> entityClass, String name) {
        EntityRegistry.registerModEntity(
            new ResourceLocation(ShrunkGateway.MODID, name),
            entityClass,
            name,
            entityID++,
            ShrunkGateway.instance,
            64,
            3,
            true
        );
    }
}
