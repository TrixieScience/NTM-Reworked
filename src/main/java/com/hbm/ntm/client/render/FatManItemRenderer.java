package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.FatManItem;
import com.hbm.ntm.weapon.MiniNukeAmmoType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class FatManItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/fatman.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/fatman.png");
    private static final ResourceLocation NUKE_TEXTURE = id("textures/models/weapons/fatman_mininuke.png");
    private static final ResourceLocation BALEFIRE_TEXTURE = id("textures/models/weapons/fatman_balefire.png");
    private static final Set<String> GROUPS = Set.of(
            "Launcher", "Handle", "Gauge", "Lid", "Piston", "MiniNuke");
    private EnvsuitMesh mesh;

    public FatManItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof FatManItem)) return;
        boolean first = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        poses.pushPose();
        setupContext(context, poses);
        if (first) renderFirstPerson(stack, poses, buffers, light, overlay);
        else renderStatic(stack, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float time = (FatManItem.animationTimer(stack) + partial) * 50.0F;
        FatManItem.GunAnimation animation = FatManItem.animation(stack);
        boolean loaded = FatManItem.rounds(stack) > 0;

        poses.scale(0.5F, 0.5F, 0.5F);
        pivotX(poses, 0.0D, 1.0D, -2.0D, equipAngle(animation, time));
        render("Launcher", TEXTURE, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 0.0D, handleZ(animation, time));
        render("Handle", TEXTURE, poses, buffers, light, overlay);
        poses.translate(0.4375D, -0.875D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees(gaugeAngle(stack, animation, time)));
        poses.translate(-0.4375D, 0.875D, 0.0D);
        render("Gauge", TEXTURE, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.25D, 0.125D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees(lidAngle(animation, time)));
        poses.translate(-0.25D, -0.125D, 0.0D);
        render("Lid", TEXTURE, poses, buffers, light, overlay);
        poses.popPose();

        float piston = pistonZ(animation, time);
        poses.pushPose();
        poses.translate(0.0D, 0.0D, piston);
        if (!loaded && piston == 0.0F) poses.translate(0.0D, 0.0D, 3.0D);
        render("Piston", TEXTURE, poses, buffers, light, overlay);
        poses.popPose();

        Vec3f nuke = nukePosition(animation, time);
        if (loaded || nuke.nonzero()) {
            poses.pushPose();
            poses.translate(nuke.x, nuke.y, nuke.z);
            renderNuke(FatManItem.loadedAmmo(stack), poses, buffers, light, overlay);
            poses.popPose();
        }
    }

    private void renderStatic(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                              int light, int overlay) {
        boolean loaded = FatManItem.rounds(stack) > 0;
        render("Launcher", TEXTURE, poses, buffers, light, overlay);
        render("Handle", TEXTURE, poses, buffers, light, overlay);
        render("Gauge", TEXTURE, poses, buffers, light, overlay);
        render("Lid", TEXTURE, poses, buffers, light, overlay);
        poses.pushPose();
        if (!loaded) poses.translate(0.0D, 0.0D, 3.0D);
        render("Piston", TEXTURE, poses, buffers, light, overlay);
        poses.popPose();
        if (loaded) renderNuke(FatManItem.loadedAmmo(stack), poses, buffers, light, overlay);
    }

    private void renderNuke(MiniNukeAmmoType ammo, PoseStack poses, MultiBufferSource buffers,
                            int light, int overlay) {
        ResourceLocation texture = ammo == MiniNukeAmmoType.BALEFIRE
                ? BALEFIRE_TEXTURE : NUKE_TEXTURE;
        render("MiniNuke", texture, poses, buffers, light, overlay);
        if (ammo == MiniNukeAmmoType.BALEFIRE) {
            mesh().render("MiniNuke", poses.last(), buffers.getBuffer(RenderType.entityGlint()),
                    1.0F, light, OverlayTexture.NO_OVERLAY, 0xFF00CC26);
        }
    }

    private void render(String group, ResourceLocation texture, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Fat Man");
        }
        return mesh;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.375F / 16.0F, 1.375F / 16.0F, 1.375F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(0.0D, -0.5D, 0.0D);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                poses.scale(2.5F, 2.5F, 2.5F);
                poses.translate(-0.5D, 0.5D, -3.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 1.2D, side * 0.8D, aim),
                        lerp(-1.0D, -1.0D, aim), lerp(0.4D, 0.0D, aim));
            }
            case GROUND, FIXED -> {
                poses.scale(0.0625F, 0.0625F, 0.0625F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            default -> {
                poses.scale(0.05F, 0.05F, 0.05F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void setupThirdPerson(ItemDisplayContext context, PoseStack poses) {
        float side = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? -1.0F : 1.0F;
        poses.translate(-side / 16.0D, -0.125D, 0.625D);
        poses.mulPose(Axis.YN.rotationDegrees(180.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F));
        poses.translate(-side / 16.0D, 0.4375D, 0.0625D);
        poses.translate(side * 0.25D, 0.1875D, -0.1875D);
        poses.scale(0.375F, 0.375F, 0.375F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 60.0F));
        poses.mulPose(Axis.XN.rotationDegrees(90.0F));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 20.0F));
        poses.translate(0.0D, -0.3D, 0.0D);
        poses.scale(1.5F, 1.5F, 1.5F);
        poses.mulPose(Axis.YP.rotationDegrees(side * 50.0F));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 335.0F));
        poses.translate(-side * 0.9375D, -0.0625D, 0.0D);
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 15.0F));
        poses.mulPose(Axis.YP.rotationDegrees(side * 12.5F));
        poses.mulPose(Axis.XP.rotationDegrees(15.0F));
        poses.translate(side * 3.5D, 0.0D, 0.0D);
    }

    private static float equipAngle(FatManItem.GunAnimation animation, float time) {
        if (animation == FatManItem.GunAnimation.EQUIP) {
            return lerp(60.0F, 0.0F, sineDown(time / 1_000.0F));
        }
        if (animation == FatManItem.GunAnimation.RELOAD) {
            if (time < 500.0F) return lerp(0.0F, 5.0F, smooth(time / 500.0F));
            if (time < 1_000.0F) return lerp(5.0F, 0.0F, smooth((time - 500.0F) / 500.0F));
            if (time < 1_450.0F) return 0.0F;
            if (time < 1_550.0F) return lerp(0.0F, 3.0F, sineDown((time - 1_450.0F) / 100.0F));
            if (time < 1_650.0F) return lerp(3.0F, 0.0F, smooth((time - 1_550.0F) / 100.0F));
            if (time < 2_150.0F) return 0.0F;
            if (time < 2_525.0F) return lerp(0.0F, -10.0F, sineDown((time - 2_150.0F) / 375.0F));
            if (time < 2_900.0F) return lerp(-10.0F, 0.0F, sineUp((time - 2_525.0F) / 375.0F));
        }
        if (animation == FatManItem.GunAnimation.JAMMED) {
            if (time < 500.0F) return 0.0F;
            if (time < 750.0F) return lerp(0.0F, -15.0F, smooth((time - 500.0F) / 250.0F));
            if (time < 1_750.0F) return -15.0F;
            if (time < 2_000.0F) return lerp(-15.0F, 0.0F, smooth((time - 1_750.0F) / 250.0F));
        }
        if (animation == FatManItem.GunAnimation.INSPECT) {
            if (time < 250.0F) return lerp(0.0F, -15.0F, smooth(time / 250.0F));
            if (time < 1_250.0F) return -15.0F;
            if (time < 1_500.0F) return lerp(-15.0F, 0.0F, smooth((time - 1_250.0F) / 250.0F));
        }
        return 0.0F;
    }

    private static float handleZ(FatManItem.GunAnimation animation, float time) {
        if (animation == FatManItem.GunAnimation.RELOAD) {
            if (time < 500.0F) return lerp(0.0F, -2.0F, smooth(time / 500.0F));
            if (time < 2_200.0F) return -2.0F;
            if (time < 2_950.0F) return lerp(-2.0F, 0.0F, smooth((time - 2_200.0F) / 750.0F));
        }
        if (animation == FatManItem.GunAnimation.JAMMED) {
            return doubleHandle(time, 750.0F);
        }
        if (animation == FatManItem.GunAnimation.INSPECT) {
            return doubleHandle(time, 250.0F);
        }
        return 0.0F;
    }

    private static float doubleHandle(float time, float delay) {
        if (time < delay) return 0.0F;
        float phase = time - delay;
        if (phase < 250.0F) return lerp(0.0F, -2.0F, smooth(phase / 250.0F));
        if (phase < 500.0F) return lerp(-2.0F, 0.0F, smooth((phase - 250.0F) / 250.0F));
        if (phase < 750.0F) return lerp(0.0F, -2.0F, smooth((phase - 500.0F) / 250.0F));
        if (phase < 1_000.0F) return lerp(-2.0F, 0.0F, smooth((phase - 750.0F) / 250.0F));
        return 0.0F;
    }

    private static float gaugeAngle(ItemStack stack, FatManItem.GunAnimation animation, float time) {
        if (animation != FatManItem.GunAnimation.CYCLE) return 0.0F;
        float target = 135.0F + Math.round(ClientWeaponEvents.shotRandom(stack) * 135.0F);
        if (time < 100.0F) return lerp(0.0F, target, sineDown(time / 100.0F));
        if (time < 600.0F) return lerp(target, 0.0F, sineDown((time - 100.0F) / 500.0F));
        return 0.0F;
    }

    private static float lidAngle(FatManItem.GunAnimation animation, float time) {
        if (animation != FatManItem.GunAnimation.RELOAD || time < 250.0F) return 0.0F;
        if (time < 500.0F) return lerp(0.0F, -45.0F, sineUp((time - 250.0F) / 250.0F));
        if (time < 1_700.0F) return -45.0F;
        if (time < 1_950.0F) return lerp(-45.0F, 0.0F, sineUp((time - 1_700.0F) / 250.0F));
        return 0.0F;
    }

    private static float pistonZ(FatManItem.GunAnimation animation, float time) {
        if (animation == FatManItem.GunAnimation.CYCLE && time < 100.0F) {
            return lerp(0.0F, 3.0F, sineUp(time / 100.0F));
        }
        if (animation == FatManItem.GunAnimation.RELOAD) {
            if (time < 2_200.0F) return 3.0F;
            if (time < 2_950.0F) return lerp(3.0F, 0.0F, smooth((time - 2_200.0F) / 750.0F));
        }
        return 0.0F;
    }

    private static Vec3f nukePosition(FatManItem.GunAnimation animation, float time) {
        if (animation == FatManItem.GunAnimation.CYCLE && time < 100.0F) {
            return new Vec3f(0.0F, 0.0F, lerp(0.0F, 3.0F, sineUp(time / 100.0F)));
        }
        if (animation != FatManItem.GunAnimation.RELOAD) return Vec3f.ZERO;
        if (time < 750.0F) return new Vec3f(5.0F, -4.0F, 3.0F);
        if (time < 1_250.0F) return Vec3f.lerp(new Vec3f(5.0F, -4.0F, 3.0F),
                new Vec3f(2.0F, 0.5F, 3.0F), sineUp((time - 750.0F) / 500.0F));
        if (time < 1_350.0F) return Vec3f.lerp(new Vec3f(2.0F, 0.5F, 3.0F),
                new Vec3f(1.0F, 0.5F, 3.0F), (time - 1_250.0F) / 100.0F);
        if (time < 1_450.0F) return Vec3f.lerp(new Vec3f(1.0F, 0.5F, 3.0F),
                new Vec3f(0.0F, 0.0F, 3.0F), (time - 1_350.0F) / 100.0F);
        if (time < 2_200.0F) return new Vec3f(0.0F, 0.0F, 3.0F);
        if (time < 2_950.0F) return Vec3f.lerp(new Vec3f(0.0F, 0.0F, 3.0F), Vec3f.ZERO,
                smooth((time - 2_200.0F) / 750.0F));
        return Vec3f.ZERO;
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, float angle) {
        if (angle == 0.0F) return;
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees(angle));
        poses.translate(-x, -y, -z);
    }

    private static float sineDown(float progress) {
        return (float) Math.sin(Math.PI * 0.5D * Mth.clamp(progress, 0.0F, 1.0F));
    }

    private static float sineUp(float progress) {
        return 1.0F - (float) Math.cos(Math.PI * 0.5D * Mth.clamp(progress, 0.0F, 1.0F));
    }

    private static float smooth(float progress) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);
        return (1.0F - (float) Math.cos(Math.PI * clamped)) * 0.5F;
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * Mth.clamp(progress, 0.0F, 1.0F);
    }

    private static double lerp(double from, double to, float progress) {
        return from + (to - from) * Mth.clamp(progress, 0.0F, 1.0F);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private record Vec3f(float x, float y, float z) {
        private static final Vec3f ZERO = new Vec3f(0.0F, 0.0F, 0.0F);
        private boolean nonzero() { return x != 0.0F || y != 0.0F || z != 0.0F; }
        private static Vec3f lerp(Vec3f from, Vec3f to, float progress) {
            return new Vec3f(FatManItemRenderer.lerp(from.x, to.x, progress),
                    FatManItemRenderer.lerp(from.y, to.y, progress),
                    FatManItemRenderer.lerp(from.z, to.z, progress));
        }
    }
}
