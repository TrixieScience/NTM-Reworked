package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CombustionEngineBlock;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.client.sound.CombustionEngineSoundInstance;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.WeakHashMap;

public final class CombustionEngineRenderer implements BlockEntityRenderer<CombustionEngineBlockEntity> {
    public static final ModelResourceLocation ENGINE = model("combustion_engine_engine");
    public static final ModelResourceLocation CANISTER = model("combustion_engine_canister");
    public static final ModelResourceLocation HATCH = model("combustion_engine_hatch");
    public static final ModelResourceLocation ITEM = model("combustion_engine_item");
    private final Map<CombustionEngineBlockEntity, CombustionEngineSoundInstance> sounds = new WeakHashMap<>();

    public CombustionEngineRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(CombustionEngineBlockEntity engine, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        sounds.entrySet().removeIf(entry -> entry.getValue().isStopped());
        if (engine.active() && !sounds.containsKey(engine)) {
            CombustionEngineSoundInstance sound = new CombustionEngineSoundInstance(engine);
            sounds.put(engine, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        }
        poses.pushPose();
        poses.translate(0.5D, 0D, 0.5D);
        poses.mulPose(Axis.YP.rotationDegrees(facingRotation(
                engine.getBlockState().getValue(CombustionEngineBlock.FACING))));
        poses.translate(-0.5D, 0D, 3D);
        ThermalModelRenderer.render(ENGINE, poses, buffers, light, overlay);
        int color = canisterColor(engine.selectedFluid());
        ThermalModelRenderer.render(CANISTER, poses, buffers, light, overlay,
                ((color >> 16) & 255) / 256F, ((color >> 8) & 255) / 256F, (color & 255) / 256F);
        poses.translate(1D, 0D, -2.6875D);
        poses.mulPose(Axis.YP.rotationDegrees(-engine.doorAngle(partialTick)));
        poses.translate(-1D, 0D, 2.6875D);
        ThermalModelRenderer.render(HATCH, poses, buffers, light, overlay);
        poses.popPose();
    }

    public static void renderItem(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        ThermalModelRenderer.render(ITEM, poses, buffers, light, overlay);
    }

    private static int canisterColor(FluidIdentifierItem.Selection selection) {
        return switch (selection) {
            case HEAVYOIL -> 0x513F39;
            case HEATINGOIL -> 0x694235;
            case NAPHTHA -> 0x5F6D44;
            case LIGHTOIL -> 0xB46B52;
            case DIESEL -> 0xFF2C2C;
            case KEROSENE -> 0xFF377D;
            default -> selection.color();
        };
    }

    private static ModelResourceLocation model(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/" + path));
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

    @Override public AABB getRenderBoundingBox(CombustionEngineBlockEntity engine) {
        BlockPos core = engine.getBlockPos();
        Direction facing = engine.getBlockState().getValue(CombustionEngineBlock.FACING);
        AABB bounds = new AABB(core);
        for (BlockPos part : CombustionEngineBlock.partPositions(core, facing)) bounds = bounds.minmax(new AABB(part));
        return bounds.inflate(0.25D);
    }
    @Override public int getViewDistance() { return 256; }
}
