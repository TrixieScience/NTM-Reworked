package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.item.ShredderItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** Shredder B-side: same choreography, older paint job. */
public final class ShredderItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation GUN = model("shredder_orig_gun");
    public static final ModelResourceLocation MAGAZINE = model("shredder_orig_magazine");
    public static final ModelResourceLocation SHELLS = model("shredder_orig_shells");

    public ShredderItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof ShredderItem)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        poses.pushPose();
        setupContext(context, poses);
        if (firstPerson) renderFirstPerson(stack, poses, buffers, packedLight, packedOverlay);
        else renderStatic(poses, buffers, packedLight, packedOverlay);

        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long shot = ClientWeaponEvents.lastShot(stack);
        long elapsed = System.currentTimeMillis() - shot;
        if (held && elapsed >= 0L && elapsed < 75L) {
            renderMuzzleFlash(poses, buffers, elapsed / 75.0F, ClientWeaponEvents.shotRandom(stack));
        }
        poses.popPose();
    }

    private static void renderFirstPerson(ItemStack stack, PoseStack poses,
                                          MultiBufferSource buffers, int light, int overlay) {
        Animation animation = animation(stack);

        poses.translate(0.0D, -2.0D, -6.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.equip.x));
        poses.translate(0.0D, 2.0D, 6.0D);

        poses.translate(0.0D, 0.0D, -4.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.lift.x));
        poses.translate(0.0D, 0.0D, 4.0D);

        poses.translate(0.0D, 0.0D, animation.recoil.z);

        renderModel(GUN, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        poses.translate(0.0D, -1.0D, -0.5D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.speen.x));
        poses.translate(0.0D, 1.0D, 0.5D);
        renderModel(MAGAZINE, poses, buffers, light, overlay);
        poses.translate(0.0D, -1.0D, -0.5D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.cycle.z));
        poses.translate(0.0D, 1.0D, 0.5D);
        renderModel(SHELLS, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 1.0D, 7.5D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.75F, 0.75F, 0.75F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.5D,
                WeaponSmokeRenderer.STANDARD,
                ShredderItem.state(stack) == ShredderItem.GunState.RELOADING);
        poses.popPose();
    }

    private static void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        renderModel(GUN, poses, buffers, light, overlay);
        renderModel(MAGAZINE, poses, buffers, light, overlay);
        renderModel(SHELLS, poses, buffers, light, overlay);
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
                poses.translate(-1.5D, 0.0D, 0.0D);
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
                // Hip to sights.
                poses.translate(lerp(side * 1.2D, 0.0D, aim), lerp(-1.0D, -0.78125D, aim),
                        lerp(1.2D, 0.5D, aim));
                poses.scale(0.25F, 0.25F, 0.25F);
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
        // Shared third-person hand bridge.
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 15.0F));
        poses.mulPose(Axis.YP.rotationDegrees(side * 12.5F));
        poses.mulPose(Axis.XP.rotationDegrees(15.0F));
        poses.translate(side * 3.5D, 0.0D, 0.0D);
        // Shredder-specific third-person bulk.
        poses.scale(1.5F, 1.5F, 1.5F);
        poses.translate(0.0D, 0.5D, 4.0D);
    }

    private static Animation animation(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (ShredderItem.animationTimer(stack) + partial) * 50.0D;
        return switch (ShredderItem.animation(stack)) {
            case EQUIP -> new Animation(
                    sequence(time, frame(60, 0, 0, 0), frame(0, 0, 0, 500, Curve.SIN_DOWN)),
                    ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new Animation(ZERO, ZERO,
                    sequence(time, frame(0, 0, -1, 50, Curve.SIN_DOWN), frame(0, 0, 0, 150, Curve.SIN_FULL)),
                    ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 150), frame(0, 0, 18, 100)));
            case CYCLE_DRY -> new Animation(ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 150), frame(0, 0, 18, 100)));
            case JAMMED -> new Animation(ZERO,
                    sequence(time, frame(0, 0, 0, 750), frame(-2, 0, 0, 100, Curve.SIN_DOWN),
                            frame(0, 0, 0, 100, Curve.SIN_FULL)),
                    ZERO,
                    sequence(time, frame(0, 0, 0, 500), frame(0, -2, 0, 150, Curve.SIN_UP),
                            frame(0, 0, 0, 100)),
                    ZERO, ZERO);
            case INSPECT -> new Animation(ZERO,
                    sequence(time, frame(0, 0, 0, 1450), frame(-2, 0, 0, 100, Curve.SIN_DOWN),
                            frame(0, 0, 0, 100, Curve.SIN_FULL)),
                    ZERO,
                    sequence(time, frame(0, -1, 0, 150), frame(6, -1, 0, 150),
                            frame(6, 12, 0, 350, Curve.SIN_DOWN), frame(6, -2, 0, 350, Curve.SIN_UP),
                            frame(6, -1, 0, 50), frame(6, -1, 0, 100),
                            frame(0, -1, 0, 150, Curve.SIN_FULL), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    sequence(time, frame(0, 0, 0, 300), frame(360, 0, 0, 700)),
                    ZERO);
            default -> Animation.NONE;
        };
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

    private static void renderModel(ModelResourceLocation location, PoseStack poses, MultiBufferSource buffers,
                                    int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(location);
        ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
        VertexConsumer consumer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        renderer.renderModel(poses.last(), consumer, Blocks.IRON_BLOCK.defaultBlockState(), model,
                1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
    }

    private static void renderMuzzleFlash(PoseStack poses, MultiBufferSource buffers, float progress,
                                          float shotRandom) {
        poses.pushPose();
        poses.translate(0.0D, 1.0D, 7.5D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(shotRandom * 90.0F));
        poses.scale(0.75F, 0.75F, 0.75F);
        SednaMuzzleFlash.render(poses, buffers, progress, 7.5F);
        poses.popPose();
    }

    private static void flashQuad(VertexConsumer consumer, PoseStack.Pose pose,
                                  float width, float length, boolean vertical) {
        flashVertex(consumer, pose, vertical ? 0 : -width, vertical ? -width : 0, 0, 0, 1);
        flashVertex(consumer, pose, vertical ? 0 : width, vertical ? width : 0, 0, 1, 1);
        flashVertex(consumer, pose, vertical ? 0 : width, vertical ? width : 0, length, 1, 0);
        flashVertex(consumer, pose, vertical ? 0 : -width, vertical ? -width : 0, length, 0, 0);
    }

    private static void flashVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                    float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static ModelResourceLocation model(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "item/" + path));
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
        SIN_UP { double apply(double x) { return 1.0D - Math.cos(x * Math.PI * 0.5D); } },
        SIN_DOWN { double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } },
        SIN_FULL { double apply(double x) { return (-Math.cos(x * Math.PI) + 1.0D) * 0.5D; } };
        abstract double apply(double x);
    }
    /** Equip, lift, recoil, magazine, speen, cycle. */
    private record Animation(Vec equip, Vec lift, Vec recoil, Vec mag, Vec speen, Vec cycle) {
        private static final Animation NONE = new Animation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
