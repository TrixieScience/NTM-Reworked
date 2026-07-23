package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.ZirnoxDestroyedBlock;
import com.hbm.ntm.blockentity.ZirnoxDestroyedBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class ZirnoxDestroyedRenderer implements BlockEntityRenderer<ZirnoxDestroyedBlockEntity> {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/zirnox_destroyed"));
    public ZirnoxDestroyedRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(ZirnoxDestroyedBlockEntity wreck, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose(); poses.translate(.5, 0, .5);
        poses.mulPose(Axis.YP.rotationDegrees(270F - wreck.getBlockState().getValue(ZirnoxDestroyedBlock.FACING).toYRot()));
        ThermalModelRenderer.render(MODEL, poses, buffers, light, overlay); poses.popPose();
    }
    @Override public AABB getRenderBoundingBox(ZirnoxDestroyedBlockEntity wreck) { return wreck.renderBounds(); }
}
