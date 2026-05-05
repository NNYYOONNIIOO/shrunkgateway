package nyonio.common.item;

import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import net.minecraft.item.Item;

public class ItemShrunkGateway extends Item {

    public ItemShrunkGateway() {
        setMaxStackSize(1);
        setCreativeTab(RegistryItems.creativeTabAstralSorcery);
        setUnlocalizedName("shrunkgateway.shrunkgateway");
        setRegistryName("shrunkgateway");
    }
}
