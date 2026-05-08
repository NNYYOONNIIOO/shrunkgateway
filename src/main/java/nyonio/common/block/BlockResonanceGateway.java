package nyonio.common.block;

import nyonio.common.tile.TileResonanceGateway;
import hellfirepvp.astralsorcery.common.block.BlockCelestialGateway;
import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockResonanceGateway extends BlockCelestialGateway {

    public BlockResonanceGateway() {
        super();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileResonanceGateway gateway = MiscUtils.getTileAt(worldIn, pos, TileResonanceGateway.class, true);
        if(gateway != null) {
            if (stack.hasDisplayName()) {
                gateway.setGatewayName(stack.getDisplayName());
            }
            if (placer instanceof EntityPlayerMP && !MiscUtils.isPlayerFakeMP((EntityPlayerMP) placer)) {
                gateway.setOwner(placer.getUniqueID());
                gateway.setPlacedBy(placer.getUniqueID());
            }
        }
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileResonanceGateway();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileResonanceGateway();
    }
}
