package nyonio.common.item;

import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import nyonio.common.tile.TileResonanceGateway;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemResonantStarwheel extends Item {

    private static final TextFormatting[] COLORS = {
        TextFormatting.BLACK,      // 0
        TextFormatting.DARK_BLUE,  // 1
        TextFormatting.DARK_GREEN, // 2
        TextFormatting.DARK_AQUA,  // 3
        TextFormatting.DARK_RED,   // 4
        TextFormatting.DARK_PURPLE,// 5
        TextFormatting.GOLD,       // 6
        TextFormatting.GRAY,       // 7
        TextFormatting.DARK_GRAY,  // 8
        TextFormatting.BLUE,       // 9
        TextFormatting.GREEN,      // A
        TextFormatting.AQUA,       // B
        TextFormatting.RED,        // C
        TextFormatting.LIGHT_PURPLE,// D
        TextFormatting.YELLOW,     // E
        TextFormatting.WHITE       // F
    };
    
    private static final String[] SYMBOLS = {
        "ᔑ ", "ʖ ", "ᓵ ", "↸ ", "Ŀ ", "⎓ ", "ㅓ ", "〒 ",
        "⍑ ", "╎ ", "ᒷ ", "リ ", "フ ", "¡ ", "ᑑ ", "። "
    };

    public ItemResonantStarwheel() {
        setMaxStackSize(64);
        setCreativeTab(RegistryItems.creativeTabAstralSorcery);
        setUnlocalizedName("shrunkgateway.resonant_starwheel");
        setRegistryName("resonant_starwheel");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasUniqueId("pairId")) {
            UUID pairId = stack.getTagCompound().getUniqueId("pairId");
            tooltip.add(TextFormatting.GOLD + I18n.format("tooltip.shrunkgateway.constellation_hub.paired"));
            
            String uuidHex = pairId.toString().replace("-", "").toUpperCase();
            StringBuilder visualUUID = new StringBuilder();
            
            for (int i = 0; i < 16 && i < uuidHex.length(); i += 2) {
                int colorIndex = Character.digit(uuidHex.charAt(i), 16);
                int symbolIndex = Character.digit(uuidHex.charAt(i + 1), 16);
                
                if (colorIndex >= 0 && colorIndex < COLORS.length && 
                    symbolIndex >= 0 && symbolIndex < SYMBOLS.length) {
                    visualUUID.append(COLORS[colorIndex]);
                    visualUUID.append(SYMBOLS[symbolIndex]);
                }
            }
            
            tooltip.add(visualUUID.toString());
        } else {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.shrunkgateway.constellation_hub.unpaired"));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return EnumActionResult.SUCCESS;
        
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasUniqueId("pairId")) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.constellation_hub.not_paired");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return EnumActionResult.FAIL;
        }
        
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileResonanceGateway)) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.constellation_hub.not_gateway");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return EnumActionResult.FAIL;
        }
        
        TileResonanceGateway gateway = (TileResonanceGateway) te;
        UUID pairId = stack.getTagCompound().getUniqueId("pairId");
        
        if (gateway.getHubPairId() == null) {
            gateway.setHubPairId(pairId);
            
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.constellation_hub.bound_first");
            message.getStyle().setColor(TextFormatting.GREEN);
            player.sendMessage(message);
        } else if (gateway.getHubPairId().equals(pairId)) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.constellation_hub.already_linked");
            message.getStyle().setColor(TextFormatting.YELLOW);
            player.sendMessage(message);
            return EnumActionResult.SUCCESS;
        } else {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.constellation_hub.conflict");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return EnumActionResult.FAIL;
        }
        
        stack.shrink(1);
        
        return EnumActionResult.SUCCESS;
    }
    
    public static UUID getPairId(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasUniqueId("pairId")) {
            return stack.getTagCompound().getUniqueId("pairId");
        }
        return null;
    }
    
    public static boolean isPaired(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasUniqueId("pairId");
    }
}
