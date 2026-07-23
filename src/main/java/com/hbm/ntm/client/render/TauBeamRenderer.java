package com.hbm.ntm.client.render;

import com.hbm.ntm.entity.TauBeamEntity;
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

public final class TauBeamRenderer extends EntityRenderer<TauBeamEntity> {
    private static final RenderType TYPE = RenderType.create(
            "hbm_tau_beam", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 4096,
            false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    public TauBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(TauBeamEntity beam, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight) {
        float age = Math.max(0.0F, Math.min(1.0F,
                1.0F - (beam.tickCount - 2.0F + partialTick) / beam.lifetime()));
        if (age <= 0.0F || beam.beamLength() <= 0.0F) return;

        poses.pushPose();
        Vec3 direction = beam.beamDirection();
        poses.mulPose(new Quaternionf().rotationTo(0.0F, 0.0F, 1.0F,
                (float) direction.x, (float) direction.y, (float) direction.z));
        float width = age * 0.5F + 0.5F;
        poses.scale(width, width, 1.0F);

        VertexConsumer out = buffers.getBuffer(TYPE);
        float red = (beam.charged() ? 0x60 : 0x30) / 255.0F * age;
        float green = (beam.charged() ? 0x50 : 0x25) / 255.0F * age;
        float blue = (beam.charged() ? 0x30 : 0x10) / 255.0F * age;
        jagged(out, poses.last(), beam.beamLength(), (beam.tickCount + beam.getId()) / 2L,
                0.30F, 0.0625F, red, green, blue);

        float tipRed = 1.0F;
        float tipGreen = (beam.charged() ? 0xF0 : 0xBF) / 255.0F;
        float tipBlue = beam.charged() ? 0xA0 / 255.0F : 0.0F;
        tip(out, poses.last(), beam.beamLength(), age * 0.28F,
                tipRed, tipGreen, tipBlue);
        poses.popPose();
        super.render(beam, yaw, partialTick, poses, buffers, packedLight);
    }

    private static void jagged(VertexConsumer out, PoseStack.Pose pose, float length, long seed,
                               float wander, float radius, float red, float green, float blue) {
        Random random = new Random(seed);
        int segments = Math.max(1, (int) (length / 2.0F) + 1);
        Vector3f previous = new Vector3f();
        for (int i = 1; i <= segments; i++) {
            float z = length * i / segments;
            float taper = i == segments ? 0.0F : 1.0F;
            double angle = random.nextDouble() * Math.PI * 2.0D;
            Vector3f next = new Vector3f((float) Math.cos(angle) * wander * taper,
                    (float) Math.sin(angle) * wander * taper, z);
            prism(out, pose, previous, next, radius, red, green, blue);
            prism(out, pose, previous, next, radius * 0.5F,
                    Math.min(1.0F, red * 1.8F), Math.min(1.0F, green * 1.8F),
                    Math.min(1.0F, blue * 1.8F));
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
            vertex(out, pose, a[i], red, green, blue);
            vertex(out, pose, a[j], red, green, blue);
            vertex(out, pose, b[j], red, green, blue);
            vertex(out, pose, b[i], red, green, blue);
        }
    }

    private static void tip(VertexConsumer out, PoseStack.Pose pose, float z, float radius,
                            float red, float green, float blue) {
        quad(out, pose, -radius, 0, z, 0, -radius, z, radius, 0, z, 0, radius, z,
                red, green, blue);
        quad(out, pose, -radius, 0, z, 0, 0, z - radius, radius, 0, z, 0, 0, z + radius,
                red, green, blue);
        quad(out, pose, 0, -radius, z, 0, 0, z - radius, 0, radius, z, 0, 0, z + radius,
                red, green, blue);
    }

    private static void quad(VertexConsumer out, PoseStack.Pose pose,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float red, float green, float blue) {
        vertex(out, pose, new Vector3f(ax, ay, az), red, green, blue);
        vertex(out, pose, new Vector3f(bx, by, bz), red, green, blue);
        vertex(out, pose, new Vector3f(cx, cy, cz), red, green, blue);
        vertex(out, pose, new Vector3f(dx, dy, dz), red, green, blue);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, Vector3f point,
                               float red, float green, float blue) {
        out.addVertex(pose, point.x, point.y, point.z).setColor(red, green, blue, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(TauBeamEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
