package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidBarrelBlock;
import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import com.hbm.ntm.fluid.FluidTankProperties;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.EnumMap;
import java.util.Map;

public final class FluidBarrelRenderer implements BlockEntityRenderer<FluidBarrelBlockEntity> {
    public static final Map<FluidBarrelBlock.Type, ModelResourceLocation> CONNECTORS = connectors();

    public FluidBarrelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FluidBarrelBlockEntity barrel, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        FluidIdentifierItem.Selection selection = barrel.selection();
        if (selection == FluidIdentifierItem.Selection.NONE || barrel.getLevel() == null) return;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = barrel.getBlockPos().relative(direction);
            if (barrel.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    neighbor, direction.getOpposite()) == null) continue;
            poses.pushPose();
            poses.translate(.5D, 0D, .5D);
            poses.mulPose(Axis.YP.rotationDegrees(rotation(direction)));
            poses.translate(-.5D, 0D, -.5D);
            ThermalModelRenderer.render(CONNECTORS.get(
                            ((FluidBarrelBlock) barrel.getBlockState().getBlock()).type()),
                    poses, buffers, packedLight, packedOverlay);
            poses.popPose();
        }

        FluidTankProperties.Profile profile = FluidTankProperties.get(selection);
        poses.pushPose();
        poses.translate(.5D, .5D, .5D);
        for (int side = 0; side < 4; side++) {
            poses.pushPose();
            poses.mulPose(Axis.YP.rotationDegrees(side * 90F));
            poses.translate(.4D, .30D, -.24D);
            poses.scale(1F, .25F, .25F);
            HazardDiamondRenderer.render(profile, poses, buffers, packedLight);
            poses.popPose();
        }
        poses.popPose();
    }

    private static float rotation(Direction direction) {
        return switch (direction) {
            case EAST -> 0F;
            case SOUTH -> 270F;
            case WEST -> 180F;
            case NORTH -> 90F;
            default -> 0F;
        };
    }

    private static Map<FluidBarrelBlock.Type, ModelResourceLocation> connectors() {
        EnumMap<FluidBarrelBlock.Type, ModelResourceLocation> models =
                new EnumMap<>(FluidBarrelBlock.Type.class);
        for (FluidBarrelBlock.Type type : FluidBarrelBlock.Type.values()) {
            if (!type.storesFluid()) continue;
            models.put(type, ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                    HbmNtm.MOD_ID, "block/barrel_" + type.name().toLowerCase() + "_connector")));
        }
        return models;
    }
}
