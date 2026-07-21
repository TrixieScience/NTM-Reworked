package com.hbm.ntm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Pocket RadGen: parked rotor, green lamp, glass left at home. */
public final class RadGenItemRenderer extends BlockEntityWithoutLevelRenderer {
    public RadGenItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poses.pushPose();
        if (context == ItemDisplayContext.GUI) {
            // Old inventory camera. Mirror X/Z for the GUI handedness but keep Y upright:
            // scaling Y by -1 (with the -30 tilt) point-reflects the engine onto its head.
            // The three-tall model grows up from its base, so sit its origin low to centre it.
            poses.translate(0.5D, 0.35D, 0.0D);
            poses.mulPose(Axis.XP.rotationDegrees(30.0F));
            poses.mulPose(Axis.YP.rotationDegrees(45.0F));
            poses.scale(-1.0F, 1.0F, -1.0F);
            poses.translate(0.0D, -1.0D / 16.0D, 0.0D);
            float inventoryScale = 4.5F / 16.0F;
            poses.scale(inventoryScale, inventoryScale, inventoryScale);
        } else {
            boolean ground = context == ItemDisplayContext.GROUND;
            if (!ground) poses.translate(0.5D, 0.25D, 0.0D);
            float scale = 0.25F * (ground ? 1.5F : 1.0F);
            poses.scale(scale, scale, scale);
            if (context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }
        poses.scale(0.5F, 0.5F, 0.5F);
        poses.translate(0.5D, 0.0D, 0.0D);
        RadGenRenderer.renderStatic(poses, buffers, packedLight, packedOverlay);
        poses.popPose();
    }
}
