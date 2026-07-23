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
    private static final ResourceLocation RICHARD_MODEL = id("models/turrets/turret_richard.obj");
    private static final ResourceLocation HOWARD_MODEL = id("models/turrets/turret_howard.obj");
    private static final ResourceLocation FRITZ_MODEL = id("models/turrets/turret_fritz.obj");
    private static final ResourceLocation MAXWELL_MODEL = id("models/turrets/turret_microwave.obj");
    private static final ResourceLocation ARTY_MODEL = id("models/turrets/turret_arty.obj");
    private static final ResourceLocation HIMARS_MODEL = id("models/turrets/turret_himars.obj");
    private static final ResourceLocation SENTRY_MODEL = id("models/turrets/turret_sentry.obj");
    private static final ResourceLocation BASE = id("textures/models/turrets/base.png");
    private static final ResourceLocation CARRIAGE = id("textures/models/turrets/carriage.png");
    private static final ResourceLocation FRIENDLY_BASE = id("textures/models/turrets/base_friendly.png");
    private static final ResourceLocation FRIENDLY_CARRIAGE = id("textures/models/turrets/carriage_friendly.png");
    private static final ResourceLocation BODY = id("textures/models/turrets/chekhov.png");
    private static final ResourceLocation BARRELS = id("textures/models/turrets/chekhov_barrels.png");
    private static final ResourceLocation JEREMY = id("textures/models/turrets/jeremy.png");
    private static final ResourceLocation TAUON = id("textures/models/turrets/tauon.png");
    private static final ResourceLocation RICHARD = id("textures/models/turrets/richard.png");
    private static final ResourceLocation HOWARD = id("textures/models/turrets/howard.png");
    private static final ResourceLocation HOWARD_BARRELS = id("textures/models/turrets/howard_barrels.png");
    private static final ResourceLocation FRITZ = id("textures/models/turrets/fritz.png");
    private static final ResourceLocation MAXWELL = id("textures/models/turrets/maxwell.png");
    private static final ResourceLocation ARTY = id("textures/models/turrets/arty.png");
    private static final ResourceLocation HIMARS = id("textures/models/turrets/himars.png");
    private static final ResourceLocation SENTRY = id("textures/models/turrets/sentry.png");
    private static final ResourceLocation CIWS_CARRIAGE = id("textures/models/turrets/carriage_ciws.png");
    private static final ResourceLocation HIMARS_STANDARD =
            id("textures/models/projectiles/himars_standard.png");
    private static final ResourceLocation HIMARS_SINGLE =
            id("textures/models/projectiles/himars_single.png");
    private EnvsuitMesh chekhovMesh;
    private EnvsuitMesh jeremyMesh;
    private EnvsuitMesh tauonMesh;
    private EnvsuitMesh richardMesh;
    private EnvsuitMesh howardMesh;
    private EnvsuitMesh fritzMesh;
    private EnvsuitMesh maxwellMesh, artyMesh, himarsMesh, sentryMesh;

    public TurretFriendlyRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(TurretFriendlyBlockEntity turret, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        Vec3 offset = TurretFriendlyBlock.horizontalOffset(
                turret.getBlockState().getValue(TurretFriendlyBlock.FACING), turret.variant());
        poses.translate(offset.x, offset.y, offset.z);
        if (turret.variant() == TurretVariant.ARTY || turret.variant() == TurretVariant.HIMARS) {
            renderArtillery(turret, partialTick, poses, buffers, light, overlay);
            poses.popPose();
            return;
        }
        if (turret.variant() == TurretVariant.SENTRY) {
            renderSentry(turret, partialTick, poses, buffers, light, overlay);
            poses.popPose();
            return;
        }
        boolean friendly = turret.variant() == TurretVariant.FRIENDLY;
        render(chekhovMesh(), "Base", friendly ? FRIENDLY_BASE : BASE, poses, buffers, light, overlay);

        poses.mulPose(Axis.YP.rotationDegrees(-turret.yaw(partialTick) - 90F));
        if (turret.variant() == TurretVariant.HOWARD || turret.variant() == TurretVariant.MAXWELL) {
            render(howardMesh(), "Carriage",
                    turret.variant() == TurretVariant.MAXWELL ? CIWS_CARRIAGE : HOWARD,
                    poses, buffers, light, overlay);
        } else {
            render(chekhovMesh(), "Carriage", friendly ? FRIENDLY_CARRIAGE : CARRIAGE,
                    poses, buffers, light, overlay);
        }

        poses.pushPose();
        double pivot = turret.variant() == TurretVariant.HOWARD ? 2.25D : 1.5D;
        poses.translate(0D, pivot, 0D);
        poses.mulPose(Axis.ZP.rotationDegrees(-turret.pitch(partialTick)));
        poses.translate(0D, -pivot, 0D);
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
            case RICHARD -> {
                render(richardMesh(), "Launcher", RICHARD, poses, buffers, light, overlay);
                poses.translate(0D, 0.375D, 0.1875D);
                for (int i = 0; i < turret.loaded(); i++) {
                    render(richardMesh(), "MissileLoaded", RICHARD, poses, buffers, light, overlay);
                    if (i == 2 || i == 6 || i == 9 || i == 13) {
                        poses.translate(0D, -0.1875D, 0.46875D);
                    } else {
                        poses.translate(0D, 0D, -0.1875D);
                    }
                }
            }
            case HOWARD -> {
                render(howardMesh(), "Body", HOWARD, poses, buffers, light, overlay);
                poses.pushPose();
                poses.translate(0D, 2.5D, 0D);
                poses.mulPose(Axis.XN.rotationDegrees(turret.spin(partialTick)));
                poses.translate(0D, -2.5D, 0D);
                render(howardMesh(), "BarrelsTop", HOWARD_BARRELS, poses, buffers, light, overlay);
                poses.popPose();
                poses.pushPose();
                poses.translate(0D, 2D, 0D);
                poses.mulPose(Axis.XP.rotationDegrees(turret.spin(partialTick)));
                poses.translate(0D, -2D, 0D);
                render(howardMesh(), "BarrelsBottom", HOWARD_BARRELS, poses, buffers, light, overlay);
                poses.popPose();
            }
            case FRITZ -> render(fritzMesh(), "Gun", FRITZ, poses, buffers, light, overlay);
            case MAXWELL -> render(maxwellMesh(), "Microwave", MAXWELL, poses, buffers, light, overlay);
            case ARTY, HIMARS, SENTRY -> { }
        }
        poses.popPose();
        poses.popPose();
    }

    private void renderArtillery(TurretFriendlyBlockEntity turret, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        render(artyMesh(), "Base", ARTY, poses, buffers, light, overlay);
        poses.mulPose(Axis.YP.rotationDegrees(-turret.yaw(partialTick) - 180F));
        if (turret.variant() == TurretVariant.ARTY) {
            render(artyMesh(), "Carriage", ARTY, poses, buffers, light, overlay);
            poses.translate(0, 3, 0);
            poses.mulPose(Axis.XP.rotationDegrees(-turret.pitch(partialTick)));
            poses.translate(0, -3, 0);
            render(artyMesh(), "Cannon", ARTY, poses, buffers, light, overlay);
            render(artyMesh(), "Barrel", ARTY, poses, buffers, light, overlay);
            return;
        }
        render(himarsMesh(), "Carriage", HIMARS, poses, buffers, light, overlay);
        poses.translate(0, 2.25D, 2D);
        poses.mulPose(Axis.XP.rotationDegrees(-turret.pitch(partialTick)));
        poses.translate(0, -2.25D, -2D);
        render(himarsMesh(), "Launcher", HIMARS, poses, buffers, light, overlay);
        poses.translate(0, 0, turret.crane(partialTick) * -5D);
        render(himarsMesh(), "Crane", HIMARS, poses, buffers, light, overlay);
        if (turret.loadedType() < 0) return;
        boolean single = turret.loadedType() == 1 || turret.loadedType() == 5;
        ResourceLocation rocket = himarsTexture(turret.loadedType());
        render(himarsMesh(), single ? "TubeSingle" : "TubeStandard", rocket,
                poses, buffers, light, overlay);
        if (single && turret.loaded() > 0) {
            render(himarsMesh(), "CapSingle", rocket, poses, buffers, light, overlay);
        } else if (!single) {
            for (int i = 0; i < Math.min(turret.loaded(), 6); i++) {
                render(himarsMesh(), "CapStandard" + (6 - i), rocket, poses, buffers, light, overlay);
            }
        }
    }

    private void renderSentry(TurretFriendlyBlockEntity turret, float partialTick, PoseStack poses,
                              MultiBufferSource buffers, int light, int overlay) {
        render(sentryMesh(), "Base", SENTRY, poses, buffers, light, overlay);
        poses.mulPose(Axis.YP.rotationDegrees(-turret.yaw(partialTick)));
        render(sentryMesh(), "Pivot", SENTRY, poses, buffers, light, overlay);
        poses.translate(0, 1.25D, 0);
        poses.mulPose(Axis.XP.rotationDegrees(turret.pitch(partialTick)));
        poses.translate(0, -1.25D, 0);
        render(sentryMesh(), "Body", SENTRY, poses, buffers, light, overlay);
        render(sentryMesh(), "Drum", SENTRY, poses, buffers, light, overlay);
        poses.pushPose();
        poses.translate(0, 0, turret.sentryLeftRecoil(partialTick) * -0.5D);
        render(sentryMesh(), "BarrelL", SENTRY, poses, buffers, light, overlay);
        poses.popPose();
        poses.pushPose();
        poses.translate(0, 0, turret.sentryRightRecoil(partialTick) * -0.5D);
        render(sentryMesh(), "BarrelR", SENTRY, poses, buffers, light, overlay);
        poses.popPose();
    }

    private static ResourceLocation himarsTexture(int type) {
        String name = switch (type) {
            case 1 -> "himars_single";
            case 2 -> "himars_standard_he";
            case 3 -> "himars_standard_wp";
            case 4 -> "himars_standard_tb";
            case 5 -> "himars_single_tb";
            case 6 -> "himars_standard_mini_nuke";
            case 7 -> "himars_standard_lava";
            default -> "himars_standard";
        };
        return id("textures/models/projectiles/" + name + ".png");
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
    private EnvsuitMesh richardMesh() {
        if (richardMesh == null) richardMesh = load(RICHARD_MODEL,
                Set.of("Launcher", "MissileLoaded"), "Richard turret");
        return richardMesh;
    }
    private EnvsuitMesh howardMesh() {
        if (howardMesh == null) howardMesh = load(HOWARD_MODEL,
                Set.of("Carriage", "Body", "BarrelsTop", "BarrelsBottom"), "Howard turret");
        return howardMesh;
    }
    private EnvsuitMesh fritzMesh() {
        if (fritzMesh == null) fritzMesh = load(FRITZ_MODEL, Set.of("Gun"), "Fritz turret");
        return fritzMesh;
    }
    private EnvsuitMesh maxwellMesh() {
        if (maxwellMesh == null) maxwellMesh = load(MAXWELL_MODEL,
                Set.of("Microwave"), "Maxwell turret");
        return maxwellMesh;
    }
    private EnvsuitMesh artyMesh() {
        if (artyMesh == null) artyMesh = load(ARTY_MODEL,
                Set.of("Base", "Carriage", "Cannon", "Barrel"), "Artillery turret");
        return artyMesh;
    }
    private EnvsuitMesh himarsMesh() {
        if (himarsMesh == null) himarsMesh = load(HIMARS_MODEL,
                Set.of("Carriage", "Launcher", "Crane", "TubeStandard", "TubeSingle",
                        "CapSingle", "CapStandard1", "CapStandard2", "CapStandard3",
                        "CapStandard4", "CapStandard5", "CapStandard6"), "HIMARS turret");
        return himarsMesh;
    }
    private EnvsuitMesh sentryMesh() {
        if (sentryMesh == null) sentryMesh = load(SENTRY_MODEL,
                Set.of("Base", "Pivot", "Body", "Drum", "BarrelL", "BarrelR"), "Sentry turret");
        return sentryMesh;
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
