package com.hbm.ntm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** RenderCombustionEngine#getRenderer with the original inventory transform. */
public final class CombustionEngineItemRenderer extends BlockEntityWithoutLevelRenderer {
    public CombustionEngineItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(0.5D, 0.5D, 0.5D);
        if (context == ItemDisplayContext.GUI) {
            poses.translate(0D, -1D, 0D);
            poses.scale(2.75F, 2.75F, 2.75F);
        }
        poses.mulPose(Axis.YP.rotationDegrees(90F));
        poses.translate(0D, 0D, 2.75D);
        CombustionEngineRenderer.renderItem(poses, buffers, light, overlay);
        poses.popPose();
    }
}
