package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.item.MaresLegItem;
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

/** Maresleg renderer, lever clunks included. */
public final class MaresLegItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation GUN = model("maresleg_gun");
    public static final ModelResourceLocation STOCK = model("maresleg_stock");
    public static final ModelResourceLocation BARREL = model("maresleg_barrel");
    public static final ModelResourceLocation LEVER = model("maresleg_lever");
    public static final ModelResourceLocation SHELL = model("maresleg_shell");

    public MaresLegItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof MaresLegItem)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        poses.pushPose();
        setupContext(stack, context, poses);
        if (firstPerson) renderFirstPerson(stack, poses, buffers, packedLight, packedOverlay);
        else renderStatic(stack, poses, buffers, packedLight, packedOverlay);

        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long shot = ClientWeaponEvents.lastShot(stack);
        long elapsed = System.currentTimeMillis() - shot;
        if (held && elapsed >= 0L && elapsed < 75L) {
            double turn = firstPerson ? animation(stack).turn.z : 0.0D;
            renderMuzzleFlash(poses, buffers, elapsed / 75.0F, turn, shot,
                    MaresLegItem.isSawedOff(stack));
        }
        poses.popPose();
    }

    private static void renderFirstPerson(ItemStack stack, PoseStack poses,
                                          MultiBufferSource buffers, int light, int overlay) {
        Animation animation = animation(stack);
        boolean sawed = MaresLegItem.isSawedOff(stack);

        poses.translate(animation.recoil.x * 2.0D, animation.recoil.y, animation.recoil.z);
        poses.mulPose(Axis.XP.rotationDegrees((float) (animation.recoil.z * 5.0D)));
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.turn.z));
        poses.translate(0.0D, 0.0D, -4.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.lift.x));
        poses.translate(0.0D, 0.0D, 4.0D);

        poses.translate(0.0D, 0.0D, -4.0D);
        poses.mulPose(Axis.XN.rotationDegrees((float) animation.equip.x));
        poses.translate(0.0D, 0.0D, 4.0D);

        if (sawed) {
            poses.translate(0.0D, 0.0D, -2.0D);
            poses.mulPose(Axis.XN.rotationDegrees((float) shortFlip(stack)));
            poses.translate(0.0D, 0.0D, 2.0D);
        }

        poses.pushPose();
        poses.translate(0.0D, 1.0D, sawed ? 3.75D : 8.0D);
        poses.mulPose(Axis.ZN.rotationDegrees((float) animation.turn.z));
        if (sawed) poses.mulPose(Axis.XP.rotationDegrees((float) shortFlip(stack)));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.25D,
                WeaponSmokeRenderer.STANDARD,
                MaresLegItem.state(stack) == MaresLegItem.GunState.RELOADING);
        poses.popPose();

        renderModel(GUN, poses, buffers, light, overlay);
        if (!sawed) {
            renderModel(STOCK, poses, buffers, light, overlay);
            renderModel(BARREL, poses, buffers, light, overlay);
        }

        poses.pushPose();
        poses.translate(0.0D, 0.125D, -2.875D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.lever.x));
        poses.translate(0.0D, -0.125D, 2.875D);
        renderModel(LEVER, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.shell.x, animation.shell.y - 0.75D, animation.shell.z);
        renderModel(SHELL, poses, buffers, light, overlay);
        poses.popPose();

        if (animation.flag.x != 0.0D || animation.flag.y != 0.0D || animation.flag.z != 0.0D) {
            poses.pushPose();
            poses.translate(0.0D, -0.5D, 0.0D);
            renderModel(SHELL, poses, buffers, light, overlay);
            poses.popPose();
        }
    }

    private static void renderStatic(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                     int light, int overlay) {
        renderModel(GUN, poses, buffers, light, overlay);
        if (!MaresLegItem.isSawedOff(stack)) {
            renderModel(STOCK, poses, buffers, light, overlay);
            renderModel(BARREL, poses, buffers, light, overlay);
        }
        renderModel(LEVER, poses, buffers, light, overlay);
    }

    private static void setupContext(ItemStack stack, ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                // GUI adds a Y reflection that the Maresleg camera never asked for. Cancel it.
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                float scale = MaresLegItem.isSawedOff(stack) ? 2.5F : 1.4375F;
                poses.scale(scale / 16.0F, scale / 16.0F, scale / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(MaresLegItem.isSawedOff(stack) ? -1.0D : -0.5D,
                        MaresLegItem.isSawedOff(stack) ? 0.0D : 0.5D, 0.0D);
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
                poses.translate(lerp(side, 0.0D, aim), lerp(-0.8D, -0.484375D, aim),
                        lerp(1.6D, 1.0D, aim));
                poses.scale(0.375F, 0.375F, 0.375F);
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
        poses.scale(1.75F, 1.75F, 1.75F);
        poses.translate(0.0D, 0.25D, 3.0D);
    }

    private static Animation animation(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (MaresLegItem.animationTimer(stack) + partial) * 50.0D;
        boolean sawed = MaresLegItem.isSawedOff(stack);
        return switch (MaresLegItem.animation(stack)) {
            case EQUIP -> new Animation(ZERO,
                    sequence(time, frame(-60, 0, 0, 0), frame(0, 0, -3,
                            sawed ? 250 : 500, Curve.SIN_DOWN)),
                    ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new Animation(
                    sequence(time, frame(0, 0, 0, 50), frame(0, 0, -1, 50), frame(0, 0, 0, 250)),
                    ZERO,
                    sequence(time, frame(0, 0, 0, 600), frame(-85, 0, 0, 200), frame(0, 0, 0, 200)),
                    sawed ? ZERO : sequence(time, frame(0, 0, 0, 600),
                            frame(0, 0, 45, 200, Curve.SIN_DOWN), frame(0, 0, 0, 200, Curve.SIN_UP)),
                    ZERO, sawed ? new Vec(-20, 0, 0) : ZERO, ZERO);
            case CYCLE_DRY -> new Animation(ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 600), frame(-90, 0, 0, 200), frame(0, 0, 0, 200)),
                    sawed ? ZERO : sequence(time, frame(0, 0, 0, 600),
                            frame(0, 0, 45, 200, Curve.SIN_DOWN), frame(0, 0, 0, 200, Curve.SIN_UP)),
                    ZERO, sawed ? new Vec(-20, 0, 0) : ZERO, ZERO);
            case RELOAD -> reload(time, MaresLegItem.amountBeforeReload(stack) <= 0);
            case RELOAD_CYCLE -> reloadCycle(time);
            case RELOAD_END -> reloadEnd(time);
            case JAMMED -> sawed ? shortJammed(time) : jammed(time);
            case INSPECT -> new Animation(ZERO, ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 450), frame(0, 0, -90, 500, Curve.SIN_FULL),
                            frame(0, 0, -90, 500), frame(0, 0, 0, 500, Curve.SIN_FULL)),
                    sequence(time, frame(-35, 0, 0, 300, Curve.SIN_FULL),
                            frame(-35, 0, 0, 1150), frame(0, 0, 0, 500, Curve.SIN_FULL)), ZERO, ZERO);
            default -> Animation.NONE;
        };
    }

    private static double shortFlip(ItemStack stack) {
        if (MaresLegItem.animation(stack) != MaresLegItem.GunAnimation.CYCLE
                && MaresLegItem.animation(stack) != MaresLegItem.GunAnimation.CYCLE_DRY) return 0.0D;
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (MaresLegItem.animationTimer(stack) + partial) * 50.0D;
        return sequence(time, frame(0, 0, 0, 600), frame(360, 0, 0, 400)).x;
    }

    private static Animation shortJammed(double time) {
        Animation normal = jammed(time);
        return new Animation(normal.recoil, normal.equip, normal.lever, ZERO,
                normal.lift, normal.shell, normal.flag);
    }

    private static Animation reload(double time, boolean empty) {
        Vec flag = empty
                ? sequence(time, frame(0, 0, 0, 900), frame(1, 1, 1, 0))
                : sequence(time, frame(1, 1, 1, 0));
        return new Animation(ZERO, ZERO,
                sequence(time, frame(0, 0, 0, 400), frame(-85, 0, 0, 200)),
                ZERO,
                sequence(time, frame(30, 0, 0, 400, Curve.SIN_FULL)),
                sequence(time, frame(0, 0, 0, 600), frame(0, 0.25, -3, 0),
                        frame(0, empty ? 0.25 : 0.125, -1.5, 150, Curve.SIN_UP),
                        frame(0, empty ? 0.25 : -0.25, 0, 150, Curve.SIN_DOWN)),
                flag);
    }

    private static Animation reloadCycle(double time) {
        return new Animation(ZERO, ZERO, sequence(time, frame(-85, 0, 0, 0)), ZERO,
                sequence(time, frame(30, 0, 0, 0)),
                sequence(time, frame(0, 0.25, -3, 0),
                        frame(0, 0.125, -1.5, 150, Curve.SIN_UP),
                        frame(0, -0.125, 0, 150, Curve.SIN_DOWN)),
                sequence(time, frame(1, 1, 1, 0)));
    }

    private static Animation reloadEnd(double time) {
        return new Animation(ZERO, ZERO,
                sequence(time, frame(-85, 0, 0, 0), frame(0, 0, 0, 200)), ZERO,
                sequence(time, frame(30, 0, 0, 0), frame(30, 0, 0, 250),
                        frame(0, 0, 0, 400, Curve.SIN_FULL)),
                ZERO, sequence(time, frame(1, 1, 1, 0)));
    }

    private static Animation jammed(double time) {
        return new Animation(ZERO, ZERO,
                sequence(time, frame(-85, 0, 0, 0), frame(-15, 0, 0, 200),
                        frame(-15, 0, 0, 650), frame(-85, 0, 0, 200),
                        frame(-15, 0, 0, 200), frame(-15, 0, 0, 200),
                        frame(-85, 0, 0, 200), frame(0, 0, 0, 200)),
                sequence(time, frame(0, 0, 0, 850), frame(0, 0, 45, 200, Curve.SIN_DOWN),
                        frame(0, 0, 45, 800), frame(0, 0, 0, 200, Curve.SIN_UP)),
                sequence(time, frame(30, 0, 0, 0), frame(30, 0, 0, 250),
                        frame(0, 0, 0, 400, Curve.SIN_FULL)),
                ZERO, sequence(time, frame(1, 1, 1, 0)));
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
                                          double turn, long shot, boolean sawed) {
        poses.pushPose();
        poses.translate(0.0D, 1.0D, sawed ? 3.75D : 8.0D);
        poses.mulPose(Axis.ZN.rotationDegrees((float) turn));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees((shot & 3L) * 90.0F));
        SednaMuzzleFlash.render(poses, buffers, progress, 5.0F);
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
    private record Animation(Vec recoil, Vec equip, Vec lever, Vec turn,
                             Vec lift, Vec shell, Vec flag) {
        private static final Animation NONE = new Animation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
