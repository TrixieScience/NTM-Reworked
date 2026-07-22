package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.Stg77Item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Grouped STG77 model with its Blender-authored animation schedule. */
public final class Stg77ItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/stg77.obj");
    private static final ResourceLocation ANIMATION = id("models/weapons/animations/stg77.json");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/stg77.png");
    private static final Set<String> GROUPS = Set.of(
            "Gun", "Barrel", "Lever", "Magazine", "Safety", "Handle", "Breech", "Bullets");

    private EnvsuitMesh mesh;
    private LegacyWeaponAnimation animations;

    public Stg77ItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static void validateAnimationResources(ResourceManager resources) {
        LegacyWeaponAnimation.load(resources, ANIMATION);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof Stg77Item)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        boolean hiddenByScope = firstPerson && ClientWeaponEvents.fullyAimed();
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);

        poses.pushPose();
        setupContext(context, poses);
        if (!hiddenByScope) {
            if (firstPerson) renderFirstPerson(stack, poses, buffers, light, overlay);
            else renderStatic(poses, buffers, light, overlay);
            if (held && elapsed >= 0L && elapsed < 75L) {
                renderFlash(poses, buffers, elapsed, ClientWeaponEvents.shotRandom(stack));
            }
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay) {
        double time = animationTime(stack);
        String clip = animationClip(stack);

        poses.scale(0.5F, 0.5F, 0.5F);
        pivotX(poses, 0.0D, -1.0D, -4.0D, equipAngle(stack, time));
        poses.translate(0.0D, 0.0D, recoilOffset(stack, time));

        // Gun is the authored root and remains applied while its child groups render.
        poses.pushPose();
        apply(poses, clip, "Gun", time);
        render("Gun", poses, buffers, light, overlay);

        poses.pushPose();
        apply(poses, clip, "Magazine", time);
        render("Magazine", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        apply(poses, clip, "Lever", time);
        render("Lever", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.pushPose();
        apply(poses, clip, "Breech", time);
        render("Breech", poses, buffers, light, overlay);
        poses.popPose();
        apply(poses, clip, "Handle", time);
        render("Handle", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(safetyOffset(stack, time), 0.0D, 0.0D);
        apply(poses, clip, "Safety", time);
        render("Safety", poses, buffers, light, overlay);
        poses.popPose();
        poses.popPose();

        // Barrel shares the Gun root but insists on its own container.
        poses.pushPose();
        apply(poses, clip, "Gun", time);
        apply(poses, clip, "Barrel", time);
        render("Barrel", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.0D, 8.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.75F, 0.75F, 0.75F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.5D,
                WeaponSmokeRenderer.SEVEN_SIX_TWO,
                Stg77Item.state(stack) == Stg77Item.GunState.RELOADING);
        poses.popPose();
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        render("Gun", poses, buffers, light, overlay);
        render("Barrel", poses, buffers, light, overlay);
        render("Lever", poses, buffers, light, overlay);
        render("Magazine", poses, buffers, light, overlay);
        render("Safety", poses, buffers, light, overlay);
        render("Handle", poses, buffers, light, overlay);
        render("Breech", poses, buffers, light, overlay);
    }

    private void apply(PoseStack poses, String clip, String group, double time) {
        if (clip != null) LegacyWeaponAnimation.apply(poses, animations().transform(clip, group, time));
    }

    private void render(String group, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS, "StG 77");
        }
        return mesh;
    }

    private LegacyWeaponAnimation animations() {
        if (animations == null) {
            animations = LegacyWeaponAnimation.load(Minecraft.getInstance().getResourceManager(), ANIMATION);
        }
        return animations;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                // ItemRenderWeaponBase.setupInv followed by ItemRenderSTG77's concrete pose.
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.375F / 16.0F, 1.375F / 16.0F, 1.375F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(-0.5D, 0.5D, 0.0D);
            }
            case GROUND -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case FIXED -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(context, poses);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 1.2D, 0.0D, aim),
                        lerp(-0.8D, -0.71875D, aim), 2.0D);
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
        poses.scale(1.5F, 1.5F, 1.5F);
        poses.translate(0.0D, 1.0D, 2.0D);
    }

    private static String animationClip(ItemStack stack) {
        return switch (Stg77Item.animation(stack)) {
            case CYCLE_DRY -> "FireDry";
            case RELOAD -> "Reload";
            case INSPECT -> "Inspect";
            default -> null;
        };
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (Stg77Item.animationTimer(stack) + partial) * 50.0D;
    }

    private static double equipAngle(ItemStack stack, double time) {
        if (Stg77Item.animation(stack) != Stg77Item.GunAnimation.EQUIP) return 0.0D;
        double progress = clamp(time / 500.0D);
        return 45.0D * (1.0D - sinFull(progress));
    }

    private static double recoilOffset(ItemStack stack, double time) {
        if (Stg77Item.animation(stack) != Stg77Item.GunAnimation.CYCLE) return 0.0D;
        double amount = Stg77Item.aiming(stack) ? -0.125D : -0.375D;
        if (time < 25.0D) return amount * Math.sin(clamp(time / 25.0D) * Math.PI * 0.5D);
        if (time < 100.0D) return amount + (0.0D - amount) * sinFull((time - 25.0D) / 75.0D);
        return 0.0D;
    }

    private static double safetyOffset(ItemStack stack, double time) {
        if (Stg77Item.animation(stack) != Stg77Item.GunAnimation.CYCLE) return 0.0D;
        if (time <= 2_000.0D) return 0.25D;
        if (time < 2_050.0D) return 0.25D * (1.0D - clamp((time - 2_000.0D) / 50.0D));
        return 0.0D;
    }

    private static void renderFlash(PoseStack poses, MultiBufferSource buffers, long elapsed, float random) {
        poses.pushPose();
        poses.translate(0.0D, 0.0D, 7.5D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.25F, 0.25F, 0.25F);
        poses.mulPose(Axis.XP.rotationDegrees(-5.0F + random * 10.0F));
        SednaMuzzleFlash.render(poses, buffers, elapsed / 75.0F, 10.0F);
        poses.popPose();
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, double angle) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) angle));
        poses.translate(-x, -y, -z);
    }

    private static double lerp(double start, double end, double amount) {
        return start + (end - start) * amount;
    }

    private static double sinFull(double progress) {
        return (-Math.cos(clamp(progress) * Math.PI) + 1.0D) * 0.5D;
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(value, 1.0D));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
