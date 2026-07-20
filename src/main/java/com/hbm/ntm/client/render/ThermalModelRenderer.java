package com.hbm.ntm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Blocks;

final class ThermalModelRenderer {
    private ThermalModelRenderer() {
    }

    static void render(ModelResourceLocation location, PoseStack poses, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        render(location, poses, buffers, packedLight, packedOverlay, 1F, 1F, 1F);
    }

    static void render(ModelResourceLocation location, PoseStack poses, MultiBufferSource buffers,
                       int packedLight, int packedOverlay, float red, float green, float blue) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(location);
        ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
        VertexConsumer consumer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        renderer.renderModel(poses.last(), consumer, Blocks.IRON_BLOCK.defaultBlockState(), model,
                red, green, blue, packedLight, packedOverlay);
    }
}
