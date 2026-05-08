package nyonio.common.item;

import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemShrunkGateway extends Item {

    public ItemShrunkGateway() {
        setMaxStackSize(1);
        setCreativeTab(RegistryItems.creativeTabAstralSorcery);
        setUnlocalizedName("shrunkgateway.shrunkgateway");
        setRegistryName("shrunkgateway");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.shrunkgateway.shrunkgateway"));
    }
}
