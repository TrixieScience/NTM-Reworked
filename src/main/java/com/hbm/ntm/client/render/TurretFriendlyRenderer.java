package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.TurretFriendlyBlock;
import com.hbm.ntm.blockentity.TurretFriendlyBlockEntity;
import com.hbm.ntm.blockentity.TurretVariant;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class TurretFriendlyRenderer implements BlockEntityRenderer<TurretFriendlyBlockEntity> {
    private static final ResourceLocation CHEKHOV_MODEL = id("models/turrets/turret_chekhov.obj");
    private static final ResourceLocation JEREMY_MODEL = id("models/turrets/turret_jeremy.obj");
    private static final ResourceLocation TAUON_MODEL = id("models/turrets/turret_tauon.obj");
    private static final ResourceLocation BASE = id("textures/models/turrets/base.png");
    private static final ResourceLocation CARRIAGE = id("textures/models/turrets/carriage.png");
    private static final ResourceLocation FRIENDLY_BASE = id("textures/models/turrets/base_friendly.png");
    private static final ResourceLocation FRIENDLY_CARRIAGE = id("textures/models/turrets/carriage_friendly.png");
    private static final ResourceLocation BODY = id("textures/models/turrets/chekhov.png");
    private static final ResourceLocation BARRELS = id("textures/models/turrets/chekhov_barrels.png");
    private static final ResourceLocation JEREMY = id("textures/models/turrets/jeremy.png");
    private static final ResourceLocation TAUON = id("textures/models/turrets/tauon.png");
    private EnvsuitMesh chekhovMesh;
    private EnvsuitMesh jeremyMesh;
    private EnvsuitMesh tauonMesh;

    public TurretFriendlyRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(TurretFriendlyBlockEntity turret, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        Vec3 offset = TurretFriendlyBlock.horizontalOffset(
                turret.getBlockState().getValue(TurretFriendlyBlock.FACING));
        poses.translate(offset.x, offset.y, offset.z);
        boolean friendly = turret.variant() == TurretVariant.FRIENDLY;
        render(chekhovMesh(), "Base", friendly ? FRIENDLY_BASE : BASE, poses, buffers, light, overlay);

        poses.mulPose(Axis.YP.rotationDegrees(-turret.yaw(partialTick) - 90F));
        render(chekhovMesh(), "Carriage", friendly ? FRIENDLY_CARRIAGE : CARRIAGE,
                poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0D, 1.5D, 0D);
        poses.mulPose(Axis.ZP.rotationDegrees(-turret.pitch(partialTick)));
        poses.translate(0D, -1.5D, 0D);
        switch (turret.variant()) {
            case CHEKHOV, FRIENDLY -> {
                render(chekhovMesh(), "Body", BODY, poses, buffers, light, overlay);
                poses.translate(0D, 1.5D, 0D);
                poses.mulPose(Axis.XN.rotationDegrees(turret.spin(partialTick)));
                poses.translate(0D, -1.5D, 0D);
                render(chekhovMesh(), "Barrels", BARRELS, poses, buffers, light, overlay);
            }
            case JEREMY -> render(jeremyMesh(), "Gun", JEREMY, poses, buffers, light, overlay);
            case TAUON -> {
                render(tauonMesh(), "Cannon", TAUON, poses, buffers, light, overlay);
                poses.translate(0D, 1.375D, 0D);
                poses.mulPose(Axis.XN.rotationDegrees(turret.spin(partialTick)));
                poses.translate(0D, -1.375D, 0D);
                render(tauonMesh(), "Rotor", TAUON, poses, buffers, light, overlay);
            }
        }
        poses.popPose();
        poses.popPose();
    }

    private void render(EnvsuitMesh mesh, String group, ResourceLocation texture, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        mesh.render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1F, light, overlay, -1);
    }

    private EnvsuitMesh chekhovMesh() {
        if (chekhovMesh == null) chekhovMesh = load(CHEKHOV_MODEL,
                Set.of("Base", "Carriage", "Body", "Barrels", "Connectors"), "Chekhov turret");
        return chekhovMesh;
    }
    private EnvsuitMesh jeremyMesh() {
        if (jeremyMesh == null) jeremyMesh = load(JEREMY_MODEL, Set.of("Gun"), "Jeremy turret");
        return jeremyMesh;
    }
    private EnvsuitMesh tauonMesh() {
        if (tauonMesh == null) tauonMesh = load(TAUON_MODEL, Set.of("Cannon", "Rotor"), "Tauon turret");
        return tauonMesh;
    }
    private static EnvsuitMesh load(ResourceLocation model, Set<String> groups, String name) {
        return EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), model, groups, name);
    }

    @Override public AABB getRenderBoundingBox(TurretFriendlyBlockEntity turret) {
        BlockPos pos = turret.getBlockPos();
        return new AABB(pos.getX() - 3, pos.getY(), pos.getZ() - 3,
                pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5);
    }

    @Override public int getViewDistance() { return 256; }
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
