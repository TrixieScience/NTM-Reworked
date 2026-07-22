package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.entity.TorpedoEntity;
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

public final class TorpedoRenderer extends EntityRenderer<TorpedoEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "models/weapons/torpedo.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "textures/models/weapons/torpedo.png");
    private EnvsuitMesh mesh;

    public TorpedoRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.0F; }

    @Override
    public void render(TorpedoEntity entity, float yaw, float partial, PoseStack poses,
                       MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.mulPose(Axis.XP.rotationDegrees(Math.min(85.0F, (entity.tickCount + partial) * 3.0F)));
        mesh().render("Cylinder", poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, OverlayTexture.NO_OVERLAY, -1);
        poses.popPose();
        super.render(entity, yaw, partial, poses, buffers, light);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL,
                Set.of("Cylinder"), "Torpedo");
        return mesh;
    }

    @Override public ResourceLocation getTextureLocation(TorpedoEntity entity) { return TEXTURE; }
}
