package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.TauGunItem;
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

public final class TauGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/tau.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/tau.png");
    private static final Set<String> GROUPS = Set.of("Body", "Rotor");
    private EnvsuitMesh mesh;

    public TauGunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof TauGunItem)) return;
        boolean first = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        poses.pushPose();
        setupContext(context, poses);
        if (first) renderFirstPerson(stack, poses, buffers, light, overlay);
        else renderStatic(poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float time = (TauGunItem.animationTimer(stack) + partial) * 50.0F;
        TauGunItem.GunAnimation animation = TauGunItem.animation(stack);

        poses.scale(0.75F, 0.75F, 0.75F);
        pivotX(poses, 0.0D, -1.0D, -4.0D, equipAngle(animation, time));
        float recoil = recoilZ(animation, time);
        poses.translate(0.0D, 0.0D, recoil);
        pivotX(poses, 0.0D, 0.0D, -2.0D, recoil * 5.0F);

        render("Body", poses, buffers, light, overlay);
        poses.pushPose();
        poses.translate(0.0D, -0.25D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees(rotorAngle(animation, time)));
        poses.translate(0.0D, 0.25D, 0.0D);
        render("Rotor", poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        render("Body", poses, buffers, light, overlay);
        render("Rotor", poses, buffers, light, overlay);
    }

    private void render(String group, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Tau Cannon");
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
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(-0.25D, 0.5D, 0.0D);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                poses.scale(2.5F, 2.5F, 2.5F);
                poses.translate(0.0D, 1.0D, 2.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(side * 1.4D, -1.4D, 2.8D);
            }
            case GROUND -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            default -> {
                poses.scale(0.075F, 0.075F, 0.075F);
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

    private static float equipAngle(TauGunItem.GunAnimation animation, float time) {
        if (animation == TauGunItem.GunAnimation.EQUIP) {
            return lerp(45.0F, 0.0F, smooth(time / 500.0F));
        }
        if (animation == TauGunItem.GunAnimation.INSPECT) {
            if (time < 150.0F) return lerp(0.0F, 2.0F, sineDown(time / 150.0F));
            if (time < 250.0F) return lerp(2.0F, 0.0F, smooth((time - 150.0F) / 100.0F));
        }
        return 0.0F;
    }

    private static float recoilZ(TauGunItem.GunAnimation animation, float time) {
        if (animation == TauGunItem.GunAnimation.CYCLE) {
            if (time < 50.0F) return lerp(0.0F, -0.5F, time / 50.0F);
            if (time < 200.0F) return lerp(-0.5F, 0.0F, smooth((time - 50.0F) / 150.0F));
        }
        if (animation == TauGunItem.GunAnimation.ALT_CYCLE) {
            if (time < 100.0F) return lerp(0.0F, -3.0F, sineDown(time / 100.0F));
            if (time < 350.0F) return lerp(-3.0F, 0.0F, smooth((time - 100.0F) / 250.0F));
        }
        return 0.0F;
    }

    private static float rotorAngle(TauGunItem.GunAnimation animation, float time) {
        if (animation == TauGunItem.GunAnimation.CYCLE
                || animation == TauGunItem.GunAnimation.ALT_CYCLE) {
            if (time < 50.0F) return lerp(0.0F, -5.0F, sineDown(time / 50.0F));
            if (time < 150.0F) return lerp(-5.0F, 5.0F, smooth((time - 50.0F) / 100.0F));
            if (time < 200.0F) return lerp(5.0F, 0.0F, sineUp((time - 150.0F) / 50.0F));
        }
        if (animation == TauGunItem.GunAnimation.INSPECT) {
            return lerp(0.0F, -1080.0F, sineDown(time / 1500.0F));
        }
        if (animation == TauGunItem.GunAnimation.SPINUP) {
            if (time < 3000.0F) return lerp(0.0F, 2160.0F, sineUp(time / 3000.0F));
            return lerp(0.0F, 14400.0F, (time - 3000.0F) / 10000.0F);
        }
        return 0.0F;
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
