package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientNuclearFlash;
import com.hbm.ntm.client.ShaderPackCompat;
import com.hbm.ntm.nuclear.MushroomCloudEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Batches the Torex cloudlets and the eye-searing flare behind them. */
public final class MushroomCloudRenderer extends EntityRenderer<MushroomCloudEntity> {
    private static final ResourceLocation SHADER_CLOUD_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/particle/torex_cloud.png");
    private static final ResourceLocation CLASSIC_CLOUD_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/particle/particle_base.png");
    // The flare texture has black baked in. True alpha keeps Iris from displaying the evidence.
    private static final ResourceLocation FLARE_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/particle/flash.png");
    private static final RenderType SHADER_CLOUD_TYPE = RenderType.entityTranslucent(SHADER_CLOUD_TEXTURE, false);
    private static final RenderType CLASSIC_CLOUD_TYPE = RenderType.entityTranslucent(CLASSIC_CLOUD_TEXTURE, false);
    private static final RenderType FLARE_TYPE = RenderType.entityTranslucentEmissive(FLARE_TEXTURE, false);

    private final List<MushroomCloudEntity.Cloudlet> sortedCloudlets = new ArrayList<>();

    public MushroomCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(MushroomCloudEntity cloud, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight) {
        Quaternionf camera = entityRenderDispatcher.cameraOrientation();
        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F).rotate(camera);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F).rotate(camera);
        Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F).rotate(camera);

        RenderType cloudType = ShaderPackCompat.shadersEnabled() ? SHADER_CLOUD_TYPE : CLASSIC_CLOUD_TYPE;
        renderCloudlets(cloud, partialTick, poses, buffers.getBuffer(cloudType), right, up, normal,
                packedLight);
        renderShockwave(cloud, partialTick, poses.last(), buffers.getBuffer(RenderType.lightning()));
        if (cloud.age() < 101) {
            renderFlash(cloud, partialTick, poses, buffers.getBuffer(FLARE_TYPE), right, up, normal);
        }
        if (cloud.age() < 10) ClientNuclearFlash.trigger();
        shakeCamera(cloud);
        super.render(cloud, yaw, partialTick, poses, buffers, LightTexture.FULL_BRIGHT);
    }

    private void renderCloudlets(MushroomCloudEntity cloud, float partialTick, PoseStack poses,
                                 VertexConsumer consumer, Vector3f right, Vector3f up, Vector3f normal,
                                 int packedLight) {
        sortedCloudlets.clear();
        sortedCloudlets.addAll(cloud.cloudlets());
        sortedCloudlets.sort(Comparator.comparingDouble((MushroomCloudEntity.Cloudlet cloudlet) ->
                entityRenderDispatcher.distanceToSqr(cloudlet.x(), cloudlet.y(), cloudlet.z())).reversed());

        for (MushroomCloudEntity.Cloudlet cloudlet : sortedCloudlets) {
            Vec3 position = cloudlet.interpolatedPosition(partialTick);
            float x = (float) (position.x - cloud.getX());
            float y = (float) (position.y - cloud.getY());
            float z = (float) (position.z - cloud.getZ());
            float scale = cloudlet.renderScale();
            float alpha = Mth.clamp(cloudlet.alpha(), 0.0F, 1.0F);
            if (alpha <= 0.0F || !Float.isFinite(scale)) continue;

            Vec3 color = cloudlet.interpolatedColor(partialTick);
            float brightness = switch (cloudlet.type()) {
                case CONDENSATION -> 0.9F;
                case SHOCK -> 0.62F * cloudlet.colorMod();
                default -> 0.75F * cloudlet.colorMod();
            };
            float red = Mth.clamp((float) color.x * brightness, 0.0F, 1.0F);
            float green = Mth.clamp((float) color.y * brightness, 0.0F, 1.0F);
            float blue = Mth.clamp((float) color.z * brightness, 0.0F, 1.0F);
            int light = cloudlet.type() != MushroomCloudEntity.TorexType.CONDENSATION
                    && cloudlet.type() != MushroomCloudEntity.TorexType.SHOCK
                    && cloud.age() < Math.min(180, cloud.maxAge() / 3)
                    ? LightTexture.FULL_BRIGHT : packedLight;
            emitBillboard(consumer, poses.last(), x, y, z, scale, right, up, normal,
                    cloudlet.renderRotation(partialTick), red, green, blue, alpha, light);
        }
    }

    private static void renderShockwave(MushroomCloudEntity cloud, float partialTick,
                                        PoseStack.Pose pose, VertexConsumer consumer) {
        float alpha = cloud.shockwaveAlpha(partialTick);
        if (alpha <= 0.0F) return;

        float radius = cloud.shockwaveRadius(partialTick);
        float width = 1.4F + radius * 0.018F;
        float y = (float) (cloud.shockwaveY() - cloud.getY()) + 0.18F;
        int segments = Mth.clamp(Mth.ceil(radius * 1.5F), 48, 144);
        float phase = cloud.getId() * 0.731F;
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            float r0 = radius + Mth.sin(a0 * 7.0F + phase) * width * 0.16F;
            float r1 = radius + Mth.sin(a1 * 7.0F + phase) * width * 0.16F;
            shockwaveBand(consumer, pose, a0, a1, r0 - width, r1 - width, r0, r1, y,
                    0.0F, alpha);
            shockwaveBand(consumer, pose, a0, a1, r0, r1, r0 + width, r1 + width, y,
                    alpha, 0.0F);
        }
    }

    private static void shockwaveBand(VertexConsumer consumer, PoseStack.Pose pose,
                                      float a0, float a1, float inner0, float inner1,
                                      float outer0, float outer1, float y,
                                      float innerAlpha, float outerAlpha) {
        shockwaveVertex(consumer, pose, Mth.cos(a0) * inner0, y, Mth.sin(a0) * inner0, innerAlpha);
        shockwaveVertex(consumer, pose, Mth.cos(a1) * inner1, y, Mth.sin(a1) * inner1, innerAlpha);
        shockwaveVertex(consumer, pose, Mth.cos(a1) * outer1, y, Mth.sin(a1) * outer1, outerAlpha);
        shockwaveVertex(consumer, pose, Mth.cos(a0) * outer0, y, Mth.sin(a0) * outer0, outerAlpha);
    }

    private static void shockwaveVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                        float x, float y, float z, float alpha) {
        consumer.addVertex(pose, x, y, z).setColor(1.0F, 0.72F, 0.42F, alpha);
    }

    private void renderFlash(MushroomCloudEntity cloud, float partialTick, PoseStack poses,
                             VertexConsumer consumer, Vector3f right, Vector3f up, Vector3f normal) {
        float interpolatedAge = Math.min(cloud.age() + partialTick, 100.0F);
        float alpha = Mth.clamp((100.0F - interpolatedAge) / 100.0F, 0.0F, 1.0F);
        alpha *= alpha;
        Random random = new Random(cloud.getId());
        float scale = (float) (25.0D * cloud.rollerSize());
        for (int i = 0; i < 3; i++) {
            float x = (float) (random.nextGaussian() * 0.5D * cloud.rollerSize());
            float y = (float) (cloud.coreHeight() + random.nextGaussian() * 0.5D * cloud.rollerSize());
            float z = (float) (random.nextGaussian() * 0.5D * cloud.rollerSize());
            emitBillboard(consumer, poses.last(), x, y, z, scale * (0.82F + i * 0.09F),
                    right, up, normal, i * 0.73F, 1.0F, 0.92F, 0.72F, alpha,
                    LightTexture.FULL_BRIGHT);
        }
    }

    private static void emitBillboard(VertexConsumer consumer, PoseStack.Pose pose,
                                      float centerX, float centerY, float centerZ, float scale,
                                      Vector3f right, Vector3f up, Vector3f normal,
                                      float rotation, float red, float green, float blue, float alpha,
                                      int packedLight) {
        float cos = Mth.cos(rotation);
        float sin = Mth.sin(rotation);
        float rx = (right.x() * cos - up.x() * sin) * scale;
        float ry = (right.y() * cos - up.y() * sin) * scale;
        float rz = (right.z() * cos - up.z() * sin) * scale;
        float ux = (right.x() * sin + up.x() * cos) * scale;
        float uy = (right.y() * sin + up.y() * cos) * scale;
        float uz = (right.z() * sin + up.z() * cos) * scale;

        vertex(consumer, pose, centerX - rx - ux, centerY - ry - uy, centerZ - rz - uz,
                1.0F, 1.0F, red, green, blue, alpha, normal, packedLight);
        vertex(consumer, pose, centerX - rx + ux, centerY - ry + uy, centerZ - rz + uz,
                1.0F, 0.0F, red, green, blue, alpha, normal, packedLight);
        vertex(consumer, pose, centerX + rx + ux, centerY + ry + uy, centerZ + rz + uz,
                0.0F, 0.0F, red, green, blue, alpha, normal, packedLight);
        vertex(consumer, pose, centerX + rx - ux, centerY + ry - uy, centerZ + rz - uz,
                0.0F, 1.0F, red, green, blue, alpha, normal, packedLight);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v,
                               float red, float green, float blue, float alpha, Vector3f normal,
                               int packedLight) {
        consumer.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normal.x(), normal.y(), normal.z());
    }

    private static void shakeCamera(MushroomCloudEntity cloud) {
        if (!cloud.heardSound() || cloud.shookCamera()) return;
        cloud.markCameraShaken();
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.hurtTime = 15;
            Minecraft.getInstance().player.hurtDuration = 15;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(MushroomCloudEntity entity) {
        return ShaderPackCompat.shadersEnabled() ? SHADER_CLOUD_TEXTURE : CLASSIC_CLOUD_TEXTURE;
    }
}
