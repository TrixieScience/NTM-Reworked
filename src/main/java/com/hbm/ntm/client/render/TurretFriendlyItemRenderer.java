package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.blockentity.TurretVariant;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class TurretFriendlyItemRenderer extends BlockEntityWithoutLevelRenderer {
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
    private EnvsuitMesh chekhovMesh, jeremyMesh, tauonMesh, richardMesh, howardMesh, fritzMesh;
    private EnvsuitMesh maxwellMesh, artyMesh, himarsMesh, sentryMesh;

    public TurretFriendlyItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(0.5D, 0.25D, 0.5D);
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.mulPose(Axis.YP.rotationDegrees(135F));
        TurretVariant variant = variant(stack);
        if (variant == TurretVariant.JEREMY) poses.scale(0.625F, 0.625F, 0.625F);
        if (variant == TurretVariant.TAUON) poses.scale(1.25F, 1.25F, 1.25F);
        if (variant == TurretVariant.ARTY || variant == TurretVariant.HIMARS) {
            poses.scale(0.2F, 0.2F, 0.2F);
            render(artyMesh(), "Base", ARTY, poses, buffers, light, overlay);
            if (variant == TurretVariant.ARTY) {
                render(artyMesh(), "Carriage", ARTY, poses, buffers, light, overlay);
                render(artyMesh(), "Cannon", ARTY, poses, buffers, light, overlay);
                render(artyMesh(), "Barrel", ARTY, poses, buffers, light, overlay);
            } else {
                render(himarsMesh(), "Carriage", HIMARS, poses, buffers, light, overlay);
                render(himarsMesh(), "Launcher", HIMARS, poses, buffers, light, overlay);
                render(himarsMesh(), "Crane", HIMARS, poses, buffers, light, overlay);
                render(himarsMesh(), "TubeStandard", HIMARS_STANDARD, poses, buffers, light, overlay);
            }
            poses.popPose();
            return;
        }
        if (variant == TurretVariant.SENTRY) {
            poses.scale(0.6F, 0.6F, 0.6F);
            for (String group : Set.of("Base", "Pivot", "Body", "Drum", "BarrelL", "BarrelR")) {
                render(sentryMesh(), group, SENTRY, poses, buffers, light, overlay);
            }
            poses.popPose();
            return;
        }
        boolean friendly = variant == TurretVariant.FRIENDLY;
        render(chekhovMesh(), "Base", friendly ? FRIENDLY_BASE : BASE, poses, buffers, light, overlay);
        if (variant == TurretVariant.HOWARD || variant == TurretVariant.MAXWELL) {
            render(howardMesh(), "Carriage", variant == TurretVariant.MAXWELL ? CIWS_CARRIAGE : HOWARD,
                    poses, buffers, light, overlay);
        } else {
            render(chekhovMesh(), "Carriage", friendly ? FRIENDLY_CARRIAGE : CARRIAGE,
                    poses, buffers, light, overlay);
        }
        switch (variant) {
            case CHEKHOV, FRIENDLY -> {
                render(chekhovMesh(), "Body", BODY, poses, buffers, light, overlay);
                render(chekhovMesh(), "Barrels", BARRELS, poses, buffers, light, overlay);
            }
            case JEREMY -> render(jeremyMesh(), "Gun", JEREMY, poses, buffers, light, overlay);
            case TAUON -> {
                render(tauonMesh(), "Cannon", TAUON, poses, buffers, light, overlay);
                render(tauonMesh(), "Rotor", TAUON, poses, buffers, light, overlay);
            }
            case RICHARD -> {
                render(richardMesh(), "Launcher", RICHARD, poses, buffers, light, overlay);
                render(richardMesh(), "MissileLoaded", RICHARD, poses, buffers, light, overlay);
            }
            case HOWARD -> {
                render(howardMesh(), "Body", HOWARD, poses, buffers, light, overlay);
                render(howardMesh(), "BarrelsTop", HOWARD_BARRELS, poses, buffers, light, overlay);
                render(howardMesh(), "BarrelsBottom", HOWARD_BARRELS, poses, buffers, light, overlay);
            }
            case FRITZ -> render(fritzMesh(), "Gun", FRITZ, poses, buffers, light, overlay);
            case MAXWELL -> render(maxwellMesh(), "Microwave", MAXWELL, poses, buffers, light, overlay);
            case ARTY, HIMARS, SENTRY -> { }
        }
        poses.popPose();
    }

    private void render(EnvsuitMesh mesh, String group, ResourceLocation texture, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        mesh.render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1F, light, overlay, -1);
    }

    private EnvsuitMesh chekhovMesh() {
        if (chekhovMesh == null) chekhovMesh = load(CHEKHOV_MODEL,
                Set.of("Base", "Carriage", "Body", "Barrels", "Connectors"), "turret item");
        return chekhovMesh;
    }
    private EnvsuitMesh jeremyMesh() {
        if (jeremyMesh == null) jeremyMesh = load(JEREMY_MODEL, Set.of("Gun"), "Jeremy item");
        return jeremyMesh;
    }
    private EnvsuitMesh tauonMesh() {
        if (tauonMesh == null) tauonMesh = load(TAUON_MODEL, Set.of("Cannon", "Rotor"), "Tauon item");
        return tauonMesh;
    }
    private EnvsuitMesh richardMesh() {
        if (richardMesh == null) richardMesh = load(RICHARD_MODEL,
                Set.of("Launcher", "MissileLoaded"), "Richard item");
        return richardMesh;
    }
    private EnvsuitMesh howardMesh() {
        if (howardMesh == null) howardMesh = load(HOWARD_MODEL,
                Set.of("Carriage", "Body", "BarrelsTop", "BarrelsBottom"), "Howard item");
        return howardMesh;
    }
    private EnvsuitMesh fritzMesh() {
        if (fritzMesh == null) fritzMesh = load(FRITZ_MODEL, Set.of("Gun"), "Fritz item");
        return fritzMesh;
    }
    private EnvsuitMesh maxwellMesh() {
        if (maxwellMesh == null) maxwellMesh = load(MAXWELL_MODEL, Set.of("Microwave"), "Maxwell item");
        return maxwellMesh;
    }
    private EnvsuitMesh artyMesh() {
        if (artyMesh == null) artyMesh = load(ARTY_MODEL,
                Set.of("Base", "Carriage", "Cannon", "Barrel"), "Artillery item");
        return artyMesh;
    }
    private EnvsuitMesh himarsMesh() {
        if (himarsMesh == null) himarsMesh = load(HIMARS_MODEL,
                Set.of("Carriage", "Launcher", "Crane", "TubeStandard"), "HIMARS item");
        return himarsMesh;
    }
    private EnvsuitMesh sentryMesh() {
        if (sentryMesh == null) sentryMesh = load(SENTRY_MODEL,
                Set.of("Base", "Pivot", "Body", "Drum", "BarrelL", "BarrelR"), "Sentry item");
        return sentryMesh;
    }
    private static EnvsuitMesh load(ResourceLocation model, Set<String> groups, String name) {
        return EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), model, groups, name);
    }
    private static TurretVariant variant(ItemStack stack) {
        if (stack.is(ModItems.TURRET_CHEKHOV_ITEM.get())) return TurretVariant.CHEKHOV;
        if (stack.is(ModItems.TURRET_JEREMY_ITEM.get())) return TurretVariant.JEREMY;
        if (stack.is(ModItems.TURRET_TAUON_ITEM.get())) return TurretVariant.TAUON;
        if (stack.is(ModItems.TURRET_RICHARD_ITEM.get())) return TurretVariant.RICHARD;
        if (stack.is(ModItems.TURRET_HOWARD_ITEM.get())) return TurretVariant.HOWARD;
        if (stack.is(ModItems.TURRET_FRITZ_ITEM.get())) return TurretVariant.FRITZ;
        if (stack.is(ModItems.TURRET_MAXWELL_ITEM.get())) return TurretVariant.MAXWELL;
        if (stack.is(ModItems.TURRET_ARTY_ITEM.get())) return TurretVariant.ARTY;
        if (stack.is(ModItems.TURRET_HIMARS_ITEM.get())) return TurretVariant.HIMARS;
        if (stack.is(ModItems.TURRET_SENTRY_ITEM.get())) return TurretVariant.SENTRY;
        return TurretVariant.FRIENDLY;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
