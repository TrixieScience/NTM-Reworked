package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.DualMinigunItem;
import com.hbm.ntm.item.LacunaeItem;
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

/** The last two miniguns share a mesh and disagree about quantity. */
public final class FinalMinigunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/minigun.obj");
    private static final ResourceLocation LACUNAE = id("textures/models/weapons/minigun_lacunae.png");
    private static final ResourceLocation DUAL = id("textures/models/weapons/minigun_dual.png");
    private static final Set<String> GROUPS = Set.of("Gun", "Grip", "Barrels", "GunDual");

    private EnvsuitMesh mesh;

    public FinalMinigunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        boolean dual = stack.getItem() instanceof DualMinigunItem;
        if (!dual && !(stack.getItem() instanceof LacunaeItem)) return;
        boolean first = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        poses.pushPose();
        setupContext(context, poses, dual);
        if (first) {
            if (dual) {
                renderDualFirstPerson(stack, poses, buffers, light, overlay);
            } else {
                renderLacunaeFirstPerson(stack, poses, buffers, light, overlay);
            }
        } else {
            renderStatic(context, dual, poses, buffers, light, overlay);
            if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                int index = dual && context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? 0 : 1;
                flash(stack, dual ? index : 0, false, !dual, poses, buffers);
            }
        }
        poses.popPose();
    }

    private void renderLacunaeFirstPerson(ItemStack stack, PoseStack poses,
                                          MultiBufferSource buffers, int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        MiniAnimation animation = animation(stack, 0);
        poses.scale(0.375F, 0.375F, 0.375F);
        pivotX(poses, 0.0D, 3.0D, -6.0D, animation.equip);
        poses.translate(0.0D, 0.0D, animation.recoil);
        render("Gun", LACUNAE, poses, buffers, light, overlay);
        render("Grip", LACUNAE, poses, buffers, light, overlay);
        poses.pushPose();
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.rotate));
        render("Barrels", LACUNAE, poses, buffers, light, overlay);
        poses.popPose();
        flash(stack, 0, true, true, poses, buffers);
    }

    private void renderDualFirstPerson(ItemStack stack, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float aim = ClientWeaponEvents.aimingProgress(partial);
        for (int side = -1; side <= 1; side += 2) {
            int index = side < 0 ? 0 : 1;
            MiniAnimation animation = animation(stack, index);
            poses.pushPose();
            poses.translate(lerp(-2.2D * side, 0.0D, aim),
                    lerp(-1.4D, 0.0D, aim), lerp(2.0D, 0.0D, aim));
            poses.scale(0.375F, 0.375F, 0.375F);
            pivotX(poses, 0.0D, 3.0D, -6.0D, animation.equip);
            poses.translate(0.0D, 0.0D, animation.recoil);
            render(index == 0 ? "GunDual" : "Gun", DUAL, poses, buffers, light, overlay);
            poses.pushPose();
            poses.mulPose(Axis.ZP.rotationDegrees((float) (animation.rotate * side)));
            render("Barrels", DUAL, poses, buffers, light, overlay);
            poses.popPose();
            flash(stack, index, true, false, poses, buffers);
            poses.popPose();
        }
    }

    private void renderStatic(ItemDisplayContext context, boolean dual, PoseStack poses,
                              MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation texture = dual ? DUAL : LACUNAE;
        if (!dual) {
            render("Gun", texture, poses, buffers, light, overlay);
            render("Grip", texture, poses, buffers, light, overlay);
            render("Barrels", texture, poses, buffers, light, overlay);
            return;
        }
        boolean left = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        render(left ? "GunDual" : "Gun", texture, poses, buffers, light, overlay);
        render("Barrels", texture, poses, buffers, light, overlay);
        if (context == ItemDisplayContext.GUI) {
            poses.pushPose();
            poses.translate(0.0D, 0.0D, 8.0D);
            poses.mulPose(Axis.YP.rotationDegrees(180.0F));
            poses.mulPose(Axis.XP.rotationDegrees(-90.0F));
            render("Gun", texture, poses, buffers, light, overlay);
            render("Barrels", texture, poses, buffers, light, overlay);
            poses.popPose();
        }
    }

    private static void flash(ItemStack stack, int index, boolean first, boolean laser,
                              PoseStack poses, MultiBufferSource buffers) {
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack, index);
        if (elapsed < 0L || elapsed >= 50L) return;
        poses.pushPose();
        poses.translate(0.0D, laser ? 0.0D : 0.5D, first ? 12.0D : 12.25D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (laser) {
            SednaMuzzleFlash.renderLaser(poses, buffers, elapsed / 50.0F, 1.0F, 0xFFFF00FF);
            poses.translate(0.0D, 0.0D, -0.25D);
            SednaMuzzleFlash.renderLaser(poses, buffers, elapsed / 50.0F, 0.5F, 0xFFFF0080);
        } else {
            poses.scale(1.5F, 1.5F, 1.5F);
            SednaMuzzleFlash.render(poses, buffers, elapsed / 50.0F, 7.5F);
        }
        poses.popPose();
    }

    private MiniAnimation animation(ItemStack stack, int index) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time;
        Enum<?> animation;
        boolean aiming;
        if (stack.getItem() instanceof LacunaeItem) {
            time = (LacunaeItem.animationTimer(stack) + partial) * 50.0D;
            animation = LacunaeItem.animation(stack);
            aiming = LacunaeItem.aiming(stack);
        } else {
            time = (DualMinigunItem.animationTimer(stack, index) + partial) * 50.0D;
            animation = DualMinigunItem.animation(stack, index);
            aiming = stack.getItem() instanceof DualMinigunItem gun && gun.gunAiming(stack);
        }
        String name = animation.name();
        return switch (name) {
            case "EQUIP" -> new MiniAnimation(ease(time, 0, 1000, 45, 0), 0, 0);
            case "CYCLE" -> new MiniAnimation(0, recoil(time, aiming), rotate(time, false));
            case "CYCLE_DRY" -> new MiniAnimation(0, 0, rotate(time, false));
            case "RELOAD" -> new MiniAnimation(time < 250 ? ease(time, 0, 250, 0, -15)
                    : ease(time, 250, 750, -15, 0), 0, rotate(time, false));
            case "INSPECT" -> new MiniAnimation(time < 150 ? ease(time, 0, 150, 0, 3)
                    : ease(time, 150, 250, 3, 0), 0, rotate(time, true));
            default -> new MiniAnimation(0, 0, 0);
        };
    }

    private static double recoil(double time, boolean aiming) {
        double kick = aiming ? -0.25D : -0.5D;
        if (time < 100.0D) return kick;
        return time < 250.0D ? ease(time, 100, 250, kick, 0) : 0.0D;
    }

    private static double rotate(double time, boolean reverse) {
        double sign = reverse ? -1.0D : 1.0D;
        if (time < 50.0D) return ease(time, 0, 50, 0, sign * 60.0D);
        return time < 1050.0D ? ease(time, 50, 1050, sign * 60.0D, sign * 720.0D)
                : sign * 720.0D;
    }

    private void render(String group, ResourceLocation texture, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, 0xFFFFFFFF);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                MODEL, GROUPS, "Final Miniguns");
        return mesh;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses, boolean dual) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(0.875F / 16.0F, 0.875F / 16.0F, 0.875F / 16.0F);
                if (!dual) {
                    poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                    poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                    poses.translate(-0.25D, 0.5D, 0.0D);
                }
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                if (!dual) {
                    float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                    float aim = ClientWeaponEvents.aimingProgress(partial);
                    double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                    poses.translate(lerp(side * 1.4D, 0.0D, aim), lerp(-1.4D, -0.78125D, aim),
                            lerp(2.8D, 1.0D, aim));
                }
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                standardThirdPerson(context, poses);
                poses.scale(1.75F, 1.75F, 1.75F);
                double side = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? 2.0D : -1.0D;
                poses.translate(side, -3.5D, 8.0D);
            }
            default -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void standardThirdPerson(ItemDisplayContext context, PoseStack poses) {
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

    private static void pivotX(PoseStack poses, double x, double y, double z, double angle) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) angle));
        poses.translate(-x, -y, -z);
    }

    private static double ease(double time, double begin, double end,
                               double startValue, double endValue) {
        if (time <= begin) return startValue;
        if (time >= end) return endValue;
        double x = (time - begin) / (end - begin);
        double eased = 0.5D - Math.cos(x * Math.PI) * 0.5D;
        return startValue + (endValue - startValue) * eased;
    }

    private static double lerp(double start, double end, float progress) {
        return start + (end - start) * progress;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private record MiniAnimation(double equip, double recoil, double rotate) { }
}
