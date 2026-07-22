package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.LagPistolItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Source ItemRenderLAG model groups, JSON animations, transforms, and muzzle plume. */
public final class LagPistolItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "models/weapons/mike_hawk.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/models/weapons/lag.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "models/weapons/animations/lag.json");
    private static final Set<String> GROUPS = Set.of("Grip", "Slide", "Hammer", "Bullet", "Magazine");

    private EnvsuitMesh mesh;
    private LegacyWeaponAnimation animations;

    public LagPistolItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LagPistolItem)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);
        boolean flash = held && elapsed >= 0L && elapsed < 75L;

        poses.pushPose();
        setupContext(context, poses);
        if (firstPerson) {
            renderFirstPerson(stack, poses, buffers, packedLight, packedOverlay,
                    flash, elapsed / 75.0F);
        } else {
            renderStatic(poses, buffers, packedLight, packedOverlay);
            if (flash) {
                poses.pushPose();
                poses.translate(-10.25D, 1.0D, 0.0D);
                poses.mulPose(Axis.XP.rotationDegrees(90.0F * ClientWeaponEvents.shotRandom(stack)));
                renderMuzzleFlash(poses, buffers, elapsed / 75.0F);
                poses.popPose();
            }
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay, boolean flash, float flashProgress) {
        double time = animationTime(stack);
        String clip = animationClip(stack);

        poses.scale(0.25F, 0.25F, 0.25F);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));

        poses.translate(4.0D, -4.0D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) -equipRotation(stack, time)));
        poses.translate(-4.0D, 4.0D, 0.0D);

        Vec addTranslation = inspectBus(stack, time, new Vec(-4.0D, 0.0D, -3.0D));
        Vec addRotation = inspectBus(stack, time, new Vec(0.0D, -2.0D, 5.0D));
        poses.translate(addTranslation.x, addTranslation.y, addTranslation.z);
        poses.mulPose(Axis.ZP.rotationDegrees((float) addRotation.z));
        poses.mulPose(Axis.YP.rotationDegrees((float) addRotation.y));

        poses.pushPose();
        apply(poses, clip, "Grip", time);
        render("Grip", poses, buffers, light, overlay);

        poses.pushPose();
        apply(poses, clip, "Slide", time);
        render("Slide", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(3.125D, 0.125D, 0.0D);
        poses.mulPose(Axis.ZN.rotationDegrees(25.0F));
        poses.translate(-3.125D, -0.125D, 0.0D);
        apply(poses, clip, "Hammer", time);
        render("Hammer", poses, buffers, light, overlay);
        poses.popPose();

        if (LagPistolItem.rounds(stack) > 0) {
            poses.pushPose();
            apply(poses, clip, "Bullet", time);
            render("Bullet", poses, buffers, light, overlay);
            poses.popPose();
        }

        poses.pushPose();
        apply(poses, clip, "Magazine", time);
        render("Magazine", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(-10.25D, 1.0D, 0.0D);
        poses.scale(0.5F, 0.5F, 0.5F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.5D,
                WeaponSmokeRenderer.STANDARD,
                LagPistolItem.state(stack) == LagPistolItem.GunState.RELOADING);
        poses.popPose();

        if (flash) {
            poses.pushPose();
            poses.translate(-10.25D, 1.0D, 0.0D);
            poses.mulPose(Axis.XP.rotationDegrees(90.0F * ClientWeaponEvents.shotRandom(stack)));
            renderMuzzleFlash(poses, buffers, flashProgress);
            poses.popPose();
        }
        poses.popPose();
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        render("Grip", poses, buffers, light, overlay);
        render("Slide", poses, buffers, light, overlay);
        render("Hammer", poses, buffers, light, overlay);
    }

    private void apply(PoseStack poses, String clip, String group, double time) {
        if (clip != null) LegacyWeaponAnimation.apply(poses, animations().transform(clip, group, time));
    }

    private void render(String group, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, 0xFFFFFFFF);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Comically Long Pistol");
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
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.5F / 16.0F, 1.5F / 16.0F, 1.5F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(2.5D, 1.0D, 0.0D);
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
                poses.translate(lerp(side * 1.2D, 0.0D, aim), lerp(-0.8D, -0.421875D, aim),
                        lerp(1.2D, 0.5D, aim));
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
        poses.translate(0.0D, 1.0D, 1.0D);
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (LagPistolItem.animationTimer(stack) + partial) * 50.0D;
    }

    private static String animationClip(ItemStack stack) {
        return switch (LagPistolItem.animation(stack)) {
            case CYCLE -> "Firing";
            case CYCLE_DRY -> "Dryfire";
            case RELOAD -> "Reload";
            case JAMMED -> "Jam";
            case INSPECT -> "Inspect";
            default -> null;
        };
    }

    private static double equipRotation(ItemStack stack, double time) {
        if (LagPistolItem.animation(stack) != LagPistolItem.GunAnimation.EQUIP) return 0.0D;
        double progress = Math.max(0.0D, Math.min(time / 350.0D, 1.0D));
        return -90.0D + 90.0D * Math.sin(progress * Math.PI * 0.5D);
    }

    private static Vec inspectBus(ItemStack stack, double time, Vec target) {
        if (LagPistolItem.animation(stack) != LagPistolItem.GunAnimation.INSPECT) return Vec.ZERO;
        if (time < 500.0D) return Vec.ZERO.lerp(target, Math.max(0.0D, time / 500.0D));
        if (time < 2500.0D) return target;
        if (time < 3000.0D) return target.lerp(Vec.ZERO, (time - 2500.0D) / 500.0D);
        return Vec.ZERO;
    }

    private static double lerp(double start, double end, float progress) {
        return start + (end - start) * progress;
    }

    private static void renderMuzzleFlash(PoseStack poses, MultiBufferSource buffers, float progress) {
        float width = 6.0F * progress;
        float length = 7.5F * progress;
        float inset = 2.0F;
        VertexConsumer consumer = buffers.getBuffer(SednaMuzzleFlash.TYPE);
        PoseStack.Pose pose = poses.last();

        flashVertex(consumer, pose, 0, -width, -inset, 1, 1);
        flashVertex(consumer, pose, 0, width, -inset, 0, 1);
        flashVertex(consumer, pose, 0.1F, width, length - inset, 0, 0);
        flashVertex(consumer, pose, 0.1F, -width, length - inset, 1, 0);
        flashVertex(consumer, pose, 0, width, inset, 0, 1);
        flashVertex(consumer, pose, 0, -width, inset, 1, 1);
        flashVertex(consumer, pose, 0.1F, -width, -length + inset, 1, 0);
        flashVertex(consumer, pose, 0.1F, width, -length + inset, 0, 0);
        flashVertex(consumer, pose, 0, -inset, width, 0, 1);
        flashVertex(consumer, pose, 0, -inset, -width, 1, 1);
        flashVertex(consumer, pose, 0.1F, length - inset, -width, 1, 0);
        flashVertex(consumer, pose, 0.1F, length - inset, width, 0, 0);
        flashVertex(consumer, pose, 0, inset, -width, 1, 1);
        flashVertex(consumer, pose, 0, inset, width, 0, 1);
        flashVertex(consumer, pose, 0.1F, -length + inset, width, 0, 0);
        flashVertex(consumer, pose, 0.1F, -length + inset, -width, 1, 0);
    }

    private static void flashVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                    float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private record Vec(double x, double y, double z) {
        private static final Vec ZERO = new Vec(0.0D, 0.0D, 0.0D);

        private Vec lerp(Vec other, double progress) {
            return new Vec(x + (other.x - x) * progress, y + (other.y - y) * progress,
                    z + (other.z - z) * progress);
        }
    }
}
