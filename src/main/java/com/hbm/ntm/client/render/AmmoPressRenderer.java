package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.AmmoPressBlock;
import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AmmoPressRenderer implements BlockEntityRenderer<AmmoPressBlockEntity> {
    public static final Map<String, ModelResourceLocation> MODELS = createModels();

    public AmmoPressRenderer(BlockEntityRendererProvider.Context context) { }

    private static Map<String, ModelResourceLocation> createModels() {
        Map<String, ModelResourceLocation> models = new LinkedHashMap<>();
        for (String part : new String[]{"frame", "press", "shells", "bullets"}) {
            models.put(part, ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                    HbmNtm.MOD_ID, "block/ammo_press_" + part)));
        }
        return Map.copyOf(models);
    }

    @Override public void render(AmmoPressBlockEntity press, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5D, 0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(rotation(press.getBlockState().getValue(AmmoPressBlock.FACING))));
        renderPart("frame", pose, buffers, light, overlay);
        int elapsed = 120 - press.animationTicks();
        float lift = elapsed < 40 ? elapsed / 40F : elapsed < 80 ? 1F : Math.max(0F, 1F - (elapsed - 80) / 40F);
        float ram = elapsed < 40 ? 0F : elapsed < 60 ? (elapsed - 40) / 20F : elapsed < 80 ? 1F - (elapsed - 60) / 20F : 0F;
        pose.pushPose();
        pose.translate(0D, -ram * 0.25D, 0D);
        renderPart("press", pose, buffers, light, overlay);
        pose.popPose();
        pose.pushPose();
        pose.translate(0D, lift * 0.5D - 0.5D, 0D);
        renderPart("shells", pose, buffers, light, overlay);
        if (elapsed >= 60 && press.animationTicks() > 0) renderPart("bullets", pose, buffers, light, overlay);
        pose.popPose();
        pose.popPose();
    }

    private static void renderPart(String part, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(MODELS.get(part));
        ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
        VertexConsumer consumer = buffers.getBuffer(Sheets.solidBlockSheet());
        renderer.renderModel(pose.last(), consumer, Blocks.IRON_BLOCK.defaultBlockState(), model,
                1F, 1F, 1F, light, overlay);
    }

    private static float rotation(Direction facing) {
        return switch (facing) { case EAST -> 90F; case SOUTH -> 180F; case WEST -> 270F; default -> 0F; };
    }

    @Override public AABB getRenderBoundingBox(AmmoPressBlockEntity press) {
        BlockPos pos = press.getBlockPos();
        Direction facing = press.getBlockState().getValue(AmmoPressBlock.FACING);
        BlockPos first = pos.relative(facing.getOpposite());
        BlockPos last = pos.relative(facing);
        return new AABB(Math.min(first.getX(), last.getX()), pos.getY(), Math.min(first.getZ(), last.getZ()),
                Math.max(first.getX(), last.getX()) + 1, pos.getY() + 2, Math.max(first.getZ(), last.getZ()) + 1);
    }

    @Override public int getViewDistance() { return 256; }
}
