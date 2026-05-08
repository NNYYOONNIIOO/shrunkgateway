package nyonio.common.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import nyonio.ShrunkGateway;

import javax.annotation.Nonnull;
import java.util.*;

public class ResonanceGatewayDataManager extends WorldSavedData {

    private static final String DATA_NAME = ShrunkGateway.MODID + "_resonance_gateways";
    
    private final Map<UUID, List<ResonanceGatewayNode>> gatewaysByPairId = new HashMap<>();
    private final Map<BlockPos, ResonanceGatewayNode> gatewaysByPos = new HashMap<>();
    
    public ResonanceGatewayDataManager(String name) {
        super(name);
    }
    
    public ResonanceGatewayDataManager() {
        super(DATA_NAME);
    }
    
    public static ResonanceGatewayDataManager get(World world) {
        MapStorage storage = world.getMapStorage();
        ResonanceGatewayDataManager instance = (ResonanceGatewayDataManager) storage.getOrLoadData(ResonanceGatewayDataManager.class, DATA_NAME);
        
        if (instance == null) {
            instance = new ResonanceGatewayDataManager();
            storage.setData(DATA_NAME, instance);
        }
        
        return instance;
    }
    
    public void addGateway(BlockPos pos, int dimension, UUID pairId) {
        ResonanceGatewayNode node = new ResonanceGatewayNode(pos, dimension, pairId);
        
        gatewaysByPos.put(pos, node);
        
        gatewaysByPairId.computeIfAbsent(pairId, k -> new ArrayList<>()).add(node);
        
        markDirty();
    }
    
    public void removeGateway(BlockPos pos) {
        ResonanceGatewayNode node = gatewaysByPos.remove(pos);
        
        if (node != null) {
            List<ResonanceGatewayNode> pairList = gatewaysByPairId.get(node.pairId);
            if (pairList != null) {
                pairList.remove(node);
                if (pairList.isEmpty()) {
                    gatewaysByPairId.remove(node.pairId);
                }
            }
            markDirty();
        }
    }
    
    public ResonanceGatewayNode getPairedGateway(BlockPos excludePos, UUID pairId) {
        List<ResonanceGatewayNode> pairList = gatewaysByPairId.get(pairId);
        if (pairList == null) return null;
        
        for (ResonanceGatewayNode node : pairList) {
            if (!node.pos.equals(excludePos)) {
                return node;
            }
        }
        
        return null;
    }
    
    public UUID getPairId(BlockPos pos) {
        ResonanceGatewayNode node = gatewaysByPos.get(pos);
        return node != null ? node.pairId : null;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        gatewaysByPairId.clear();
        gatewaysByPos.clear();
        
        NBTTagList gatewayList = nbt.getTagList("gateways", 10);
        for (int i = 0; i < gatewayList.tagCount(); i++) {
            NBTTagCompound gatewayTag = gatewayList.getCompoundTagAt(i);
            
            BlockPos pos = new BlockPos(
                gatewayTag.getInteger("x"),
                gatewayTag.getInteger("y"),
                gatewayTag.getInteger("z")
            );
            int dimension = gatewayTag.getInteger("dim");
            UUID pairId = gatewayTag.getUniqueId("pairId");
            
            ResonanceGatewayNode node = new ResonanceGatewayNode(pos, dimension, pairId);
            gatewaysByPos.put(pos, node);
            gatewaysByPairId.computeIfAbsent(pairId, k -> new ArrayList<>()).add(node);
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList gatewayList = new NBTTagList();
        
        for (ResonanceGatewayNode node : gatewaysByPos.values()) {
            NBTTagCompound gatewayTag = new NBTTagCompound();
            gatewayTag.setInteger("x", node.pos.getX());
            gatewayTag.setInteger("y", node.pos.getY());
            gatewayTag.setInteger("z", node.pos.getZ());
            gatewayTag.setInteger("dim", node.dimension);
            gatewayTag.setUniqueId("pairId", node.pairId);
            gatewayList.appendTag(gatewayTag);
        }
        
        compound.setTag("gateways", gatewayList);
        return compound;
    }
    
    public static class ResonanceGatewayNode {
        public final BlockPos pos;
        public final int dimension;
        public final UUID pairId;
        
        public ResonanceGatewayNode(BlockPos pos, int dimension, UUID pairId) {
            this.pos = pos;
            this.dimension = dimension;
            this.pairId = pairId;
        }
    }
}
