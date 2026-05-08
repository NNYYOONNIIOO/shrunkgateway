package nyonio.common.event;

import hellfirepvp.astralsorcery.common.item.ItemKnowledgeShare;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nyonio.common.block.BlockPrivateGateway;
import nyonio.common.tile.TilePrivateGateway;

import java.util.UUID;

public class KnowledgeScrollBindingHandler {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItem(event.getHand());
        
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ItemKnowledgeShare)) {
            return;
        }

        ItemKnowledgeShare knowledgeShare = (ItemKnowledgeShare) heldItem.getItem();
        
        if (knowledgeShare.getKnowledge(heldItem) == null) {
            return;
        }

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Block block = world.getBlockState(pos).getBlock();
        
        if (!(block instanceof BlockPrivateGateway)) {
            return;
        }

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TilePrivateGateway)) {
            return;
        }

        TilePrivateGateway gateway = (TilePrivateGateway) te;
        
        if (!player.getUniqueID().equals(gateway.getOwner())) {
            player.sendMessage(new TextComponentTranslation("message.shrunkgateway.binding.not_owner"));
            return;
        }

        String knowledgeOwnerName = knowledgeShare.getKnowledgeOwnerName(heldItem);
        if (knowledgeOwnerName == null) {
            player.sendMessage(new TextComponentTranslation("message.shrunkgateway.binding.no_player"));
            return;
        }

        UUID knowledgeOwnerUUID = null;
        NBTTagCompound compound = NBTHelper.getPersistentData(heldItem);
        if (compound.hasUniqueId("knowledgeOwnerUUID")) {
            knowledgeOwnerUUID = compound.getUniqueId("knowledgeOwnerUUID");
        }
        
        if (knowledgeOwnerUUID == null) {
            player.sendMessage(new TextComponentTranslation("message.shrunkgateway.binding.no_player"));
            return;
        }

        if (knowledgeOwnerUUID.equals(player.getUniqueID())) {
            player.sendMessage(new TextComponentTranslation("message.shrunkgateway.binding.self_binding"));
            return;
        }

        if (gateway.isPlayerAuthorized(knowledgeOwnerUUID)) {
            player.sendMessage(new TextComponentTranslation("message.shrunkgateway.binding.already_authorized", knowledgeOwnerName));
            return;
        }

        boolean success = gateway.authorizePlayer(knowledgeOwnerUUID);
        
        if (success) {
            player.sendMessage(new TextComponentTranslation("message.shrunkgateway.binding.success", knowledgeOwnerName));
        } else {
            player.sendMessage(new TextComponentTranslation("message.shrunkgateway.binding.failed"));
        }
    }
}
