package nyonio.common.data;

import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import nyonio.ShrunkGateway;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PrivateGatewayDataManager {

    private static final String DATA_DIR_NAME = "ShrunkGatewayData";
    private static final String DATA_FILE_NAME = "private_gateways.dat";
    
    private static Map<Integer, PrivateGatewayWorldData> worldDataCache = new HashMap<>();
    private static File saveDir;

    public static PrivateGatewayWorldData getOrCreate(World world) {
        if (world.isRemote) {
            throw new IllegalArgumentException("Cannot access private gateway data on client side!");
        }
        
        int dim = world.provider.getDimension();
        if (!worldDataCache.containsKey(dim)) {
            PrivateGatewayWorldData data = loadFromFile(world);
            worldDataCache.put(dim, data);
            data.onLoad(world);
        }
        
        return worldDataCache.get(dim);
    }

    public static Map<Integer, PrivateGatewayWorldData> getAllWorldData() {
        return new HashMap<>(worldDataCache);
    }

    public static void loadAllDimensions(net.minecraft.server.MinecraftServer server) {
        for (net.minecraft.world.WorldServer world : server.worlds) {
            getOrCreate(world);
        }
        
        loadAllDimensionFiles(server);
    }

    private static void loadAllDimensionFiles(net.minecraft.server.MinecraftServer server) {
        File worldDir = server.getEntityWorld().getSaveHandler().getWorldDirectory();
        File dataDir = new File(worldDir, DATA_DIR_NAME);
        
        if (!dataDir.exists()) {
            return;
        }
        
        File[] dimDirs = dataDir.listFiles(File::isDirectory);
        if (dimDirs == null) return;
        
        for (File dimDir : dimDirs) {
            String dirName = dimDir.getName();
            
            if (dirName.startsWith("DIM_")) {
                try {
                    int dim = Integer.parseInt(dirName.substring(4));
                    
                    if (!worldDataCache.containsKey(dim)) {
                        File dataFile = new File(dimDir, DATA_FILE_NAME);
                        
                        if (dataFile.exists()) {
                            try {
                                NBTTagCompound compound = CompressedStreamTools.read(dataFile);
                                PrivateGatewayWorldData data = new PrivateGatewayWorldData();
                                data.readFromNBT(compound);
                                worldDataCache.put(dim, data);
                            } catch (IOException e) {
                                ShrunkGateway.LOGGER.error("Failed to load private gateway data for dimension {}", dim, e);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid directory names
                }
            }
        }
    }

    private static PrivateGatewayWorldData loadFromFile(World world) {
        File dataFile = getDataFile(world);
        
        if (!dataFile.exists()) {
            return new PrivateGatewayWorldData();
        }
        
        try {
            NBTTagCompound compound = CompressedStreamTools.read(dataFile);
            PrivateGatewayWorldData data = new PrivateGatewayWorldData();
            data.readFromNBT(compound);
            return data;
        } catch (IOException e) {
            ShrunkGateway.LOGGER.error("Failed to load private gateway data for dimension {}", world.provider.getDimension(), e);
            return new PrivateGatewayWorldData();
        }
    }

    public static void saveToFile(World world) {
        if (world.isRemote) return;
        
        int dim = world.provider.getDimension();
        PrivateGatewayWorldData data = worldDataCache.get(dim);
        if (data == null) return;
        
        File dataFile = getDataFile(world);
        try {
            NBTTagCompound compound = new NBTTagCompound();
            data.writeToNBT(compound);
            CompressedStreamTools.write(compound, dataFile);
        } catch (IOException e) {
            ShrunkGateway.LOGGER.error("Failed to save private gateway data for dimension {}", dim, e);
        }
    }

    private static File getDataFile(World world) {
        if (saveDir == null) {
            saveDir = new File(world.getSaveHandler().getWorldDirectory(), DATA_DIR_NAME);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
        }
        
        File dimDir = new File(saveDir, "DIM_" + world.provider.getDimension());
        if (!dimDir.exists()) {
            dimDir.mkdirs();
        }
        
        return new File(dimDir, DATA_FILE_NAME);
    }

    public static void clearCache() {
        worldDataCache.clear();
        saveDir = null;
    }

    public static void markDirty(World world) {
        if (world.isRemote) return;
        
        int dim = world.provider.getDimension();
        PrivateGatewayWorldData data = worldDataCache.get(dim);
        if (data != null) {
            data.markDirty();
        }
    }

    public static class PrivateGatewayWorldData {

        private Map<UUID, List<PrivateGatewayNode>> gatewaysByOwner = new HashMap<>();
        private boolean dirty = false;

        public void addGateway(World world, BlockPos pos, String display, UUID owner) {
            if (owner == null) return;
            
            gatewaysByOwner.computeIfAbsent(owner, k -> new ArrayList<>());
            List<PrivateGatewayNode> ownerGateways = gatewaysByOwner.get(owner);
            
            PrivateGatewayNode node = new PrivateGatewayNode(pos, display, owner);
            if (!ownerGateways.contains(node)) {
                ownerGateways.add(node);
                markDirty();
                saveToFile(world);
            }
        }

        public void removeGateway(World world, BlockPos pos, UUID owner) {
            if (owner == null) return;
            
            List<PrivateGatewayNode> ownerGateways = gatewaysByOwner.get(owner);
            if (ownerGateways != null && ownerGateways.removeIf(node -> node.equals(pos))) {
                markDirty();
                saveToFile(world);
            }
        }

        public List<PrivateGatewayNode> getGatewaysForOwner(UUID owner) {
            return new ArrayList<>(gatewaysByOwner.getOrDefault(owner, Collections.emptyList()));
        }

        public PrivateGatewayNode getGateway(UUID owner, BlockPos pos) {
            List<PrivateGatewayNode> gateways = gatewaysByOwner.get(owner);
            if (gateways != null) {
                for (PrivateGatewayNode node : gateways) {
                    if (node.equals(pos)) {
                        return node;
                    }
                }
            }
            return null;
        }

        public boolean authorizePlayer(World world, BlockPos pos, UUID owner, UUID playerToAuthorize) {
            PrivateGatewayNode node = getGateway(owner, pos);
            if (node != null && !node.isPlayerAuthorized(playerToAuthorize)) {
                node.addAuthorizedPlayer(playerToAuthorize);
                markDirty();
                saveToFile(world);
                return true;
            }
            return false;
        }

        public boolean deauthorizePlayer(World world, BlockPos pos, UUID owner, UUID playerToDeauthorize) {
            PrivateGatewayNode node = getGateway(owner, pos);
            if (node != null && node.isPlayerAuthorized(playerToDeauthorize)) {
                node.removeAuthorizedPlayer(playerToDeauthorize);
                markDirty();
                saveToFile(world);
                return true;
            }
            return false;
        }

        public boolean isPlayerAuthorized(UUID owner, BlockPos pos, UUID playerUUID) {
            PrivateGatewayNode node = getGateway(owner, pos);
            return node != null && node.isPlayerAuthorized(playerUUID);
        }

        public List<PrivateGatewayNode> getGatewaysForAuthorizedPlayer(UUID playerUUID) {
            List<PrivateGatewayNode> authorizedGateways = new ArrayList<>();
            
            for (List<PrivateGatewayNode> gateways : gatewaysByOwner.values()) {
                for (PrivateGatewayNode node : gateways) {
                    if (node.isPlayerAuthorized(playerUUID)) {
                        authorizedGateways.add(node);
                    }
                }
            }
            
            return authorizedGateways;
        }

        public Map<UUID, List<PrivateGatewayNode>> getAllGateways() {
            Map<UUID, List<PrivateGatewayNode>> copy = new HashMap<>();
            for (Map.Entry<UUID, List<PrivateGatewayNode>> entry : gatewaysByOwner.entrySet()) {
                copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return copy;
        }

        public void onLoad(World world) {
        }

        public void readFromNBT(NBTTagCompound compound) {
            gatewaysByOwner.clear();
            
            NBTTagList ownerList = compound.getTagList("owners", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < ownerList.tagCount(); i++) {
                NBTTagCompound ownerTag = ownerList.getCompoundTagAt(i);
                UUID owner = ownerTag.getUniqueId("owner");
                
                NBTTagList gatewayList = ownerTag.getTagList("gateways", Constants.NBT.TAG_COMPOUND);
                List<PrivateGatewayNode> gateways = new ArrayList<>();
                
                for (int j = 0; j < gatewayList.tagCount(); j++) {
                    NBTTagCompound gatewayTag = gatewayList.getCompoundTagAt(j);
                    BlockPos pos = NBTHelper.readBlockPosFromNBT(gatewayTag);
                    String display = gatewayTag.getString("display");
                    PrivateGatewayNode node = new PrivateGatewayNode(pos, display, owner);
                    
                    if (gatewayTag.hasKey("authorizedPlayers")) {
                        NBTTagList authList = gatewayTag.getTagList("authorizedPlayers", Constants.NBT.TAG_COMPOUND);
                        Set<UUID> authorizedPlayers = new HashSet<>();
                        for (int k = 0; k < authList.tagCount(); k++) {
                            NBTTagCompound playerTag = authList.getCompoundTagAt(k);
                            if (playerTag.hasUniqueId("player")) {
                                authorizedPlayers.add(playerTag.getUniqueId("player"));
                            }
                        }
                        node.setAuthorizedPlayers(authorizedPlayers);
                    }
                    
                    gateways.add(node);
                }
                
                gatewaysByOwner.put(owner, gateways);
            }
        }

        public void writeToNBT(NBTTagCompound compound) {
            NBTTagList ownerList = new NBTTagList();
            
            for (Map.Entry<UUID, List<PrivateGatewayNode>> entry : gatewaysByOwner.entrySet()) {
                NBTTagCompound ownerTag = new NBTTagCompound();
                ownerTag.setUniqueId("owner", entry.getKey());
                
                NBTTagList gatewayList = new NBTTagList();
                for (PrivateGatewayNode node : entry.getValue()) {
                    NBTTagCompound gatewayTag = new NBTTagCompound();
                    NBTHelper.writeBlockPosToNBT(node, gatewayTag);
                    gatewayTag.setString("display", node.display);
                    
                    Set<UUID> authorizedPlayers = node.getAuthorizedPlayers();
                    if (!authorizedPlayers.isEmpty()) {
                        NBTTagList authList = new NBTTagList();
                        for (UUID playerUUID : authorizedPlayers) {
                            NBTTagCompound playerTag = new NBTTagCompound();
                            playerTag.setUniqueId("player", playerUUID);
                            authList.appendTag(playerTag);
                        }
                        gatewayTag.setTag("authorizedPlayers", authList);
                    }
                    
                    gatewayList.appendTag(gatewayTag);
                }
                
                ownerTag.setTag("gateways", gatewayList);
                ownerList.appendTag(ownerTag);
            }
            
            compound.setTag("owners", ownerList);
        }

        public void markDirty() {
            this.dirty = true;
        }

        public boolean isDirty() {
            return dirty;
        }
    }

    public static class PrivateGatewayNode extends BlockPos {
        public final String display;
        public final UUID owner;
        private Set<UUID> authorizedPlayers = new HashSet<>();

        public PrivateGatewayNode(BlockPos pos, String display, UUID owner) {
            super(pos.getX(), pos.getY(), pos.getZ());
            this.display = display;
            this.owner = owner;
        }

        public Set<UUID> getAuthorizedPlayers() {
            return new HashSet<>(authorizedPlayers);
        }

        public void addAuthorizedPlayer(UUID playerUUID) {
            authorizedPlayers.add(playerUUID);
        }

        public void removeAuthorizedPlayer(UUID playerUUID) {
            authorizedPlayers.remove(playerUUID);
        }

        public boolean isPlayerAuthorized(UUID playerUUID) {
            return authorizedPlayers.contains(playerUUID);
        }

        public void setAuthorizedPlayers(Set<UUID> players) {
            authorizedPlayers.clear();
            authorizedPlayers.addAll(players);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BlockPos)) return false;
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
