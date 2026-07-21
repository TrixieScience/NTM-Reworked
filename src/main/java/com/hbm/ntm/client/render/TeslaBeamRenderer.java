package com.hbm.ntm.client.render;

import com.hbm.ntm.entity.TeslaBeamEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

/** Three twitchy fullbright bolts, copied from the old BeamPronter arrangement. */
public final class TeslaBeamRenderer extends EntityRenderer<TeslaBeamEntity> {
    private static final RenderType TYPE = RenderType.create(
            "hbm_tesla_lightning", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192,
            false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    public TeslaBeamRenderer(EntityRendererProvider.Context context) {
        super(context); shadowRadius = 0.0F;
    }

    @Override public void render(TeslaBeamEntity beam, float yaw, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int packedLight) {
        float age = Math.max(0.0F, Math.min(1.0F,
                1.0F - (beam.tickCount - 2.0F + partialTick) / (beam.subBeam() ? 3.0F : 5.0F)));
        float width = beam.subBeam() ? age * 0.5F + 0.15F : age * 0.5F + 0.5F;
        poses.pushPose();
        Vec3 direction = beam.beamDirection();
        poses.mulPose(new Quaternionf().rotationTo(0.0F, 0.0F, 1.0F,
                (float) direction.x, (float) direction.y, (float) direction.z));
        poses.scale(width, width, 1.0F);
        VertexConsumer consumer = buffers.getBuffer(TYPE);
        int tick = beam.tickCount;
        bolt(consumer, poses.last(), beam.beamLength(), tick / 3L, 0.075F, 4, 0.25F,
                0.125F * age, 0.125F * age, 0.25F * age);
        bolt(consumer, poses.last(), beam.beamLength(), tick, 0.525F, 2, 0.0625F,
                0.25F, 0.25F, 0.5F);
        bolt(consumer, poses.last(), beam.beamLength(), tick / 2L, 0.525F, 2, 0.0625F,
                0.25F, 0.25F, 0.5F);
        poses.popPose();
        super.render(beam, yaw, partialTick, poses, buffers, packedLight);
    }

    private static void bolt(VertexConsumer out, PoseStack.Pose pose, float length, long seed,
                             float wander, int layers, float thickness, float red, float green, float blue) {
        Random random = new Random(seed);
        int segments = Math.max(1, (int) (length / 2.0F) + 1);
        Vector3f previous = new Vector3f();
        for (int i = 1; i <= segments; i++) {
            float z = length * i / segments;
            float taper = i == segments ? 0.0F : 1.0F;
            double angle = random.nextDouble() * Math.PI * 2.0D;
            Vector3f next = new Vector3f((float) Math.cos(angle) * wander * taper,
                    (float) Math.sin(angle) * wander * taper, z);
            for (int layer = 0; layer < layers; layer++) {
                float radius = thickness * (1.0F - layer / (float) layers);
                prism(out, pose, previous, next, radius, red, green, blue);
            }
            previous = next;
        }
    }

    private static void prism(VertexConsumer out, PoseStack.Pose pose, Vector3f from, Vector3f to,
                              float radius, float red, float green, float blue) {
        Vector3f direction = new Vector3f(to).sub(from).normalize();
        Vector3f side = Math.abs(direction.y) < 0.9F
                ? new Vector3f(direction).cross(0.0F, 1.0F, 0.0F).normalize().mul(radius)
                : new Vector3f(direction).cross(1.0F, 0.0F, 0.0F).normalize().mul(radius);
        Vector3f up = new Vector3f(direction).cross(side).normalize().mul(radius);
        Vector3f[] a = {new Vector3f(from).add(side), new Vector3f(from).add(up),
                new Vector3f(from).sub(side), new Vector3f(from).sub(up)};
        Vector3f[] b = {new Vector3f(to).add(side), new Vector3f(to).add(up),
                new Vector3f(to).sub(side), new Vector3f(to).sub(up)};
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            vertex(out, pose, a[i], red, green, blue); vertex(out, pose, a[j], red, green, blue);
            vertex(out, pose, b[j], red, green, blue); vertex(out, pose, b[i], red, green, blue);
        }
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, Vector3f point,
                               float red, float green, float blue) {
        out.addVertex(pose, point.x, point.y, point.z).setColor(red, green, blue, 1.0F);
    }

    @Override public ResourceLocation getTextureLocation(TeslaBeamEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
