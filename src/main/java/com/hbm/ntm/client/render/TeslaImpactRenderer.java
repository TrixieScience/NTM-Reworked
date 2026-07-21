package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.TeslaImpactEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The old three crossed blue plasmablast sheets. */
public final class TeslaImpactRenderer extends EntityRenderer<TeslaImpactEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/particle/shockwave.png");
    private static final RenderType TYPE = RenderType.create(
            "hbm_tesla_plasmablast", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
            false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    public TeslaImpactRenderer(EntityRendererProvider.Context context) {
        super(context); shadowRadius = 0.0F;
    }

    @Override public void render(TeslaImpactEntity impact, float yaw, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int packedLight) {
        float age = Math.min(1.0F, (impact.tickCount + partialTick) / TeslaImpactEntity.LIFETIME);
        float scale = (float) ((1.0D - Math.exp(-(impact.tickCount + partialTick) * 0.125D)) * 2.0D);
        VertexConsumer out = buffers.getBuffer(TYPE);
        for (float pitch : new float[]{-60.0F, 0.0F, 60.0F}) {
            poses.pushPose();
            poses.mulPose(Axis.YP.rotationDegrees(impact.getYRot()));
            poses.mulPose(Axis.XP.rotationDegrees(pitch));
            poses.scale(scale, scale, scale);
            PoseStack.Pose pose = poses.last();
            vertex(out, pose, -1, -1, 0, 0, 1, 1.0F - age);
            vertex(out, pose, 1, -1, 0, 1, 1, 1.0F - age);
            vertex(out, pose, 1, 1, 0, 1, 0, 1.0F - age);
            vertex(out, pose, -1, 1, 0, 0, 0, 1.0F - age);
            poses.popPose();
        }
        super.render(impact, yaw, partialTick, poses, buffers, packedLight);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, float alpha) {
        out.addVertex(pose, x, y, z).setColor(0.5F, 0.5F, 1.0F, alpha)
                .setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0, 0, 1);
    }

    @Override public ResourceLocation getTextureLocation(TeslaImpactEntity entity) { return TEXTURE; }
}
