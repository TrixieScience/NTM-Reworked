package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.entity.BoxcarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class BoxcarRenderer extends EntityRenderer<BoxcarEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "models/boxcar.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "textures/models/boxcar.png");
    private EnvsuitMesh mesh;

    public BoxcarRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.0F; }

    @Override
    public void render(BoxcarEntity entity, float yaw, float partial, PoseStack poses,
                       MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.translate(0.0D, 0.0D, -1.5D);
        poses.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F));
        mesh().render("Cube_Cube.001", poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, OverlayTexture.NO_OVERLAY, -1);
        poses.popPose();
        super.render(entity, yaw, partial, poses, buffers, light);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL,
                Set.of("Cube_Cube.001"), "Boxcar");
        return mesh;
    }

    @Override public ResourceLocation getTextureLocation(BoxcarEntity entity) { return TEXTURE; }
}
