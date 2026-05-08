package nyonio.client.event;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.effect.EffectHandler;
import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.fx.EntityFXFacingParticle;
import hellfirepvp.astralsorcery.client.sky.RenderAstralSkybox;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderConstellation;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.TextureHelper;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import nyonio.common.tile.TileResonanceGateway;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class ClientResonanceGatewayHandler {

    private static final Random effectRand = new Random();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null || mc.world == null) return;
        
        EntityPlayer player = mc.player;
        World world = player.world;
        
        BlockPos playerPos = player.getPosition();
        
        for(int x = -5; x <= 5; x++) {
            for(int y = -5; y <= 5; y++) {
                for(int z = -5; z <= 5; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);
                    TileResonanceGateway gateway = MiscUtils.getTileAt(world, checkPos, TileResonanceGateway.class, true);
                    
                    if(gateway != null && gateway.hasMultiblock() && gateway.doesSeeSky()) {
                        renderStarrySky(gateway, event.getPartialTicks());
                        
                        int teleportTicks = gateway.getTeleportTicks();
                        if(teleportTicks > 0) {
                            renderTeleportEffect(gateway, event.getPartialTicks(), teleportTicks, player);
                        }
                    }
                }
            }
        }
    }

    private void renderStarrySky(TileResonanceGateway gateway, float pticks) {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null) return;

        EntityPlayer player = mc.player;
        Vector3 origin = new Vector3(gateway.getPos()).add(0.5, 1.62, 0.5);
        double radius = 5.5;
        
        double dst = origin.distance(Vector3.atEntityCorner(player).addY(1.5));
        if(dst > 3) return;
        
        float alpha = 1F - ((float) (dst / 2D));
        alpha = MathHelper.clamp(alpha, 0F, 1F);

        long seed = 0xA781B4F01C771923L;
        seed |= ((long) gateway.getPos().getX()) << 48;
        seed |= ((long) gateway.getPos().getY()) << 24;
        seed |= ((long) gateway.getPos().getZ());
        Random rand = new Random(seed);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        Blending.DEFAULT.applyStateManager();
        GlStateManager.disableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);
        RenderAstralSkybox.TEX_STAR_1.bind();

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        
        for (int i = 0; i < 300; i++) {
            Vector3 dir = Vector3.random(rand).normalize().multiply(radius);
            float a = RenderConstellation.conCFlicker(ClientScheduler.getClientTick(), pticks, rand.nextInt(7) + 6);
            a *= alpha;
            RenderingUtils.renderFacingFullQuadVB(vb,
                    origin.getX() + dir.getX(),
                    origin.getY() + dir.getY(),
                    origin.getZ() + dir.getZ(),
                    pticks, 0.07F, 0, 1F, 1F, 1F, a);
        }
        
        RenderingUtils.sortVertexData(vb);
        tes.draw();
        TextureHelper.refreshTextureBindState();
        GlStateManager.enableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
    }
    
    private void renderTeleportEffect(TileResonanceGateway gateway, float pticks, int ticks, EntityPlayer player) {
        // 以玩家为中心，向下移动一格
        Vector3 center = new Vector3(player.posX, player.posY - 1.0, player.posZ);
        
        float progress = (float) ticks / TileResonanceGateway.getTeleportCountdown();
        
        if(ticks > 20) {
            int particleCount = (int) (progress * 50);
            
            for(int i = 0; i < particleCount; i++) {
                double angle = (EffectHandler.STATIC_EFFECT_RAND.nextDouble() * 2 * Math.PI);
                double radius = 0.5 + EffectHandler.STATIC_EFFECT_RAND.nextDouble() * 1.0;
                double height = EffectHandler.STATIC_EFFECT_RAND.nextDouble() * 2.5;
                
                Vector3 particlePos = center.clone().add(
                    Math.cos(angle) * radius,
                    height,
                    Math.sin(angle) * radius
                );
                
                EntityFXFacingParticle p = EffectHelper.genericGatewayFlareParticle(
                    particlePos.getX(),
                    particlePos.getY(),
                    particlePos.getZ()
                );
                
                p.gravity(0.02).scale(0.15F).motion(0, 0.08 + EffectHandler.STATIC_EFFECT_RAND.nextDouble() * 0.05, 0);
                
                switch(EffectHandler.STATIC_EFFECT_RAND.nextInt(4)) {
                    case 0:
                        p.setColor(Color.WHITE);
                        break;
                    case 1:
                        p.setColor(new Color(0x69B5FF));
                        break;
                    case 2:
                        p.setColor(new Color(0x0078FF));
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    public static void spawnTeleportEffect(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null || mc.world == null) return;
        
        // 在目标位置生成传送特效
        Vector3 center = new Vector3(pos).add(0.5, 0.0, 0.5);
        
        for(int i = 0; i < 50; i++) {
            double angle = (effectRand.nextDouble() * 2 * Math.PI);
            double radius = 0.5 + effectRand.nextDouble() * 1.0;
            double height = effectRand.nextDouble() * 2.5;
            
            Vector3 particlePos = center.clone().add(
                Math.cos(angle) * radius,
                height,
                Math.sin(angle) * radius
            );
            
            EntityFXFacingParticle p = EffectHelper.genericGatewayFlareParticle(
                particlePos.getX(),
                particlePos.getY(),
                particlePos.getZ()
            );
            
            p.gravity(0.02).scale(0.15F).motion(0, 0.08 + effectRand.nextDouble() * 0.05, 0);
            
            switch(effectRand.nextInt(4)) {
                case 0:
                    p.setColor(Color.WHITE);
                    break;
                case 1:
                    p.setColor(new Color(0x69B5FF));
                    break;
                case 2:
                    p.setColor(new Color(0x0078FF));
                    break;
                default:
                    break;
            }
        }
    }
}
