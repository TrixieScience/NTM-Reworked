package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.ZirnoxBlock;
import com.hbm.ntm.blockentity.ZirnoxBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class ZirnoxRenderer implements BlockEntityRenderer<ZirnoxBlockEntity> {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/zirnox"));

    public ZirnoxRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(ZirnoxBlockEntity reactor, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(.5D, 0D, .5D);
        poses.mulPose(Axis.YP.rotationDegrees(270F - reactor.getBlockState().getValue(ZirnoxBlock.FACING).toYRot()));
        ThermalModelRenderer.render(MODEL, poses, buffers, light, overlay);
        poses.popPose();
    }

    @Override public AABB getRenderBoundingBox(ZirnoxBlockEntity reactor) { return reactor.renderBounds(); }
    @Override public int getViewDistance() { return 256; }
}
