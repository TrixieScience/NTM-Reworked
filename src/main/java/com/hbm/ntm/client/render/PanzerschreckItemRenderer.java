package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.RocketLauncherItem;
import com.hbm.ntm.weapon.WeaponModManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Original Panzerschreck model, authored poses, reload choreography, rocket, shield, and plume. */
public final class PanzerschreckItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/panzerschreck.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/panzerschreck.png");
    private static final Set<String> GROUPS = Set.of("Tube", "Shield", "Rocket");

    private EnvsuitMesh mesh;

    public PanzerschreckItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof RocketLauncherItem)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);
        boolean flash = held && elapsed >= 0L && elapsed < 150L;

        poses.pushPose();
        setupContext(context, poses);
        if (firstPerson) renderFirstPerson(stack, poses, buffers, light, overlay);
        else renderStatic(stack, poses, buffers, light, overlay);
        if (flash) renderFlash(poses, buffers, elapsed / 150.0F,
                ClientWeaponEvents.shotRandom(stack));
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses,
                                   MultiBufferSource buffers, int light, int overlay) {
        poses.scale(1.25F, 1.25F, 1.25F);
        pivotX(poses, 0.0D, -1.0D, -1.0D, equipAngle(stack));
        pivotX(poses, 0.0D, -4.0D, -3.0D, reloadAngle(stack));
        render("Tube", poses, buffers, light, overlay);
        if (hasShield(stack)) render("Shield", poses, buffers, light, overlay);

        Vec rocket = rocketPosition(stack);
        poses.pushPose();
        poses.translate(rocket.x, rocket.y, rocket.z);
        render("Rocket", poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderStatic(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                              int light, int overlay) {
        // ItemRenderPanzerschreck.renderOther deliberately omitted the loaded Rocket group.
        render("Tube", poses, buffers, light, overlay);
        if (hasShield(stack)) render("Shield", poses, buffers, light, overlay);
    }

    private static boolean hasShield(ItemStack stack) {
        return !WeaponModManager.hasMod(stack, 0, WeaponModManager.NO_SHIELD);
    }

    private void render(String group, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                MODEL, GROUPS, "Panzerschreck");
        return mesh;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.5F / 16.0F, 1.5F / 16.0F, 1.5F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(-0.5D, 0.5D, 0.0D);
            }
            case GROUND, FIXED -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(context, poses);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 2.2D, side * 0.9375D, aim),
                        lerp(-1.6D, -1.15625D, aim), lerp(2.0D, 0.25D, aim));
            }
            default -> {
                poses.scale(0.125F, 0.125F, 0.125F);
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
        poses.scale(3.0F, 3.0F, 3.0F);
        poses.translate(0.0D, 0.5D, 1.0D);
    }

    private static double equipAngle(ItemStack stack) {
        if (RocketLauncherItem.animation(stack) != RocketLauncherItem.GunAnimation.EQUIP) return 0.0D;
        double duration = hasShield(stack) ? 500.0D : 250.0D;
        double progress = Mth.clamp(animationTime(stack) / duration, 0.0D, 1.0D);
        return 60.0D + (0.0D - 60.0D) * Math.sin(progress * Math.PI * 0.5D);
    }

    private static double reloadAngle(ItemStack stack) {
        RocketLauncherItem.GunAnimation animation = RocketLauncherItem.animation(stack);
        double time = animationTime(stack);
        if (animation == RocketLauncherItem.GunAnimation.RELOAD) {
            if (time < 750.0D) return 90.0D * sinFull(time / 750.0D);
            if (time < 1750.0D) return 90.0D;
            if (time < 2500.0D) return 90.0D * (1.0D - sinFull((time - 1750.0D) / 750.0D));
            return 0.0D;
        }
        if (animation == RocketLauncherItem.GunAnimation.INSPECT
                || animation == RocketLauncherItem.GunAnimation.JAMMED) {
            if (time < 750.0D) return 90.0D * sinFull(time / 750.0D);
            if (time < 1250.0D) return 90.0D;
            if (time < 2000.0D) return 90.0D * (1.0D - sinFull((time - 1250.0D) / 750.0D));
        }
        return 0.0D;
    }

    private static Vec rocketPosition(ItemStack stack) {
        RocketLauncherItem.GunAnimation animation = RocketLauncherItem.animation(stack);
        double time = animationTime(stack);
        if (animation == RocketLauncherItem.GunAnimation.RELOAD) {
            if (time < 750.0D) return new Vec(0.0D, -3.0D, -6.0D);
            if (time < 1250.0D) {
                double amount = Math.sin(Mth.clamp((time - 750.0D) / 500.0D, 0.0D, 1.0D)
                        * Math.PI * 0.5D);
                return new Vec(0.0D, lerp(-3.0D, 0.0D, amount), lerp(-6.0D, -6.5D, amount));
            }
            if (time < 1600.0D) {
                double amount = 1.0D - Math.cos(Mth.clamp((time - 1250.0D) / 350.0D, 0.0D, 1.0D)
                        * Math.PI * 0.5D);
                return new Vec(0.0D, 0.0D, lerp(-6.5D, 0.0D, amount));
            }
            return Vec.ZERO;
        }
        if (animation == RocketLauncherItem.GunAnimation.INSPECT) {
            return new Vec(0.0D, RocketLauncherItem.rounds(stack) <= 0 ? -3.0D : 0.0D, 0.0D);
        }
        return Vec.ZERO;
    }

    private static void renderFlash(PoseStack poses, MultiBufferSource buffers,
                                    float progress, float random) {
        poses.pushPose();
        poses.translate(0.0D, 0.0D, 6.5D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(random * 90.0F));
        poses.scale(0.75F, 0.75F, 0.75F);
        SednaMuzzleFlash.render(poses, buffers, progress, 7.5F);
        poses.popPose();
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (RocketLauncherItem.animationTimer(stack) + partial) * 50.0D;
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, double angle) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) angle));
        poses.translate(-x, -y, -z);
    }

    private static double sinFull(double progress) {
        return (-Math.cos(Mth.clamp(progress, 0.0D, 1.0D) * Math.PI) + 1.0D) * 0.5D;
    }

    private static double lerp(double start, double end, double amount) {
        return start + (end - start) * amount;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private record Vec(double x, double y, double z) {
        private static final Vec ZERO = new Vec(0.0D, 0.0D, 0.0D);
    }
}
