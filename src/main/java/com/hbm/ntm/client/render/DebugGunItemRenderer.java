package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.DebugGunItem;
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

/** The source Debug Gun reuses the heavy revolver mesh with its own skin. */
public final class DebugGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "models/weapons/lilmac.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/models/weapons/debug_gun.png");
    private static final Set<String> GROUPS = Set.of(
            "Pivot", "Casings", "Bullets", "Hammer", "Cylinder", "Scope", "Gun");

    private EnvsuitMesh mesh;

    public DebugGunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof DebugGunItem)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        poses.pushPose();
        setupContext(context, poses);
        if (firstPerson) renderFirstPerson(stack, poses, buffers, packedLight, packedOverlay);
        else renderStatic(poses, buffers, packedLight, packedOverlay);

        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long shot = Math.max(ClientWeaponEvents.lastShot(stack, 0),
                ClientWeaponEvents.lastShot(stack, 1));
        long elapsed = System.currentTimeMillis() - shot;
        if (held && elapsed >= 0L && elapsed < 75L) {
            poses.translate(0.125D, 2.5D, 0.0D);
            renderGapFlash(poses, buffers, elapsed / 75.0F);
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay) {
        Animation animation = animation(stack);
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float aim = ClientWeaponEvents.aimingProgress(partial);

        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.translate(6.0D, -3.0D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.equipSpin.x));
        poses.translate(-6.0D, 3.0D, 0.0D);

        poses.translate(lerp(0.0D, -animation.recoil.z, aim), 0.0D,
                lerp(animation.recoil.z, 0.0D, aim));
        poses.mulPose(Axis.ZP.rotationDegrees((float) (animation.recoil.z * 10.0D)));
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.reloadLift.x));
        poses.translate(animation.reloadJolt.x, 0.0D, 0.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.reloadTilt.x));

        render("Gun", poses, buffers, light, overlay);

        poses.pushPose();
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.cylinderFlip.x));
        render("Pivot", poses, buffers, light, overlay);
        poses.translate(0.0D, 1.75D, 0.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) (animation.drum.z * -60.0D)));
        poses.translate(0.0D, -1.75D, 0.0D);
        render("Cylinder", poses, buffers, light, overlay);
        poses.translate(animation.reloadBullets.x, animation.reloadBullets.y, animation.reloadBullets.z);
        if (animation.reloadBulletsCondition.x != 1.0D) render("Bullets", poses, buffers, light, overlay);
        render("Casings", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(4.0D, 1.25D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) (-30.0D + 30.0D * animation.hammer.z)));
        poses.translate(-4.0D, -1.25D, 0.0D);
        render("Hammer", poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        render("Gun", poses, buffers, light, overlay);
        render("Cylinder", poses, buffers, light, overlay);
        render("Bullets", poses, buffers, light, overlay);
        render("Casings", poses, buffers, light, overlay);
        render("Pivot", poses, buffers, light, overlay);
        render("Hammer", poses, buffers, light, overlay);
    }

    private void render(String group, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, 0xFFFFFFFF);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                MODEL, GROUPS, "Debug Gun");
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
                poses.scale(1.25F / 16.0F, 1.25F / 16.0F, 1.25F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
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
                poses.translate(0.0D, 0.0D, 1.0D);
                poses.translate(lerp(side * 0.8D, 0.0D, aim), lerp(-0.6D, -3.875D / 8.0D, aim),
                        lerp(0.8D, 0.0D, aim));
                poses.scale(0.125F, 0.125F, 0.125F);
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
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.translate(0.0D, 1.0D, 3.0D);
    }

    private static Animation animation(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        int index = DebugGunItem.animationTimer(stack, 1) < DebugGunItem.animationTimer(stack, 0)
                ? 1 : 0;
        double time = (DebugGunItem.animationTimer(stack, index) + partial) * 50.0D;
        return switch (DebugGunItem.animation(stack, index)) {
            case CYCLE -> new Animation(
                    sequence(time, frame(0,0,0,50), frame(0,0,-3,50), frame(0,0,0,250)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,1,50), frame(0,0,1,400), frame(0,0,0,200)),
                    sequence(time, frame(0,0,0,450), frame(0,0,1,200)));
            case CYCLE_DRY -> new Animation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,1,50), frame(0,0,1,400), frame(0,0,0,200)),
                    sequence(time, frame(0,0,0,450), frame(0,0,1,200)));
            case EQUIP -> new Animation(ZERO,
                    sequence(time, frame(-360,0,0,350)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case RELOAD -> reload(time);
            case INSPECT, JAMMED -> inspect(time);
            default -> Animation.NONE;
        };
    }

    private static Animation reload(double time) {
        return new Animation(ZERO, ZERO,
                sequence(time, frame(0,0,0,350), frame(-45,0,0,250), frame(-45,0,0,350),
                        frame(-15,0,0,200), frame(-15,0,0,1050), frame(0,0,0,100)),
                sequence(time, frame(0,0,0,600), frame(2,0,0,50), frame(0,0,0,100)),
                sequence(time, frame(-15,0,0,100), frame(65,0,0,100), frame(45,0,0,50),
                        frame(0,0,0,200), frame(0,0,0,1450), frame(-80,0,0,100),
                        frame(-80,0,0,100), frame(0,0,0,200)),
                sequence(time, frame(0,0,0,200), frame(90,0,0,100), frame(90,0,0,1700),
                        frame(0,0,0,70)),
                sequence(time, frame(0,0,0,650), frame(10,0,0,300), frame(10,0,0,200),
                        frame(0,0,0,700)),
                sequence(time, frame(1,0,0,0), frame(1,0,0,950), frame(0,0,0,1)),
                ZERO, ZERO);
    }

    private static Animation inspect(double time) {
        return new Animation(ZERO, ZERO, ZERO, ZERO,
                sequence(time, frame(-15,0,0,100), frame(65,0,0,100), frame(45,0,0,50),
                        frame(0,0,0,200), frame(0,0,0,200), frame(-80,0,0,100),
                        frame(-80,0,0,100), frame(0,0,0,200)),
                sequence(time, frame(0,0,0,200), frame(90,0,0,100), frame(90,0,0,450),
                        frame(0,0,0,70)),
                ZERO, ZERO, ZERO, ZERO);
    }

    private static Vec sequence(double time, Frame... frames) {
        double elapsed = 0.0D;
        Vec previous = ZERO;
        for (Frame frame : frames) {
            if (frame.duration <= 0.0D) {
                previous = frame.value;
                continue;
            }
            if (time < elapsed + frame.duration) {
                double progress = Math.max(0.0D, Math.min((time - elapsed) / frame.duration, 1.0D));
                return previous.lerp(frame.value, frame.curve.apply(progress));
            }
            elapsed += frame.duration;
            previous = frame.value;
        }
        return previous;
    }

    private static Frame frame(double x, double y, double z, double duration) {
        return frame(x, y, z, duration, Curve.LINEAR);
    }

    private static Frame frame(double x, double y, double z, double duration, Curve curve) {
        return new Frame(new Vec(x, y, z), duration, curve);
    }

    private static double lerp(double start, double end, float progress) {
        return start + (end - start) * progress;
    }

    private static void renderGapFlash(PoseStack poses, MultiBufferSource buffers, float progress) {
        float height = 4.0F * progress;
        float length = 15.0F * progress;
        float lift = 3.0F * progress;
        float offset = progress;
        float lengthOffset = 0.125F;
        VertexConsumer consumer = buffers.getBuffer(SednaMuzzleFlash.TYPE);
        PoseStack.Pose pose = poses.last();

        gapVertex(consumer, pose, 0, -height, -offset, 1, 1);
        gapVertex(consumer, pose, 0, height, -offset, 0, 1);
        gapVertex(consumer, pose, 0, height + lift, length - offset, 0, 0);
        gapVertex(consumer, pose, 0, -height + lift, length - offset, 1, 0);

        gapVertex(consumer, pose, 0, height, offset, 0, 1);
        gapVertex(consumer, pose, 0, -height, offset, 1, 1);
        gapVertex(consumer, pose, 0, -height + lift, -length + offset, 1, 0);
        gapVertex(consumer, pose, 0, height + lift, -length + offset, 0, 0);

        gapVertex(consumer, pose, 0, -height, -offset, 1, 1);
        gapVertex(consumer, pose, 0, height, -offset, 0, 1);
        gapVertex(consumer, pose, lengthOffset, height, length - offset, 0, 0);
        gapVertex(consumer, pose, lengthOffset, -height, length - offset, 1, 0);

        gapVertex(consumer, pose, 0, height, offset, 0, 1);
        gapVertex(consumer, pose, 0, -height, offset, 1, 1);
        gapVertex(consumer, pose, lengthOffset, -height, -length + offset, 1, 0);
        gapVertex(consumer, pose, lengthOffset, height, -length + offset, 0, 0);
    }

    private static void gapVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                  float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static final Vec ZERO = new Vec(0, 0, 0);
    private record Vec(double x, double y, double z) {
        Vec lerp(Vec other, double progress) {
            return new Vec(x + (other.x - x) * progress, y + (other.y - y) * progress,
                    z + (other.z - z) * progress);
        }
    }
    private record Frame(Vec value, double duration, Curve curve) { }
    private enum Curve {
        LINEAR { double apply(double x) { return x; } },
        SIN_DOWN { double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } };
        abstract double apply(double x);
    }
    private record Animation(Vec recoil, Vec equipSpin, Vec reloadLift, Vec reloadJolt,
                             Vec reloadTilt, Vec cylinderFlip, Vec reloadBullets,
                             Vec reloadBulletsCondition, Vec hammer, Vec drum) {
        private static final Animation NONE = new Animation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
