package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.M2Item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

/** Every M2 Browning mesh group, because subtlety was never in the contract. */
public final class M2ItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/m2_browning.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/m2_browning.png");
    private static final List<String> GROUPS = List.of(
            "Cube_Cube.001", "Cube.001_Cube.002", "Cube.002_Cube.003", "Cube.003_Cube.004",
            "Cube.004_Cube.005", "Cube.005_Cube.006", "Cube.006_Cube.007", "Cube.007_Cube.030",
            "Cube.008_Cube.031", "Cube.009_Cube", "Cube.010_Cube.011", "Cube.014_Cube.019",
            "Cube.015_Cube.020", "Cube.013_Cube.021", "Cube.016_Cube.024", "Cube.017_Cube.027",
            "Cube.018_Cube.034", "Cube.019_Cube.035", "Cube.020_Cube.036", "Cube.033_Cube.052");

    private EnvsuitMesh mesh;

    public M2ItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof M2Item)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);

        poses.pushPose();
        setupContext(context, poses);
        if (firstPerson) {
            double time = M2Item.animationTimer(stack) * 50.0D;
            double equip = M2Item.animation(stack) == M2Item.GunAnimation.EQUIP
                    ? sequence(time, 80.0D, 0.0D, 500.0D) : 0.0D;
            double recoil = M2Item.animation(stack) == M2Item.GunAnimation.CYCLE
                    ? recoil(time) : 0.0D;
            poses.scale(0.75F, 0.75F, 0.75F);
            poses.translate(0.0D, 1.0D, -2.25D);
            poses.mulPose(Axis.XP.rotationDegrees((float) equip));
            poses.translate(0.0D, -1.0D, 2.25D + recoil);
        }
        poses.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderAll(poses, buffers, light, overlay);
        if (firstPerson) {
            poses.pushPose();
            poses.translate(0.0D, 1.625D, 5.0D);
            poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            poses.scale(0.5F, 0.5F, 0.5F);
            WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.375D,
                    WeaponSmokeRenderer.FIFTY, false);
            poses.popPose();
        }
        if (held && elapsed >= 0L && elapsed < 75L) {
            poses.mulPose(Axis.YP.rotationDegrees(180.0F));
            renderFlash(poses, buffers, elapsed, ClientWeaponEvents.shotRandom(stack));
        }
        poses.popPose();
    }

    private void renderAll(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        var consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        for (String group : GROUPS) mesh().render(group, poses.last(), consumer, 1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL,
                    Set.copyOf(GROUPS), "M2 Browning");
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
                poses.scale(2.625F / 16.0F, 2.625F / 16.0F, 2.625F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(0.5D, -1.25D, 0.0D);
            }
            case GROUND, FIXED -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                poses.scale(5.0F, 5.0F, 5.0F);
                poses.translate(0.5D, -2.0D, 3.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 1.2D, 0.0D, aim),
                        lerp(-2.0D, -1.5625D, aim), lerp(1.4D, 1.75D, aim));
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

    private static void renderFlash(PoseStack poses, MultiBufferSource buffers, long elapsed, float random) {
        poses.pushPose();
        poses.translate(0.0D, 1.625D, 5.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(random * 90.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        SednaMuzzleFlash.render(poses, buffers, elapsed / 75.0F, 7.5F);
        poses.popPose();
    }

    private static double sequence(double time, double start, double end, double duration) {
        if (time <= 0.0D) return start;
        if (time >= duration) return end;
        double t = time / duration;
        return start + (end - start) * (0.5D - Math.cos(Math.PI * t) * 0.5D);
    }

    private static double recoil(double time) {
        if (time <= 50.0D) return -0.25D * Math.sin(Math.PI * time / 100.0D);
        if (time >= 125.0D) return 0.0D;
        return -0.25D * (1.0D - (time - 50.0D) / 75.0D);
    }

    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
