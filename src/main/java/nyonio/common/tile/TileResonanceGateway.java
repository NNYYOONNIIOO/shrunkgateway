package nyonio.common.tile;

import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.fx.EntityFXFacingParticle;
import hellfirepvp.astralsorcery.common.auxiliary.link.ILinkableTile;
import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.structure.array.PatternBlockArray;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import nyonio.ShrunkGateway;
import nyonio.common.data.ResonanceGatewayDataManager;
import nyonio.common.network.PacketHandler;
import nyonio.common.network.PktTeleportEffect;
import nyonio.common.registry.RegistryStructures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TileResonanceGateway extends TileCelestialGateway implements ILinkableTile {

    private UUID owner;
    private UUID placedBy;
    private boolean resonanceGatewayRegistered = false;
    
    private BlockPos linkedPos = null;
    private int linkedDimension = Integer.MIN_VALUE;
    private long cooldownTime = 0;
    
    private static final long COOLDOWN_TICKS = 60;
    private static final int TELEPORT_COUNTDOWN = 100;
    private int teleportTicks = 0;
    private UUID teleportingPlayer = null;
    private Random rand = new Random();
    
    private UUID hubPairId = null;

    @Override
    public void update() {
        super.update();
        
        if(world.isRemote) {
            // 客户端：播放框架粒子效果
            if(hasMultiblock() && doesSeeSky()) {
                playFrameParticles();
            }
        } else {
            // 服务端：共振天辉星门不注册到公共星门列表
            GatewayCache publicCache = WorldCacheManager.getOrLoadData(world, WorldCacheManager.SaveKey.GATEWAY_DATA);
            publicCache.removePosition(world, pos);
            
            // 检查附近是否有玩家
            if(hubPairId != null && !isOnCooldown()) {
                EntityPlayerMP nearbyPlayer = (EntityPlayerMP) world.getClosestPlayer(
                    pos.getX() + 0.5, 
                    pos.getY() + 1.0, 
                    pos.getZ() + 0.5, 
                    3.0, 
                    false
                );
                
                if(nearbyPlayer != null) {
                    // 检查玩家是否在星门上方（y坐标差小于2）
                    double yDiff = Math.abs(nearbyPlayer.posY - (pos.getY() + 1));
                    if(yDiff < 2.0) {
                        // 开始或继续传送倒计时
                        if(teleportingPlayer == null || !teleportingPlayer.equals(nearbyPlayer.getUniqueID())) {
                            teleportingPlayer = nearbyPlayer.getUniqueID();
                            teleportTicks = 0;
                        }
                        
                        teleportTicks++;
                        
                        // 每10tick同步一次数据到客户端
                        if(teleportTicks % 10 == 0) {
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                        }
                        
                        if(teleportTicks >= TELEPORT_COUNTDOWN) {
                            // 执行传送
                            attemptTeleport(nearbyPlayer);
                            teleportTicks = 0;
                            teleportingPlayer = null;
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                        }
                    } else {
                        // 玩家离开，重置倒计时
                        teleportTicks = 0;
                        teleportingPlayer = null;
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                    }
                } else {
                    // 没有玩家，重置倒计时
                    if(teleportTicks > 0) {
                        teleportTicks = 0;
                        teleportingPlayer = null;
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                    }
                }
            } else {
                if(teleportTicks > 0) {
                    teleportTicks = 0;
                    teleportingPlayer = null;
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                }
            }
        }
    }
    
    private void attemptTeleport(EntityPlayerMP player) {
        nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] attemptTeleport called, hubPairId: {}", hubPairId);
        
        if (!hasMultiblock()) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.teleport.structure_incomplete");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return;
        }
        
        if (!doesSeeSky()) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.teleport.no_sky");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return;
        }
        
        if (hubPairId == null) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.teleport.not_bound");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return;
        }
        
        TileResonanceGateway targetGateway = findPairedGateway();
        
        nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] findPairedGateway result: {}", targetGateway);
        
        if (targetGateway == null) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.teleport.no_paired_gateway");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return;
        }
        
        if (!targetGateway.hasMultiblock()) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.teleport.target_structure_incomplete");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return;
        }
        
        if (!targetGateway.doesSeeSky()) {
            TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.teleport.target_no_sky");
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return;
        }
        
        if (targetGateway.isOnCooldown()) {
            return;
        }
        
        int targetDim = targetGateway.getWorld().provider.getDimension();
        BlockPos targetPos = targetGateway.getPos();
        
        nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] Teleporting player to dim: {}, pos: {}", targetDim, targetPos);
        
        MiscUtils.transferEntityTo(player, targetDim, targetPos);
        
        PacketHandler.INSTANCE.sendTo(new PktTeleportEffect(targetPos), player);
        
        triggerCooldown();
        targetGateway.triggerCooldown();
    }
    
    private TileResonanceGateway findPairedGateway() {
        if (hubPairId == null || world == null) return null;
        
        nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] Finding paired gateway for hubPairId: {}", hubPairId);
        
        ResonanceGatewayDataManager dataManager = ResonanceGatewayDataManager.get(world);
        ResonanceGatewayDataManager.ResonanceGatewayNode pairedNode = dataManager.getPairedGateway(pos, hubPairId);
        
        if (pairedNode == null) {
            nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] No paired gateway found in data manager");
            return null;
        }
        
        nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] Found paired gateway at pos: {}, dim: {}", pairedNode.pos, pairedNode.dimension);
        
        World targetWorld = world.getMinecraftServer().getWorld(pairedNode.dimension);
        if (targetWorld == null) {
            nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] Target world is null");
            return null;
        }
        
        TileEntity te = targetWorld.getTileEntity(pairedNode.pos);
        if (te instanceof TileResonanceGateway) {
            return (TileResonanceGateway) te;
        }
        
        nyonio.ShrunkGateway.LOGGER.info("[ResonanceGateway] Target tile entity is not TileResonanceGateway");
        return null;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        
        if (compound.hasUniqueId("owner")) {
            this.owner = compound.getUniqueId("owner");
        }
        if (compound.hasUniqueId("placedBy")) {
            this.placedBy = compound.getUniqueId("placedBy");
        }
        this.resonanceGatewayRegistered = compound.getBoolean("resonanceRegistered");
        
        if (compound.hasKey("linkedX")) {
            this.linkedPos = new BlockPos(
                compound.getInteger("linkedX"),
                compound.getInteger("linkedY"),
                compound.getInteger("linkedZ")
            );
            this.linkedDimension = compound.getInteger("linkedDim");
        }
        this.cooldownTime = compound.getLong("cooldownTime");
        
        this.teleportTicks = compound.getInteger("teleportTicks");
        if (compound.hasUniqueId("teleportingPlayer")) {
            this.teleportingPlayer = compound.getUniqueId("teleportingPlayer");
        }
        
        if (compound.hasUniqueId("hubPairId")) {
            this.hubPairId = compound.getUniqueId("hubPairId");
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        
        if (this.owner != null) {
            compound.setUniqueId("owner", this.owner);
        }
        if (this.placedBy != null) {
            compound.setUniqueId("placedBy", this.placedBy);
        }
        compound.setBoolean("resonanceRegistered", this.resonanceGatewayRegistered);
        
        if (this.linkedPos != null) {
            compound.setInteger("linkedX", this.linkedPos.getX());
            compound.setInteger("linkedY", this.linkedPos.getY());
            compound.setInteger("linkedZ", this.linkedPos.getZ());
            compound.setInteger("linkedDim", this.linkedDimension);
        }
        compound.setLong("cooldownTime", this.cooldownTime);
        
        compound.setInteger("teleportTicks", this.teleportTicks);
        if (this.teleportingPlayer != null) {
            compound.setUniqueId("teleportingPlayer", this.teleportingPlayer);
        }
        
        if (this.hubPairId != null) {
            compound.setUniqueId("hubPairId", this.hubPairId);
        }
    }
    
    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Nullable
    public UUID getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(UUID placedBy) {
        this.placedBy = placedBy;
    }

    @Nonnull
    @Override
    public PatternBlockArray getRequiredStructure() {
        return RegistryStructures.structureResonanceGateway;
    }

    public boolean isResonanceGatewayRegistered() {
        return resonanceGatewayRegistered;
    }
    
    @Override
    public World getLinkWorld() {
        return this.world;
    }
    
    @Override
    public BlockPos getLinkPos() {
        return this.pos;
    }
    
    @Nullable
    @Override
    public String getUnLocalizedDisplayName() {
        return "tile.shrunkgateway.resonance_gateway.name";
    }
    
    @Override
    public boolean doesAcceptLinks() {
        return true;
    }
    
    @Override
    public boolean onSelect(EntityPlayer player) {
        return true;
    }
    
    @Override
    public boolean tryLink(EntityPlayer player, BlockPos other) {
        if (world == null) return false;
        
        if (world.getTileEntity(other) instanceof TileResonanceGateway) {
            TileResonanceGateway otherGateway = (TileResonanceGateway) world.getTileEntity(other);
            
            if (otherGateway.getLinkWorld().provider.getDimension() != world.provider.getDimension()) {
                TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.link.cross_dimension");
                message.getStyle().setColor(TextFormatting.RED);
                player.sendMessage(message);
                return false;
            }
            
            this.linkedPos = other;
            this.linkedDimension = world.provider.getDimension();
            
            otherGateway.linkedPos = this.pos;
            otherGateway.linkedDimension = world.provider.getDimension();
            otherGateway.markDirty();
            
            this.markDirty();
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onLinkCreate(EntityPlayer player, BlockPos other) {
        TextComponentTranslation message = new TextComponentTranslation("message.shrunkgateway.link.success");
        message.getStyle().setColor(TextFormatting.GREEN);
        player.sendMessage(message);
    }
    
    @Override
    public boolean tryUnlink(EntityPlayer player, BlockPos other) {
        if (linkedPos != null && linkedPos.equals(other)) {
            if (world.getTileEntity(other) instanceof TileResonanceGateway) {
                TileResonanceGateway otherGateway = (TileResonanceGateway) world.getTileEntity(other);
                otherGateway.linkedPos = null;
                otherGateway.linkedDimension = Integer.MIN_VALUE;
                otherGateway.markDirty();
            }
            
            this.linkedPos = null;
            this.linkedDimension = Integer.MIN_VALUE;
            this.markDirty();
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<BlockPos> getLinkedPositions() {
        List<BlockPos> positions = new ArrayList<>();
        if (linkedPos != null) {
            positions.add(linkedPos);
        }
        return positions;
    }
    
    public boolean hasLinkedGateway() {
        return linkedPos != null && linkedDimension != Integer.MIN_VALUE;
    }
    
    @Nullable
    public BlockPos getLinkedPos() {
        return linkedPos;
    }
    
    public int getLinkedDimension() {
        return linkedDimension;
    }
    
    public boolean isOnCooldown() {
        return world != null && world.getTotalWorldTime() < cooldownTime;
    }
    
    public void triggerCooldown() {
        if (world != null) {
            this.cooldownTime = world.getTotalWorldTime() + COOLDOWN_TICKS;
            this.markDirty();
        }
    }
    
    public int getTeleportTicks() {
        return teleportTicks;
    }
    
    public static int getTeleportCountdown() {
        return TELEPORT_COUNTDOWN;
    }
    
    @SideOnly(Side.CLIENT)
    private void playFrameParticles() {
        for (int i = 0; i < 2; i++) {
            Vector3 offset = new Vector3(pos).add(-2, 0, -2);
            if(rand.nextBoolean()) {
                offset.add(5 * (rand.nextBoolean() ? 1 : 0), 0, rand.nextFloat() * 5);
            } else {
                offset.add(rand.nextFloat() * 5, 0, 5 * (rand.nextBoolean() ? 1 : 0));
            }
            EntityFXFacingParticle p = EffectHelper.genericFlareParticle(offset.getX(), offset.getY(), offset.getZ());
            p.gravity(0.0045).scale(0.25F + rand.nextFloat() * 0.15F).setMaxAge(40 + rand.nextInt(40));
            Color c = new Color(60, 0, 255);
            switch (rand.nextInt(4)) {
                case 0:
                    c = Color.WHITE;
                    break;
                case 1:
                    c = new Color(0x69B5FF);
                    break;
                case 2:
                    c = new Color(0x0078FF);
                    break;
                default:
                    break;
            }
            p.setColor(c);
        }
    }
    
    public UUID getHubPairId() {
        return hubPairId;
    }
    
    public void setHubPairId(UUID hubPairId) {
        if (world != null && !world.isRemote) {
            ResonanceGatewayDataManager dataManager = ResonanceGatewayDataManager.get(world);
            
            if (this.hubPairId != null) {
                dataManager.removeGateway(pos);
            }
            
            this.hubPairId = hubPairId;
            
            if (hubPairId != null) {
                dataManager.addGateway(pos, world.provider.getDimension(), hubPairId);
            }
            
            this.markDirty();
        } else {
            this.hubPairId = hubPairId;
            this.markDirty();
        }
    }
    
    @Override
    public void onChunkUnload() {
        if (world != null && !world.isRemote && hubPairId != null) {
            ResonanceGatewayDataManager dataManager = ResonanceGatewayDataManager.get(world);
            dataManager.addGateway(pos, world.provider.getDimension(), hubPairId);
        }
        super.onChunkUnload();
    }
    
    @Override
    public void invalidate() {
        if (world != null && !world.isRemote) {
            ResonanceGatewayDataManager dataManager = ResonanceGatewayDataManager.get(world);
            dataManager.removeGateway(pos);
        }
        super.invalidate();
    }
}
