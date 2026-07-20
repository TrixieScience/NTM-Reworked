package com.hbm.ntm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** RenderCondenser#getRenderer, still hauling the entire power station in one hand. */
public final class HighPowerCondenserItemRenderer extends BlockEntityWithoutLevelRenderer {
    public HighPowerCondenserItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poses.pushPose();
        poses.translate(0.5D, 0.5D, 0.5D);
        if (context == ItemDisplayContext.GUI) {
            poses.translate(-1D, -1D, 0D);
            poses.scale(2.75F, 2.75F, 2.75F);
        } else {
            poses.mulPose(Axis.YP.rotationDegrees(90F));
        }
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.translate(0.5D, 0D, 0D);
        HighPowerCondenserRenderer.renderParts(0F, poses, buffers, packedLight, packedOverlay);
        poses.popPose();
    }
}
