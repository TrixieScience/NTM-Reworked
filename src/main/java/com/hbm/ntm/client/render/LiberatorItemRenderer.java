package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.item.LiberatorItem;
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

/** Liberator renderer. Four barrels, four shells, several counting mistakes. */
public final class LiberatorItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation GUN = model("liberator_gun");
    public static final ModelResourceLocation BARREL = model("liberator_barrel");
    public static final ModelResourceLocation SHELL1 = model("liberator_shell1");
    public static final ModelResourceLocation SHELL2 = model("liberator_shell2");
    public static final ModelResourceLocation SHELL3 = model("liberator_shell3");
    public static final ModelResourceLocation SHELL4 = model("liberator_shell4");
    public static final ModelResourceLocation LATCH = model("liberator_latch");

    public LiberatorItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LiberatorItem)) return;
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

        poses.translate(0.0D, -1.0D, -3.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.equip.x));
        poses.translate(0.0D, 1.0D, 3.0D);

        // LIFT is imaginary and therefore perfectly stable.

        poses.translate(animation.recoil.x * 2.0D, animation.recoil.y, animation.recoil.z);
        poses.mulPose(Axis.XP.rotationDegrees((float) (animation.recoil.z * 10.0D)));

        poses.pushPose();
        poses.translate(0.0D, 0.25D, 7.25D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.375F, 0.375F, 0.375F);
        boolean reloading = LiberatorItem.state(stack) == LiberatorItem.GunState.RELOADING;
        poses.translate(0.0D, 0.0D, 0.25D / 0.375D);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D, WeaponSmokeRenderer.STANDARD, reloading);
        poses.translate(0.0D, 0.0D, -0.5D / 0.375D);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D, WeaponSmokeRenderer.STANDARD, reloading);
        poses.translate(0.0D, 0.5D / 0.375D, 0.0D);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D, WeaponSmokeRenderer.STANDARD, reloading);
        poses.translate(0.0D, 0.0D, 0.5D / 0.375D);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D, WeaponSmokeRenderer.STANDARD, reloading);
        poses.popPose();

        renderModel(GUN, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, -0.5D, 0.75D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.brk.x));
        poses.translate(0.0D, 0.5D, -0.75D);
        renderModel(BARREL, poses, buffers, light, overlay);

        renderShell(SHELL1, animation.shell1, poses, buffers, light, overlay);
        renderShell(SHELL2, animation.shell2, poses, buffers, light, overlay);
        renderShell(SHELL3, animation.shell3, poses, buffers, light, overlay);
        renderShell(SHELL4, animation.shell4, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 1.15625D, 0.75D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.latch.x));
        poses.translate(0.0D, -1.15625D, -0.75D);
        renderModel(LATCH, poses, buffers, light, overlay);
        poses.popPose();

        poses.popPose();
    }

    private static void renderShell(ModelResourceLocation model, Vec shell, PoseStack poses,
                                    MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(shell.x, shell.y, shell.z);
        renderModel(model, poses, buffers, light, overlay);
        poses.popPose();
    }

    private static void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        // Catalogue pose: show everything.
        renderModel(GUN, poses, buffers, light, overlay);
        renderModel(BARREL, poses, buffers, light, overlay);
        renderModel(SHELL1, poses, buffers, light, overlay);
        renderModel(SHELL2, poses, buffers, light, overlay);
        renderModel(SHELL3, poses, buffers, light, overlay);
        renderModel(SHELL4, poses, buffers, light, overlay);
        renderModel(LATCH, poses, buffers, light, overlay);
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                // Unflip the GUI, then perform the old inventory incantation.
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.5F / 16.0F, 1.5F / 16.0F, 1.5F / 16.0F);
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
                // Hip to sights.
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 1.2D, 0.0D, aim), lerp(-1.0D, -0.578125D, aim),
                        lerp(1.0D, 0.25D, aim));
                poses.scale(0.375F, 0.375F, 0.375F);
            }
            default -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void setupThirdPerson(ItemDisplayContext context, PoseStack poses) {
        // Maresleg hand bridge with the Liberator tail bolted on.
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
        poses.translate(0.0D, 1.0D, 3.0D);
    }

    private static Animation animation(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (LiberatorItem.animationTimer(stack) + partial) * 50.0D;
        int rounds = LiberatorItem.rounds(stack);
        return switch (LiberatorItem.animation(stack)) {
            case EQUIP -> new Animation(
                    sequence(time, frame(60, 0, 0, 0), frame(0, 0, 0, 500, Curve.SIN_DOWN)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new Animation(ZERO,
                    sequence(time, frame(0, 0, -2.5, 50, Curve.SIN_DOWN), frame(0, 0, 0, 350, Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE_DRY -> Animation.NONE; // empty BusAnimation: no gun movement.
            case RELOAD -> reload(time, rounds);
            case RELOAD_CYCLE -> reloadCycle(time, rounds, LiberatorItem.amountBeforeReload(stack));
            case RELOAD_END -> new Animation(ZERO, ZERO,
                    sequence(time, frame(15, 0, 0, 0), frame(15, 0, 0, 250), frame(0, 0, 0, 50)),
                    sequence(time, frame(60, 0, 0, 0), frame(0, 0, 0, 250, Curve.SIN_UP)),
                    // Off-by-one shell. Free ammunition, visually speaking.
                    rounds >= 0 ? ZERO : PUSHED, rounds >= 1 ? ZERO : PUSHED,
                    rounds >= 2 ? ZERO : PUSHED, rounds >= 3 ? ZERO : PUSHED);
            case JAMMED -> new Animation(ZERO, ZERO,
                    sequence(time, frame(15, 0, 0, 0), frame(15, 0, 0, 250), frame(0, 0, 0, 50),
                            frame(0, 0, 0, 550), frame(15, 0, 0, 100), frame(15, 0, 0, 600),
                            frame(0, 0, 0, 50)),
                    sequence(time, frame(60, 0, 0, 0), frame(0, 0, 0, 250, Curve.SIN_UP),
                            frame(0, 0, 0, 600), frame(45, 0, 0, 250, Curve.SIN_DOWN),
                            frame(45, 0, 0, 300), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    rounds >= 0 ? ZERO : PUSHED, rounds >= 1 ? ZERO : PUSHED,
                    rounds >= 2 ? ZERO : PUSHED, rounds >= 3 ? ZERO : PUSHED);
            case INSPECT -> new Animation(ZERO, ZERO,
                    sequence(time, frame(15, 0, 0, 100), frame(15, 0, 0, 1100), frame(0, 0, 0, 50)),
                    sequence(time, frame(0, 0, 0, 100), frame(60, 0, 0, 350, Curve.SIN_DOWN),
                            frame(60, 0, 0, 500), frame(0, 0, 0, 250, Curve.SIN_UP)),
                    // Inspect suddenly remembers how counting works.
                    rounds > 0 ? ZERO : PUSHED, rounds > 1 ? ZERO : PUSHED,
                    rounds > 2 ? ZERO : PUSHED, rounds > 3 ? ZERO : PUSHED);
            default -> Animation.NONE;
        };
    }

    private static Animation reload(double time, int rounds) {
        Vec latch = sequence(time, frame(15, 0, 0, 100));
        Vec brk = sequence(time, frame(0, 0, 0, 100), frame(60, 0, 0, 350, Curve.SIN_DOWN));
        return new Animation(ZERO, ZERO, latch, brk,
                shellReload(time, rounds, 0), shellReload(time, rounds, 1),
                shellReload(time, rounds, 2), shellReload(time, rounds, 3));
    }

    /** Only the shell currently being bullied into place moves. */
    private static Vec shellReload(double time, int rounds, int slot) {
        if (slot == rounds) {
            return sequence(time, frame(2, -4, -2, 0), frame(2, -4, -2, 400),
                    frame(0, 0, -2, 450, Curve.SIN_FULL), frame(0, 0, 0, 50, Curve.SIN_UP));
        }
        return slot < rounds ? ZERO : STATIC;
    }

    private static Animation reloadCycle(double time, int rounds, int startCount) {
        if (rounds >= 3) {
            // The last cycle returns null. Starting at two shells leaves number four hanging out.
            // This is horrible, authentic and unfortunately load-bearing.
            Vec shell4 = startCount == 2 ? STATIC : ZERO;
            return new Animation(ZERO, ZERO, new Vec(15, 0, 0), new Vec(60, 0, 0),
                    ZERO, ZERO, ZERO, shell4);
        }
        return new Animation(ZERO, ZERO, new Vec(15, 0, 0), new Vec(60, 0, 0),
                shellReloadCycle(time, rounds, 0), shellReloadCycle(time, rounds, 1),
                shellReloadCycle(time, rounds, 2), shellReloadCycle(time, rounds, 3));
    }

    /** Post-load count picks which shell performs next. */
    private static Vec shellReloadCycle(double time, int rounds, int slot) {
        if (slot == rounds + 1) {
            return sequence(time, frame(2, -4, -2, 0), frame(0, 0, -2, 450, Curve.SIN_FULL),
                    frame(0, 0, 0, 50, Curve.SIN_UP));
        }
        return slot <= rounds ? ZERO : STATIC;
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
        poses.translate(0.0D, 0.5D, 8.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F * shotRandom));
        poses.scale(1.5F, 1.5F, 1.5F);
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
    private static final Vec STATIC = new Vec(2, -4, -2);  // absent shell held out of the barrel (RELOAD/RELOAD_CYCLE)
    private static final Vec PUSHED = new Vec(2, -8, -2);   // pushed-away shell (RELOAD_END/JAMMED/INSPECT)

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

    /** Animation bus depot. LIFT missed the bus. */
    private record Animation(Vec equip, Vec recoil, Vec latch, Vec brk,
                             Vec shell1, Vec shell2, Vec shell3, Vec shell4) {
        private static final Animation NONE = new Animation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
