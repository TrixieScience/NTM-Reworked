package com.hbm.ntm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** RenderTurbineGas#getRenderer, including its wonderfully unreasonable GUI zoom. */
public final class GasTurbineItemRenderer extends BlockEntityWithoutLevelRenderer {
    public GasTurbineItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poses.pushPose();
        if (context == ItemDisplayContext.GUI) {
            poses.translate(0.5D, 0.5D, 0.5D);
            poses.translate(0D, -1D, 1.5D);
            poses.scale(2.5F, 2.5F, 2.5F);
        }
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.mulPose(Axis.YP.rotationDegrees(90F));
        GasTurbineRenderer.renderModel(poses, buffers, packedLight, packedOverlay);
        poses.popPose();
    }
}
