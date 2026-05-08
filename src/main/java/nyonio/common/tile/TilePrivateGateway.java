package nyonio.common.tile;

import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.structure.array.PatternBlockArray;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import nyonio.common.data.PrivateGatewayDataManager;
import nyonio.common.event.PlayerJoinHandler;
import nyonio.common.network.PrivateGatewayCache;
import nyonio.common.registry.RegistryStructures;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TilePrivateGateway extends TileCelestialGateway {

    private UUID ownerUUID;
    private boolean privateGatewayRegistered = false;

    @Override
    public void update() {
        if(world.isRemote) {
            super.update();
        } else {
            super.update();
            
            GatewayCache publicCache = WorldCacheManager.getOrLoadData(world, WorldCacheManager.SaveKey.GATEWAY_DATA);
            publicCache.removePosition(world, pos);
            
            if(hasMultiblock() && doesSeeSky()) {
                if(!privateGatewayRegistered) {
                    String display = hasCustomName() ? getName() : "";
                    
                    PrivateGatewayDataManager.PrivateGatewayWorldData data = 
                        PrivateGatewayDataManager.getOrCreate(world);
                    data.addGateway(world, pos, display, ownerUUID);
                    
                    PrivateGatewayCache.instance.addPosition(world, pos, display, ownerUUID);
                    privateGatewayRegistered = true;
                    
                    if (ownerUUID != null) {
                        PrivateGatewayCache.instance.syncToOwner(ownerUUID);
                    }
                }
            } else {
                if(privateGatewayRegistered) {
                    PrivateGatewayDataManager.PrivateGatewayWorldData data = 
                        PrivateGatewayDataManager.getOrCreate(world);
                    data.removeGateway(world, pos, ownerUUID);
                    
                    PrivateGatewayCache.instance.removePosition(world, pos, ownerUUID);
                    privateGatewayRegistered = false;
                    
                    if (ownerUUID != null) {
                        PrivateGatewayCache.instance.syncToOwner(ownerUUID);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public PatternBlockArray getRequiredStructure() {
        return RegistryStructures.patternPrivateGateway;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        if (compound.hasUniqueId("owner")) {
            this.ownerUUID = compound.getUniqueId("owner");
        }
        this.privateGatewayRegistered = compound.getBoolean("privateRegistered");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        if (this.ownerUUID != null) {
            compound.setUniqueId("owner", this.ownerUUID);
        }
        compound.setBoolean("privateRegistered", this.privateGatewayRegistered);
    }

    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
        markForUpdate();
    }

    public UUID getOwner() {
        return ownerUUID;
    }

    public boolean isPrivateGatewayRegistered() {
        return privateGatewayRegistered;
    }

    public boolean authorizePlayer(UUID playerToAuthorize) {
        if (world.isRemote || ownerUUID == null || !privateGatewayRegistered) {
            return false;
        }
        
        PrivateGatewayDataManager.PrivateGatewayWorldData data = 
            PrivateGatewayDataManager.getOrCreate(world);
        boolean success = data.authorizePlayer(world, pos, ownerUUID, playerToAuthorize);
        
        if (success) {
            PrivateGatewayDataManager.PrivateGatewayNode node = data.getGateway(ownerUUID, pos);
            if (node != null) {
                PrivateGatewayCache.instance.addPosition(world, pos, node.display, ownerUUID, node.getAuthorizedPlayers());
            }
            
            syncToAllAuthorizedPlayers();
        }
        
        return success;
    }

    public boolean deauthorizePlayer(UUID playerToDeauthorize) {
        if (world.isRemote || ownerUUID == null || !privateGatewayRegistered) {
            return false;
        }
        
        PrivateGatewayDataManager.PrivateGatewayWorldData data = 
            PrivateGatewayDataManager.getOrCreate(world);
        boolean success = data.deauthorizePlayer(world, pos, ownerUUID, playerToDeauthorize);
        
        if (success) {
            PrivateGatewayDataManager.PrivateGatewayNode node = data.getGateway(ownerUUID, pos);
            if (node != null) {
                PrivateGatewayCache.instance.addPosition(world, pos, node.display, ownerUUID, node.getAuthorizedPlayers());
            }
            
            syncToAllAuthorizedPlayers();
        }
        
        return success;
    }

    private void syncToAllAuthorizedPlayers() {
        if (ownerUUID == null) return;
        
        PlayerJoinHandler.syncAllDimensionsToPlayer(ownerUUID);
        
        PrivateGatewayDataManager.PrivateGatewayWorldData data = 
            PrivateGatewayDataManager.getOrCreate(world);
        PrivateGatewayDataManager.PrivateGatewayNode node = data.getGateway(ownerUUID, pos);
        
        if (node != null) {
            for (UUID authorizedPlayer : node.getAuthorizedPlayers()) {
                PlayerJoinHandler.syncAllDimensionsToPlayer(authorizedPlayer);
            }
        }
    }

    public boolean isPlayerAuthorized(UUID playerUUID) {
        if (ownerUUID == null) {
            return false;
        }
        
        if (playerUUID.equals(ownerUUID)) {
            return true;
        }
        
        if (world.isRemote) {
            return PrivateGatewayCache.instance.isPlayerAuthorized(world, pos, ownerUUID, playerUUID);
        } else {
            PrivateGatewayDataManager.PrivateGatewayWorldData data = 
                PrivateGatewayDataManager.getOrCreate(world);
            return data.isPlayerAuthorized(ownerUUID, pos, playerUUID);
        }
    }
}
