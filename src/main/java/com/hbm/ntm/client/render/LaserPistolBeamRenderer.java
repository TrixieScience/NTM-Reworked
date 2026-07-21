package com.hbm.ntm.client.render;

import com.hbm.ntm.entity.LaserPistolBeamEntity;
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

/** Source four-layer, fullbright red or emerald Laser Pistol beam. */
public final class LaserPistolBeamRenderer extends EntityRenderer<LaserPistolBeamEntity> {
    private static final RenderType TYPE = RenderType.create(
            "hbm_laser_pistol_beam", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 2048,
            false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    public LaserPistolBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(LaserPistolBeamEntity beam, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight) {
        float age = Math.max(0.0F, Math.min(1.0F,
                1.0F - (beam.tickCount - 2.0F + partialTick) / LaserPistolBeamEntity.LIFETIME));
        float width = age * 0.5F + 0.5F;
        poses.pushPose();
        Vec3 direction = beam.beamDirection();
        poses.mulPose(new Quaternionf().rotationTo(0.0F, 0.0F, 1.0F,
                (float) direction.x, (float) direction.y, (float) direction.z));
        poses.scale(width, width, 1.0F);
        VertexConsumer consumer = buffers.getBuffer(TYPE);
        float red = (beam.emerald() ? 21.0F : 128.0F) / 255.0F * age;
        float green = (beam.emerald() ? 128.0F : 21.0F) / 255.0F * age;
        float blue = 21.0F / 255.0F * age;
        for (int layer = 1; layer <= 4; layer++) {
            float radius = 0.025F * layer / 4.0F;
            prism(consumer, poses.last(), radius, beam.beamLength(), red, green, blue);
        }
        poses.popPose();
        super.render(beam, yaw, partialTick, poses, buffers, packedLight);
    }

    private static void prism(VertexConsumer out, PoseStack.Pose pose, float radius, float length,
                              float red, float green, float blue) {
        quad(out, pose, radius, -radius, 0, radius, radius, 0,
                radius, radius, length, radius, -radius, length, red, green, blue);
        quad(out, pose, -radius, radius, 0, -radius, -radius, 0,
                -radius, -radius, length, -radius, radius, length, red, green, blue);
        quad(out, pose, -radius, -radius, 0, radius, -radius, 0,
                radius, -radius, length, -radius, -radius, length, red, green, blue);
        quad(out, pose, radius, radius, 0, -radius, radius, 0,
                -radius, radius, length, radius, radius, length, red, green, blue);
    }

    private static void quad(VertexConsumer out, PoseStack.Pose pose,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float red, float green, float blue) {
        vertex(out, pose, ax, ay, az, red, green, blue);
        vertex(out, pose, bx, by, bz, red, green, blue);
        vertex(out, pose, cx, cy, cz, red, green, blue);
        vertex(out, pose, dx, dy, dz, red, green, blue);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, float x, float y, float z,
                               float red, float green, float blue) {
        out.addVertex(pose, x, y, z).setColor(red, green, blue, 1.0F);
    }

    @Override public ResourceLocation getTextureLocation(LaserPistolBeamEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
