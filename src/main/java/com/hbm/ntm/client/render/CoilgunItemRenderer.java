package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.CoilgunItem;
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

public final class CoilgunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/coilgun.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/coilgun.png");
    private static final Set<String> GROUPS = Set.of("Coils_Cylinder.012");
    private EnvsuitMesh mesh;

    public CoilgunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof CoilgunItem)) return;
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
        float time = (CoilgunItem.animationTimer(stack) + partial) * 50.0F;
        CoilgunItem.GunAnimation animation = CoilgunItem.animation(stack);
        float recoil = recoil(animation, time, CoilgunItem.aiming(stack));
        float reload = reload(animation, time);

        poses.scale(0.75F, 0.75F, 0.75F);
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        poses.translate(-1.5D - recoil * 0.5D, 0.0D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees(recoil * 45.0F));
        poses.translate(1.5D, 0.0D, 0.0D);
        poses.translate(-2.5D, 0.0D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees(reload * -45.0F));
        poses.translate(2.5D, 0.0D, 0.0D);
        render(poses, buffers, light, overlay);
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        render(poses, buffers, light, overlay);
    }

    private void render(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render("Coils_Cylinder.012", poses.last(),
                buffers.getBuffer(RenderType.entityCutout(TEXTURE)), 1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Coilgun");
        }
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
                poses.scale(0.25F, 0.25F, 0.25F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(-0.25D, -0.25D, 0.0D);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                poses.scale(3.0F, 3.0F, 3.0F);
                poses.translate(0.0D, 0.25D, 1.25D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side, 0.0D, aim),
                        lerp(-1.2D, -0.9375D, aim), lerp(2.0D, 1.0D, aim));
            }
            case GROUND, FIXED -> poses.scale(0.125F, 0.125F, 0.125F);
            default -> poses.scale(0.075F, 0.075F, 0.075F);
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

    private static float recoil(CoilgunItem.GunAnimation animation, float time, boolean aiming) {
        if (animation != CoilgunItem.GunAnimation.CYCLE) return 0.0F;
        float peak = aiming ? 0.5F : 1.0F;
        if (time < 100.0F) return lerp(0.0F, peak, time / 100.0F);
        if (time < 300.0F) return lerp(peak, 0.0F, (time - 100.0F) / 200.0F);
        return 0.0F;
    }

    private static float reload(CoilgunItem.GunAnimation animation, float time) {
        if (animation == CoilgunItem.GunAnimation.EQUIP) {
            return lerp(1.0F, 0.0F, time / 250.0F);
        }
        if (animation != CoilgunItem.GunAnimation.RELOAD) return 0.0F;
        if (time < 250.0F) return lerp(0.0F, 1.0F, time / 250.0F);
        if (time < 750.0F) return 1.0F;
        if (time < 1_000.0F) return lerp(1.0F, 0.0F, (time - 750.0F) / 250.0F);
        return 0.0F;
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
}
