package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Muzzle plume with additive blending and a black-background criminal record. */
final class SednaMuzzleFlash {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/models/weapons/lilmac_plume.png");
    private static final ResourceLocation LASER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/models/weapons/laser_flash.png");

    static final RenderType TYPE = RenderType.create(
            "hbm_sedna_muzzle_flash", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));
    private static final RenderType LASER_TYPE = RenderType.create(
            "hbm_sedna_laser_flash", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LASER_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    /** Exact four-quad geometry from ItemRenderWeaponBase.renderMuzzleFlash. */
    static void render(PoseStack poses, MultiBufferSource buffers, float progress, float maximumLength) {
        float fire = Math.max(0.0F, Math.min(progress, 1.0F));
        float width = 6.0F * fire;
        float length = maximumLength * fire;
        float inset = 2.0F;
        VertexConsumer consumer = buffers.getBuffer(TYPE);
        PoseStack.Pose pose = poses.last();

        vertex(consumer, pose, 0, -width, -inset, 1, 1);
        vertex(consumer, pose, 0, width, -inset, 0, 1);
        vertex(consumer, pose, 0.1F, width, length - inset, 0, 0);
        vertex(consumer, pose, 0.1F, -width, length - inset, 1, 0);

        vertex(consumer, pose, 0, width, inset, 0, 1);
        vertex(consumer, pose, 0, -width, inset, 1, 1);
        vertex(consumer, pose, 0.1F, -width, -length + inset, 1, 0);
        vertex(consumer, pose, 0.1F, width, -length + inset, 0, 0);

        vertex(consumer, pose, 0, -inset, width, 0, 1);
        vertex(consumer, pose, 0, -inset, -width, 1, 1);
        vertex(consumer, pose, 0.1F, length - inset, -width, 1, 0);
        vertex(consumer, pose, 0.1F, length - inset, width, 0, 0);

        vertex(consumer, pose, 0, inset, -width, 1, 1);
        vertex(consumer, pose, 0, inset, width, 0, 1);
        vertex(consumer, pose, 0.1F, -length + inset, width, 0, 0);
        vertex(consumer, pose, 0.1F, -length + inset, -width, 1, 0);
    }

    static void renderFireball(PoseStack poses, MultiBufferSource buffers, float progress) {
        float fire = Math.max(0.0F, Math.min(progress, 1.0F));
        float height = 5.0F * fire;
        float length = 10.0F * fire;
        float offset = fire;
        float side = 1.125F;
        VertexConsumer consumer = buffers.getBuffer(TYPE);
        PoseStack.Pose pose = poses.last();

        vertex(consumer, pose, height, -offset, 0.0F, 0.0F, 1.0F);
        vertex(consumer, pose, -height, -offset, 0.0F, 1.0F, 1.0F);
        vertex(consumer, pose, -height, -offset + length, side, 1.0F, 0.0F);
        vertex(consumer, pose, height, -offset + length, side, 0.0F, 0.0F);

        vertex(consumer, pose, height, -offset, 0.0F, 0.0F, 1.0F);
        vertex(consumer, pose, -height, -offset, 0.0F, 1.0F, 1.0F);
        vertex(consumer, pose, -height, -offset + length, -side, 1.0F, 0.0F);
        vertex(consumer, pose, height, -offset + length, -side, 0.0F, 0.0F);
    }

    static void renderLaser(PoseStack poses, MultiBufferSource buffers, float progress,
                            float scale, int color) {
        float size = 4.0F * Math.max(0.0F, Math.min(progress, 1.0F)) * scale;
        VertexConsumer consumer = buffers.getBuffer(LASER_TYPE);
        PoseStack.Pose pose = poses.last();
        laserVertex(consumer, pose, 0, -size, -size, 1, 1, color);
        laserVertex(consumer, pose, 0, size, -size, 0, 1, color);
        laserVertex(consumer, pose, 0, size, size, 0, 0, color);
        laserVertex(consumer, pose, 0, -size, size, 1, 0, color);
    }

    private static void laserVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                    float x, float y, float z, float u, float v, int color) {
        consumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private SednaMuzzleFlash() { }
}
