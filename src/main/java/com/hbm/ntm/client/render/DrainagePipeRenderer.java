package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.DrainagePipeBlock;
import com.hbm.ntm.blockentity.DrainagePipeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class DrainagePipeRenderer implements BlockEntityRenderer<DrainagePipeBlockEntity> {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/machine_drain"));

    public DrainagePipeRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(DrainagePipeBlockEntity drain, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poses.pushPose();
        poses.translate(.5D, 0D, .5D);
        poses.mulPose(Axis.YP.rotationDegrees(rotation(
                drain.getBlockState().getValue(DrainagePipeBlock.FACING))));
        ThermalModelRenderer.render(MODEL, poses, buffers, packedLight, packedOverlay);
        poses.popPose();
    }

    private static float rotation(net.minecraft.core.Direction facing) {
        return switch (facing) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            case EAST -> 0F;
            default -> 0F;
        };
    }

    @Override
    public AABB getRenderBoundingBox(DrainagePipeBlockEntity drain) {
        return new AABB(drain.getBlockPos()).inflate(3D, 1D, 3D);
    }

    @Override public int getViewDistance() { return 256; }
}
