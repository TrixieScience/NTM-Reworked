package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HighPowerCondenserBlock;
import com.hbm.ntm.blockentity.HighPowerCondenserBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** Two fans, opposite axles, one heroic quantity of low-pressure steam. */
public final class HighPowerCondenserRenderer implements BlockEntityRenderer<HighPowerCondenserBlockEntity> {
    public static final ModelResourceLocation BODY = model("condenser_powered_body");
    public static final ModelResourceLocation FAN_ONE = model("condenser_powered_fan_one");
    public static final ModelResourceLocation FAN_TWO = model("condenser_powered_fan_two");

    public HighPowerCondenserRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(HighPowerCondenserBlockEntity condenser, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poses.pushPose();
        poses.translate(0.5D, 0D, 0.5D);
        poses.mulPose(Axis.YP.rotationDegrees(facingRotation(
                condenser.getBlockState().getValue(HighPowerCondenserBlock.FACING))));
        renderParts(condenser.spin(partialTick), poses, buffers, packedLight, packedOverlay);
        poses.popPose();
    }

    public static void renderParts(float spin, PoseStack poses, MultiBufferSource buffers,
                                   int packedLight, int packedOverlay) {
        ThermalModelRenderer.render(BODY, poses, buffers, packedLight, packedOverlay);

        poses.pushPose();
        poses.translate(0D, 1.5D, 0D);
        poses.mulPose(Axis.XP.rotationDegrees(spin));
        poses.translate(0D, -1.5D, 0D);
        ThermalModelRenderer.render(FAN_ONE, poses, buffers, packedLight, packedOverlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0D, 1.5D, 0D);
        poses.mulPose(Axis.XN.rotationDegrees(spin));
        poses.translate(0D, -1.5D, 0D);
        ThermalModelRenderer.render(FAN_TWO, poses, buffers, packedLight, packedOverlay);
        poses.popPose();
    }

    private static float facingRotation(Direction direction) {
        return switch (direction) {
            case EAST -> 0F;
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        };
    }

    @Override public AABB getRenderBoundingBox(HighPowerCondenserBlockEntity condenser) {
        return condenser.renderBounds();
    }
    @Override public int getViewDistance() { return 256; }

    private static ModelResourceLocation model(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                HbmNtm.MOD_ID, "block/" + path));
    }
}
